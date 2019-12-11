package repositories.impl;

import actors.ActivityActorRef;
import actors.ActivityProtocol.Activities;
import actors.ActivityProtocol.Activity;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.KeyCriteria;
import criterias.PagedListFactory;
import dto.PermissionException;
import mappers.KeyMapper;
import models.ActionType;
import models.Key;
import models.Message;
import models.Project;
import models.ProjectRole;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.KeyRepository;
import repositories.MessageRepository;
import repositories.Persistence;
import services.PermissionService;
import utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static utils.Stopwatch.log;

@Singleton
public class KeyRepositoryImpl extends AbstractModelRepository<Key, UUID, KeyCriteria> implements
    KeyRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyRepositoryImpl.class);

  private final Find<UUID, Key> find = new Find<UUID, Key>() {
  };

  private final MessageRepository messageRepository;
  private final PermissionService permissionService;

  @Inject
  public KeyRepositoryImpl(Persistence persistence,
                           Validator validator,
                           ActivityActorRef activityActor,
                           MessageRepository messageRepository,
                           PermissionService permissionService) {
    super(persistence, validator, activityActor);

    this.messageRepository = messageRepository;
    this.permissionService = permissionService;
  }

  @Override
  public PagedList<Key> findBy(KeyCriteria criteria) {
    Query<Key> q = fetch(criteria.getFetches());

    ExpressionList<Key> query = q.where();

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    if (criteria.getNames() != null && !criteria.getNames().isEmpty()) {
      query.in("name", criteria.getNames());
    }

    if (criteria.getSearch() != null) {
      query.disjunction().ilike("name", "%" + criteria.getSearch() + "%")
          .exists(Ebean.createQuery(Message.class).where().raw("key.id = k.id")
              .ilike("value", "%" + criteria.getSearch() + "%").query())
          .endJunction();
    }

    if (criteria.getProjectOwnerUsername() != null) {
      query.eq("project.owner.username", criteria.getProjectOwnerUsername());
    }

    if (criteria.getProjectName() != null) {
      query.eq("project.name", criteria.getProjectName());
    }

    if (Boolean.TRUE.equals(criteria.getMissing())) {
      ExpressionList<Message> messageQuery =
          Ebean.createQuery(Message.class).where().raw("key.id = k.id");

      if (criteria.getLocaleId() != null) {
        messageQuery.eq("locale.id", criteria.getLocaleId());
      }

      query.notExists(messageQuery.query());
    }

    if (criteria.getOrder() != null) {
      query.setOrderBy(criteria.getOrder());
    }

    criteria.paged(query);

    return log(() -> PagedListFactory.create(query), LOGGER, "findBy");
  }

  @Override
  public Key byId(UUID id, String... fetches) {
    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP).findUnique();
  }

  @Override
  public List<Key> latest(Project project, int limit) {
    return fetch().where().eq("project", project).order("whenUpdated desc")
        .setMaxRows(limit).findList();
  }

  @Override
  public Key byProjectAndName(Project project, String name) {
    if (project == null) {
      return null;
    }

    return byProjectAndName(project.id, name);
  }

  public Key byProjectAndName(UUID projectId, String name) {
    return fetch().where().eq("project.id", projectId).eq("name", name).findUnique();
  }

  @Override
  public Key byOwnerAndProjectAndName(String username, String projectName, String keyName, String... fetches) {
    return fetch(fetches)
        .where()
        .eq("project.owner.username", username)
        .eq("project.name", projectName)
        .eq("name", keyName)
        .findUnique();
  }

  private Query<Key> fetch(List<String> fetches) {
    return fetch(fetches.toArray(new String[0]));
  }

  private Query<Key> fetch(String... fetches) {
    return QueryUtils.fetch(find.query().alias("k").setDisableLazyLoading(true),
        QueryUtils.mergeFetches(PROPERTIES_TO_FETCH, fetches), FETCH_MAP);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void prePersist(Key t, boolean update) {
    if (update) {
      activityActor.tell(
          new Activity<>(
              ActionType.Update, User.loggedInUser(), t.project, dto.Key.class,
              KeyMapper.toDto(byId(t.id)), KeyMapper.toDto(t)),
          null
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(Key t, boolean update) {
    if (!update) {
      activityActor.tell(
          new Activity<>(ActionType.Create, User.loggedInUser(), t.project, dto.Key.class, null, KeyMapper.toDto(t)),
          null
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Key t) {
    if (!permissionService
        .hasPermissionAny(t.project.id, User.loggedInUser(), ProjectRole.Owner, ProjectRole.Manager,
            ProjectRole.Developer)) {
      throw new PermissionException("User not allowed in project");
    }

    activityActor.tell(
        new Activity<>(ActionType.Delete, User.loggedInUser(), t.project, dto.Key.class, KeyMapper.toDto(t), null),
        null
    );

    messageRepository.delete(messageRepository.byKey(t));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preDelete(Collection<Key> t) {
    activityActor.tell(
        new Activities<>(
            t.stream()
                .map(k -> new Activity<>(ActionType.Delete, User.loggedInUser(), k.project, dto.Key.class,
                    KeyMapper.toDto(k), null))
                .collect(Collectors.toList())),
        null
    );

    messageRepository
        .delete(messageRepository.byKeys(t.stream().map(k -> k.id).collect(Collectors.toList())));
  }
}
