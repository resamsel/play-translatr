package repositories.impl;

import actors.NotificationActor;
import actors.NotificationProtocol.Notification;
import akka.actor.ActorRef;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import criterias.HasNextPagedList;
import criterias.LogEntryCriteria;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.LogEntry;
import models.Project;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.cache.CacheApi;
import repositories.LogEntryRepository;
import utils.ContextKey;
import utils.QueryUtils;

@Singleton
public class LogEntryRepositoryImpl extends
    AbstractModelRepository<LogEntry, UUID, LogEntryCriteria> implements
    LogEntryRepository {

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList("user", "project");

  private final Find<UUID, LogEntry> find = new Find<UUID, LogEntry>() {
  };

  private final ActorRef notificationActor;

  @Inject
  public LogEntryRepositoryImpl(Validator validator, CacheApi cache,
      @Named(NotificationActor.NAME) ActorRef notificationActor) {
    super(validator, cache, null);

    this.notificationActor = notificationActor;
  }

  @Override
  public PagedList<LogEntry> findBy(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = findQuery(criteria);

    if (criteria.getOrder() != null) {
      query.order(criteria.getOrder());
    } else {
      query.order("whenCreated desc");
    }

    criteria.paged(query);

    return HasNextPagedList.create(query.query().fetch("user").fetch("project"));
  }

  @Override
  public LogEntry byId(UUID id, String... fetches) {
    HashSet<String> propertiesToFetch = new HashSet<>(PROPERTIES_TO_FETCH);
    if (fetches.length > 0) {
      propertiesToFetch.addAll(Arrays.asList(fetches));
    }

    return QueryUtils.fetch(find.setId(id).setDisableLazyLoading(true), propertiesToFetch)
        .findUnique();
  }

  @Override
  public int countBy(LogEntryCriteria criteria) {
    return findQuery(criteria).findCount();
  }

  private ExpressionList<LogEntry> findQuery(LogEntryCriteria criteria) {
    ExpressionList<LogEntry> query = find.where();

    if (criteria.getIds() != null) {
      query.idIn(criteria.getIds());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.disjunction().ilike("before", "%" + criteria.getSearch() + "%")
          .ilike("after", "%" + criteria.getSearch() + "%").endJunction();
    }

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (criteria.getProjectId() != null) {
      query.eq("project.id", criteria.getProjectId());
    }

    return query;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void preSave(LogEntry t, boolean update) {
    if (t.user == null) {
      t.user = User.loggedInUser();
    }
  }

  @Override
  protected void prePersist(LogEntry t, boolean update) {
    if (t.project == null) {
      UUID projectId = ContextKey.ProjectId.get();
      if (projectId != null) {
        t.project = new Project().withId(projectId);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(Collection<LogEntry> t) {
    t.forEach(e -> preSave(e, false));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(LogEntry t, boolean update) {
    if (t.user != null && t.project != null) {
      notificationActor.tell(new Notification(t), null);
    }
  }
}
