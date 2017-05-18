package models;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.FormatUtils.formatLocale;
import static utils.Stopwatch.log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;

import controllers.routes;
import criterias.HasNextPagedList;
import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import play.libs.Json;
import play.mvc.Http.Context;
import utils.QueryUtils;
import validators.LocaleNameUniqueChecker;
import validators.NameUnique;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
@NameUnique(checker = LocaleNameUniqueChecker.class)
public class Locale implements Model<Locale, UUID>, Suggestable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Locale.class);

  public static final int NAME_LENGTH = 15;

  private static final Map<String, List<String>> FETCH_MAP = ImmutableMap.of("project",
      Arrays.asList("project"), "messages", Arrays.asList("messages", "messages.key"));

  @Id
  @GeneratedValue
  public UUID id;

  @Version
  public Long version;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne(optional = false)
  @NotNull
  public Project project;

  @Column(nullable = false, length = NAME_LENGTH)
  @NotNull
  public String name;

  @JsonIgnore
  @OneToMany
  public List<Message> messages;

  public Locale() {}

  public Locale(Project project, String name) {
    this.project = project;
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public String value() {
    Context ctx = Context.current();
    return ctx.messages().at("locale.autocomplete", formatLocale(ctx.lang().locale(), this));
  }

  @Override
  public Data data() {
    return Data.from(Locale.class, id, formatLocale(Context.current().lang().locale(), this),
        routes.Locales.locale(id).absoluteURL(Context.current().request()));
  }

  private static final Find<UUID, Locale> find = new Find<UUID, Locale>() {};

  private static final List<String> PROPERTIES_TO_FETCH = Arrays.asList("project");

  /**
   * @param fromString
   * @return
   */
  public static Locale byId(UUID id, String... fetches) {
    HashSet<String> propertiesToFetch = new HashSet<>(PROPERTIES_TO_FETCH);
    if (fetches.length > 0)
      propertiesToFetch.addAll(Arrays.asList(fetches));

    return QueryUtils.fetch(find.setId(id), propertiesToFetch).findUnique();
  }

  /**
   * @param project
   * @return
   */
  public static List<Locale> byProject(Project project) {
    return find.fetch("project").where().eq("project", project).findList();
  }

  /**
   * @param project
   * @param name
   * @return
   */
  public static Locale byProjectAndName(Project project, String name) {
    if (project == null)
      return null;

    return byProjectAndName(project.id, name);
  }

  /**
   * @param projectId
   * @param name
   * @return
   */
  public static Locale byProjectAndName(UUID projectId, String name) {
    return find.fetch("project").where().eq("project.id", projectId).eq("name", name).findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static PagedList<Locale> findBy(LocaleCriteria criteria) {
    Query<Locale> q = find.fetch("project").alias("k").setDisableLazyLoading(true);

    if (StringUtils.isEmpty(criteria.getMessagesKeyName()) && !criteria.getFetches().isEmpty())
      q = QueryUtils.fetch(q, criteria.getFetches(), FETCH_MAP);

    ExpressionList<Locale> query = q.where();

    if (criteria.getProjectId() != null)
      query.eq("project.id", criteria.getProjectId());

    if (criteria.getLocaleName() != null)
      query.eq("name", criteria.getLocaleName());

    if (criteria.getSearch() != null)
      query.ilike("name", "%" + criteria.getSearch() + "%");

    if (criteria.getOrder() != null)
      query.setOrderBy(criteria.getOrder());

    criteria.paged(query);

    return log(() -> fetch(HasNextPagedList.create(query), criteria), LOGGER, "findBy");
  }

  private static HasNextPagedList<Locale> fetch(HasNextPagedList<Locale> paged,
      LocaleCriteria criteria) {
    if (StringUtils.isNotEmpty(criteria.getMessagesKeyName())
        && criteria.getFetches().contains("messages")) {
      // Retrieve messages that match the given keyName and locales retrieved
      Map<UUID, Message> messages = Message
          .findBy(new MessageCriteria().withKeyName(criteria.getMessagesKeyName())
              .withLocaleIds(paged.getList().stream().map(l -> l.id).collect(toList())))
          .getList().stream().collect(toMap(m -> m.locale.id, m -> m));

      for (Locale locale : paged.getList())
        if (messages.containsKey(locale.id))
          locale.messages = Arrays.asList(messages.get(locale.id));
    }

    return paged;
  }

  public static List<Locale> last(Project project, int limit) {
    return log(() -> find.fetch("project").where().eq("project", project).order("whenUpdated desc")
        .setMaxRows(limit).findList(), LOGGER, "last(%d)", limit);
  }

  /**
   * @param model
   */
  @Override
  public Locale updateFrom(Locale in) {
    project = in.project;
    name = in.name;

    return this;
  }

  /**
   * @param localeId
   * @param fetches
   * @return
   */
  public static String getCacheKey(UUID localeId, String... fetches) {
    if (localeId == null)
      return null;

    if (fetches.length > 0)
      return String.format("locale:%s:%s", localeId, StringUtils.join(fetches, ":"));

    return String.format("locale:%s", localeId);
  }

  @Override
  public String toString() {
    return String.format("{\"project\": %s, \"name\": %s}", project, Json.toJson(name));
  }
}
