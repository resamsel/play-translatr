package services.impl;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static utils.Stopwatch.log;

import criterias.ProjectCriteria;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.Locale;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.MessageRepository;
import repositories.ProjectRepository;
import services.CacheService;
import services.KeyService;
import services.LocaleService;
import services.LogEntryService;
import services.MessageService;
import services.ProjectService;
import services.ProjectUserService;

/**
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class ProjectServiceImpl extends AbstractModelService<Project, UUID, ProjectCriteria>
    implements ProjectService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

  private final ProjectRepository projectRepository;
  private final LocaleService localeService;
  private final KeyService keyService;
  private final MessageService messageService;
  private final MessageRepository messageRepository;
  private final ProjectUserService projectUserService;

  @Inject
  public ProjectServiceImpl(Validator validator, CacheService cache,
      ProjectRepository projectRepository, LocaleService localeService, KeyService keyService,
      MessageService messageService, MessageRepository messageRepository,
      ProjectUserService projectUserService, LogEntryService logEntryService) {
    super(validator, cache, projectRepository, Project::getCacheKey, logEntryService);

    this.projectRepository = projectRepository;
    this.localeService = localeService;
    this.keyService = keyService;
    this.messageService = messageService;
    this.messageRepository = messageRepository;
    this.projectUserService = projectUserService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Project byOwnerAndName(String username, String name, String... fetches) {
    return log(
        () -> cache.getOrElse(
            Project.getCacheKey(username, name, fetches),
            () -> projectRepository.byOwnerAndName(username, name, fetches),
            10 * 30
        ),
        LOGGER,
        "byOwnerAndName"
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void increaseWordCountBy(UUID projectId, int wordCountDiff) {
    if (wordCountDiff == 0) {
      LOGGER.debug("Not changing word count");
      return;
    }

    Project project = byId(projectId);
    if (project == null) {
      return;
    }

    if (project.wordCount == null) {
      project.wordCount = 0;
    }
    project.wordCount += wordCountDiff;

    log(
        () -> modelRepository.save(project),
        LOGGER,
        "Increased word count by %d",
        wordCountDiff
    );

    postUpdate(project);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetWordCount(UUID projectId) {
    Project project = byId(projectId, ProjectRepository.FETCH_LOCALES);
    List<UUID> localeIds = project.locales.stream().map(Locale::getId).collect(toList());

    project.wordCount = null;

    modelRepository.save(project);

    postUpdate(project);

    localeService.resetWordCount(projectId);
    keyService.resetWordCount(projectId);
    messageService.resetWordCount(projectId);
    messageService.save(messageRepository.byLocales(localeIds));
  }

  @Override
  public void changeOwner(Project project, User owner) {
    LOGGER.debug("changeOwner(project={}, owner={})", project, owner);

    requireNonNull(project, "project");
    requireNonNull(owner, "owner");

    // Make old owner a member of type Manager
    ProjectUser ownerRole = project.members.stream()
        .filter(m -> m.role == ProjectRole.Owner)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Project has no owner"));

    ownerRole.role = ProjectRole.Manager;

    // Make new owner a member of type Owner
    ProjectUser newOwnerRole = project.members.stream()
        .filter(m -> m.user.id.equals(owner.id))
        .findFirst()
        .orElseGet(() -> new ProjectUser(ProjectRole.Manager).withProject(project).withUser(owner));

    newOwnerRole.role = ProjectRole.Owner;

    if (newOwnerRole.id == null) {
      project.members.add(newOwnerRole);
    }

    update(project.withOwner(owner));
    projectUserService.update(ownerRole);
    projectUserService.update(newOwnerRole);
  }

  @Override
  protected void postCreate(Project t) {
    super.postCreate(t);

    // When project has been created, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:");
  }

  @Override
  protected void postUpdate(Project t) {
    // When project has been updated, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:");

    Project cached = cache.get(Project.getCacheKey(t.id));
    if (cached != null) {
      cache.removeByPrefix(Project.getCacheKey(
          requireNonNull(cached.owner, "owner (cached)").username,
          cached.name
      ));
    } else {
      cache.removeByPrefix(Project.getCacheKey(
          requireNonNull(t.owner, "owner").username,
          ""
      ));
    }

    super.postUpdate(t);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postDelete(Project t) {
    super.postDelete(t);

    // When key has been deleted, the project cache needs to be invalidated
    cache.removeByPrefix("project:criteria:" + t.id);

    cache.removeByPrefix(Project.getCacheKey(t.owner.username, t.name));
  }
}
