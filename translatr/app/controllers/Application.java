package controllers;

import static utils.FormatUtils.formatLocale;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import actions.ContextAction;
import commands.Command;
import commands.RevertDeleteKeyCommand;
import commands.RevertDeleteLocaleCommand;
import exporters.Exporter;
import exporters.PlayExporter;
import importers.Importer;
import importers.PlayImporter;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import play.cache.CacheApi;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.With;
import play.routing.JavaScriptReverseRouter;
import scala.collection.JavaConversions;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
@With(ContextAction.class)
public class Application extends Controller
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private static final String COMMAND_FORMAT = "command:%s";

	private final FormFactory formFactory;

	private final CacheApi cache;

	@Inject
	public Application(FormFactory formFactory, CacheApi cache)
	{
		this.formFactory = formFactory;
		this.cache = cache;
	}

	private void select(Project project)
	{
		// session("projectId", project.id.toString());
	}

	private void select(Locale locale)
	{
		// session("localeId", locale.id.toString());
		// session("localeName", locale.name);
	}

	public Result index()
	{
		// return ok(views.html.index.render());
		return redirect(routes.Application.dashboard());
	}

	public Result dashboard()
	{
		return ok(views.html.dashboard.render(Project.all()));
	}

	public Result project(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		return ok(views.html.project.render(project));
	}

	public Result projectCreate()
	{
		Project project = formFactory.form(Project.class).bindFromRequest().get();

		LOGGER.debug("Project: {}", Json.toJson(project));

		Ebean.save(project);

		return redirect(routes.Application.project(project.id));
	}

	public Result projectLocales(UUID id)
	{
		Project project = Project.byId(id);

		if(project == null)
			return redirect(routes.Application.index());

		select(project);

		java.util.Locale locale = ctx().lang().locale();
		Collections.sort(project.locales, (a, b) -> formatLocale(locale, a).compareTo(formatLocale(locale, b)));

		return ok(views.html.projectLocales.render(project, project.locales));
	}

	public Result locale(UUID id)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		Project project = Project.byId(locale.project.id);

		select(project);

		Collections.sort(project.keys, (a, b) -> a.name.compareTo(b.name));

		List<Locale> locales = Locale.byProject(project);

		return ok(views.html.locale.render(project, locale, locales));
	}

	public Result localeImport(UUID id)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale.project);

		if("POST".equals(request().method()))
		{
			importLocale(locale, request());

			return redirect(routes.Application.locale(id));
		}
		else
		{
			return ok(views.html.localeImport.render(locale.project, locale));
		}
	}

	public Result localeExport(UUID id)
	{
		Locale locale = Locale.byId(id);

		if(locale == null)
			return redirect(routes.Application.index());

		Exporter exporter = new PlayExporter();

		exporter.addHeaders(response(), locale);

		return ok(new ByteArrayInputStream(exporter.apply(locale)));
	}

	public Result localeCreate(UUID projectId)
	{
		Project project = Project.byId(projectId);

		if(project == null)
			return redirect(routes.Application.index());

		Locale locale = formFactory.form(Locale.class).bindFromRequest().get();

		locale.project = project;

		LOGGER.debug("Locale: {}", Json.toJson(locale));

		Ebean.save(locale);

		importLocale(locale, request());

		return redirect(routes.Application.locale(locale.id));
	}

	public Result localeEdit(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		if("POST".equals(request().method()))
		{
			Locale changed = formFactory.form(Locale.class).bindFromRequest().get();

			locale.name = changed.name;

			Ebean.save(locale);

			return redirect(routes.Application.projectLocales(locale.project.id));
		}

		return ok(views.html.localeEdit.render(locale));
	}

	public Result localeRemove(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		for(Message message : locale.messages)
			Ebean.delete(message);
		Ebean.delete(locale);

		UUID commandId = UUID.randomUUID();
		cache.set(String.format(COMMAND_FORMAT, commandId), new RevertDeleteLocaleCommand(locale), 120);

		return redirect(
			routes.Application.projectLocales(locale.project.id).withFragment(String.format("command=%s", commandId)));
	}

	public Result localeTranslate(UUID localeId)
	{
		Locale locale = Locale.byId(localeId);

		if(locale == null)
			return redirect(routes.Application.index());

		select(locale);

		String referer = request().getHeader("Referer");

		if(referer == null)
			return redirect(routes.Application.locale(localeId));

		return redirect(referer);
	}

	public Result keyCreate(UUID localeId)
	{
		Key key = formFactory.form(Key.class).bindFromRequest().get();

		Locale locale = Locale.byId(localeId);
		key.project = locale.project;

		LOGGER.debug("Key: {}", Json.toJson(key));

		Ebean.save(key);

		return redirect(routes.Application.locale(locale.id).withFragment("#key=" + key.name));
	}

	public Result keyRemove(UUID keyId, UUID localeId)
	{
		Key key = Key.byId(keyId);

		LOGGER.debug("Key: {}", Json.toJson(key));

		if(key == null)
			return redirect(routes.Application.index());

		for(Message message : key.messages)
			Ebean.delete(message);
		Ebean.delete(key);

		LOGGER.debug("Deleted key: {}", Json.toJson(key));

		UUID commandId = UUID.randomUUID();
		cache.set(String.format(COMMAND_FORMAT, commandId), new RevertDeleteKeyCommand(key), 120);

		Locale locale = Locale.byId(localeId);
		if(locale != null)
			return redirect(routes.Application.locale(locale.id).withFragment(String.format("#command=%s", commandId)));

		LOGGER.debug("Go to project: {}", Json.toJson(key));

		return redirect(routes.Application.project(key.project.id));
	}

	public Result load()
	{
		Project project = Project.byName("Internal");
		if(project == null)
		{
			project = new Project("Internal");
			Ebean.save(project);
		}

		for(Entry<String, scala.collection.immutable.Map<String, String>> bundle : JavaConversions
			.mapAsJavaMap(ctx().messages().messagesApi().scalaApi().messages())
			.entrySet())
		{
			LOGGER.debug("Key: {}", bundle.getKey());
			if("default.play".equals(bundle.getKey()))
				break;
			Locale locale = Locale.byProjectAndName(project, bundle.getKey());
			if(locale == null)
			{
				locale = new Locale(project, bundle.getKey());
				Ebean.save(locale);
			}
			for(Entry<String, String> msg : JavaConversions.mapAsJavaMap(bundle.getValue()).entrySet())
			{
				Key key = Key.byProjectAndName(project, msg.getKey());
				if(key == null)
				{
					key = new Key(project, msg.getKey());
					Ebean.save(key);
				}
				Message message = Message.byKeyAndLocale(key, locale);
				if(message == null)
				{
					message = new Message(locale, key, msg.getValue());
					Ebean.save(message);
				}
			}
		}

		return redirect(controllers.routes.Application.project(project.id));
	}

	public Result commandExecute(UUID id)
	{
		Command command = cache.get(String.format(COMMAND_FORMAT, id));

		if(command == null)
			notFound(Json.toJson("Command not found"));

		command.execute();

		return ok(Json.toJson("OK"));
	}

	public Result javascriptRoutes()
	{
		return ok(
			JavaScriptReverseRouter.create(
				"jsRoutes",
				routes.javascript.Application.locale(),
				routes.javascript.Application.keyRemove(),
				routes.javascript.Api.getMessage(),
				routes.javascript.Api.putMessage(),
				routes.javascript.Api.findMessages()));
	}

	/**
	 * @param locale
	 * @param request
	 * @return
	 */
	private static String importLocale(Locale locale, Request request)
	{
		MultipartFormData<File> body = request.body().asMultipartFormData();
		FilePart<File> messages = body.getFile("messages");

		if(messages == null)
			return null;

		Importer importer = new PlayImporter();

		try
		{
			importer.apply(messages.getFile(), locale);
		}
		catch(Exception e)
		{
			LOGGER.error("Error while importing messages", e);
		}

		return "OK";
	}
}
