package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.feth.play.module.pa.PlayAuthenticate;

import actions.ContextAction;
import criterias.ProjectCriteria;
import dto.SearchResponse;
import dto.Suggestion;
import forms.ProjectForm;
import forms.SearchForm;
import models.Project;
import models.Suggestable;
import models.Suggestable.Data;
import play.Configuration;
import play.cache.CacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import services.UserService;
import utils.Template;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 26 Sep 2016
 */
@With(ContextAction.class)
public class Dashboards extends AbstractController
{
	private final FormFactory formFactory;

	private final Configuration configuration;

	private final PlayAuthenticate auth;

	private final UserService userService;

	/**
	 * 
	 */
	@Inject
	public Dashboards(CacheApi cache, FormFactory formFactory, Configuration configuration, PlayAuthenticate auth,
				UserService userService)
	{
		super(cache);

		this.formFactory = formFactory;
		this.configuration = configuration;
		this.auth = auth;
		this.userService = userService;
	}

	public Result dashboard()
	{
		return ok(
			views.html.dashboard.render(
				Template.create(auth, userService),
				Project.all(),
				SearchForm.bindFromRequest(formFactory, configuration),
				ProjectForm.form(formFactory)));
	}

	public Result search()
	{
		Form<SearchForm> form = SearchForm.bindFromRequest(formFactory, configuration);
		SearchForm search = form.get();

		List<Suggestable> suggestions = new ArrayList<>();

		Collection<? extends Suggestable> projects = Project.findBy(ProjectCriteria.from(search));
		if(!projects.isEmpty())
			suggestions.addAll(projects);
		else
			suggestions.add(
				Suggestable.DefaultSuggestable.from(
					ctx().messages().at("project.create", search.search),
					Data.from(
						Project.class,
						null,
						"+++",
						controllers.routes.Projects.createImmediately(search.search).url())));

		return ok(Json.toJson(SearchResponse.from(Suggestion.from(suggestions))));
	}
}
