package controllers;

import static utils.Stopwatch.log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.PagedList;
import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import criterias.LogEntryCriteria;
import criterias.ProjectCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.ActivitySearchForm;
import forms.ProjectForm;
import forms.SearchForm;
import models.LogEntry;
import models.Project;
import models.Suggestable;
import models.Suggestable.Data;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.inject.Injector;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.ProjectService;
import utils.FormUtils;

/**
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
public class Dashboards extends AbstractController {
  private static final Logger LOGGER = LoggerFactory.getLogger(Dashboards.class);

  private final FormFactory formFactory;

  private final Configuration configuration;

  private final ProjectService projectService;

  /**
   * 
   */
  @Inject
  public Dashboards(Injector injector, CacheApi cache, FormFactory formFactory,
      Configuration configuration, PlayAuthenticate auth, ProjectService projectService) {
    super(injector, cache, auth);

    this.formFactory = formFactory;
    this.configuration = configuration;
    this.projectService = projectService;
  }

  @SubjectPresent(forceBeforeAuthCheck = true)
  public Result dashboard() {
    return loggedInUser(user -> {
      Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
      SearchForm search = form.get();
      if (search.order == null)
        search.order = "name";

      PagedList<Project> projects =
          projectService.findBy(ProjectCriteria.from(search).withMemberId(User.loggedInUserId()));

      return log(() -> ok(views.html.dashboards.dashboard.render(createTemplate(),
          projects.getList(), FormUtils.Search.bindFromRequest(formFactory, configuration),
          ProjectForm.form(formFactory))), LOGGER, "Rendering dashboard");
    });
  }

  @SubjectPresent
  public Result activity() {
    return loggedInUser(user -> {
      Form<ActivitySearchForm> form =
          FormUtils.ActivitySearch.bindFromRequest(formFactory, configuration);
      ActivitySearchForm search = form.get();

      PagedList<LogEntry> activities = logEntryService.findBy(
          LogEntryCriteria.from(search).withProjectUserId(user.id).withOrder("whenCreated desc"));

      search.pager(activities);

      return ok(views.html.dashboards.activity.render(createTemplate(), activities, form));
    });
  }

  @SubjectPresent
  public Result search() {
    Form<SearchForm> form = FormUtils.Search.bindFromRequest(formFactory, configuration);
    SearchForm search = form.get();

    List<Suggestable> suggestions = new ArrayList<>();

    PagedList<? extends Suggestable> projects = projectService.findBy(ProjectCriteria.from(search));
    if (!projects.getList().isEmpty())
      suggestions.addAll(projects.getList());
    else
      suggestions.add(Suggestable.DefaultSuggestable
          .from(ctx().messages().at("project.create", search.search), Data.from(Project.class, null,
              "+++", controllers.routes.Projects.createImmediately(search.search).url())));

    return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
  }
}
