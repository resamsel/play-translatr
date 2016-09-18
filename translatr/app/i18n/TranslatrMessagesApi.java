package i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import play.api.Configuration;
import play.api.Environment;
import play.api.i18n.DefaultMessagesApi;
import play.api.i18n.Lang;
import play.api.i18n.Langs;
import play.mvc.Http.Context;
import scala.collection.Seq;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 31 Aug 2016
 */
public class TranslatrMessagesApi extends DefaultMessagesApi
{
	private static final String KEY = "undefined";

	/**
	 * @param environment
	 * @param configuration
	 * @param langs
	 */
	@Inject
	public TranslatrMessagesApi(Environment environment, Configuration configuration, Langs langs)
	{
		super(environment, configuration, langs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String noMatch(String key, Seq<Object> args, Lang lang)
	{
		addUndefined(key);
		return super.noMatch(key, args, lang);
	}

	@SuppressWarnings("unchecked")
	private static void addUndefined(String key)
	{
		Map<String, Object> args = Context.current().args;
		if(!args.containsKey(KEY))
			args.put(KEY, new ArrayList<>());
		((List<String>)args.get(KEY)).add(key);
	}
}