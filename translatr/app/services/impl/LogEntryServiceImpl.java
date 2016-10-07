package services.impl;

import static utils.Stopwatch.log;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import criterias.LogEntryCriteria;
import models.Aggregate;
import models.LogEntry;
import models.Project;
import models.User;
import play.Configuration;
import play.mvc.Http.Context;
import services.LogEntryService;
import utils.ConfigKey;
import utils.SessionKey;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@Singleton
public class LogEntryServiceImpl extends AbstractModelService<LogEntry> implements LogEntryService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryServiceImpl.class);

	private static final String H2_COLUMN_MILLIS =
				"datediff('millisecond', timestamp '1970-01-01 00:00:00', parsedatetime(formatdatetime(when_created, 'yyyy-MM-dd HH:00:00'), 'yyyy-MM-dd HH:mm:ss'))*1000";

	private static final String POSTGRESQL_COLUMN_MILLIS = "extract(epoch from date_trunc('hour', when_created))*1000";

	/**
	 * 
	 */
	@Inject
	public LogEntryServiceImpl(Configuration configuration)
	{
		super(configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Aggregate> getStats(LogEntryCriteria criteria)
	{
		return log(
			() -> Ebean
				.find(Aggregate.class)
				.setRawSql(getStatsRawSql())
				.where()
				.eq("project_id", criteria.getProjectId())
				.findList(),
			LOGGER,
			"Retrieving log entry stats");
	}

	/**
	 * @return
	 */
	private RawSql getStatsRawSql()
	{
		String dbpName = Ebean.getDefaultServer().getPluginApi().getDatabasePlatform().getName();
		if("h2".equals(dbpName))
			return RawSqlBuilder
				.parse(
					String.format(
						"select %1$s as millis, content_type as key, count(*) as cnt from log_entry group by %1$s, content_type order by 1",
						H2_COLUMN_MILLIS))
				.columnMapping(H2_COLUMN_MILLIS, "millis")
				.columnMapping("content_type", "key")
				.columnMapping("count(*)", "value")
				.create();

		return RawSqlBuilder
			.parse(
				String.format(
					"select %1$s as millis, content_type as key, count(*) as cnt from log_entry group by 1, 2 order by 1",
					POSTGRESQL_COLUMN_MILLIS))
			.columnMapping(POSTGRESQL_COLUMN_MILLIS, "millis")
			.columnMapping("content_type", "key")
			.columnMapping("count(*)", "value")
			.create();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preSave(LogEntry t, boolean update)
	{
		if(t.user == null)
			t.user = loggedInUser();

		if(t.project == null)
		{
			if(Context.current().args.containsKey("projectId"))
			{
				t.project = new Project();
				t.project.id = (UUID)Context.current().args.get("projectId");
			}
			else
			{
				LOGGER.warn("Project has not been set and was not found in context");
			}
		}
	}
}
