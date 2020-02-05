package services.impl;

import criterias.KeyCriteria;
import models.Key;
import models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.KeyRepository;
import repositories.Persistence;
import services.CacheService;
import services.KeyService;
import services.LogEntryService;

import javax.inject.Inject;
import javax.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static utils.Stopwatch.log;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
public class KeyServiceImpl extends AbstractModelService<Key, UUID, KeyCriteria>
    implements KeyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceImpl.class);

  private final KeyRepository keyRepository;
  private final Persistence persistence;

  private final CacheService cache;

  @Inject
  public KeyServiceImpl(Validator validator, CacheService cache, KeyRepository keyRepository,
                        LogEntryService logEntryService, Persistence persistence) {
    super(validator, cache, keyRepository, Key::getCacheKey, logEntryService);

    this.cache = cache;
    this.keyRepository = keyRepository;
    this.persistence = persistence;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<UUID, Double> progress(UUID projectId) {
    return keyRepository.progress(projectId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void increaseWordCountBy(UUID keyId, int wordCountDiff) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Key key = modelRepository.byId(keyId);

    if (key == null) {
      return;
    }

    if (key.wordCount == null) {
      key.wordCount = 0;
    }
    key.wordCount += wordCountDiff;

    log(
        () -> modelRepository.persist(key),
        LOGGER,
        "Increased word count by %d",
        wordCountDiff
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    try {
      persistence.createSqlUpdate("update key set word_count = null where project_id = :projectId")
          .setParameter("projectId", projectId).execute();
    } catch (Exception e) {
      LOGGER.error("Error while resetting word count", e);
    }
  }

  @Override
  public List<Key> latest(Project project, int limit) {
    return cache.getOrElse(
        String.format("project:id:%s:latest:keys:%d", project.id, limit),
        () -> keyRepository.latest(project, limit),
        60
    );
  }

  @Override
  public Key byProjectAndName(Project project, String name) {
    return cache.getOrElse(
        getCacheKey(project.id, name),
        () -> keyRepository.byProjectAndName(project, name),
        60
    );
  }

  @Override
  public Key byOwnerAndProjectAndName(String username, String projectName, String keyName, String... fetches) {
    return cache.getOrElse(
        String.format("key:owner:%s:projectName:%s:name:%s", username, projectName, keyName),
        () -> keyRepository.byOwnerAndProjectAndName(username, projectName, keyName, fetches),
        60
    );
  }

  @Override
  protected void postCreate(Key t) {
    super.postCreate(t);

    // When key has been created, the project cache needs to be invalidated
    cache.remove(Project.getCacheKey(t.project.id));
    if (t.project.owner != null) {
      cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));
      cache.removeByPrefix("key:criteria:null:" + t.project.owner.username + ":" + t.project.name);
    }

    cache.removeByPrefix("key:criteria:" + t.project.id);
  }

  @Override
  protected void postUpdate(Key t) {
    Key existing = cache.get(Key.getCacheKey(t.id));
    if (existing != null) {
      cache.removeByPrefix(getCacheKey(existing.project.id, existing.name));
    } else {
      cache.removeByPrefix(getCacheKey(t.project.id, ""));
    }

    super.postUpdate(t);

    // When locale has been updated, the locale cache needs to be invalidated
    cache.removeByPrefix("key:criteria:" + t.project.id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Key t) {
    Key existing = byId(t.id);
    if (existing != null) {
      cache.removeByPrefix(getCacheKey(existing.project.id, existing.name));
    }

    super.postDelete(t);

    // When key has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix(Project.getCacheKey(t.project.id));
    cache.removeByPrefix(Project.getCacheKey(t.project.owner.username, t.project.name));

    // When key has been deleted, the key cache needs to be invalidated
    cache.removeByPrefix("key:criteria:" + t.project.id);
  }

  private static String getCacheKey(UUID projectId, String keyName) {
    return String.format("key:project:%s:name:%s", projectId, keyName);
  }
}
