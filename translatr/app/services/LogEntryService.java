package services;

import java.util.List;

import com.avaje.ebean.PagedList;
import com.google.inject.ImplementedBy;

import criterias.LogEntryCriteria;
import models.Aggregate;
import models.LogEntry;
import services.impl.LogEntryServiceImpl;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@ImplementedBy(LogEntryServiceImpl.class)
public interface LogEntryService extends ModelService<LogEntry> {
  /**
   * @param criteria
   * @return
   */
  List<Aggregate> getAggregates(LogEntryCriteria criteria);

  /**
   * Wraps {@link LogEntry#findBy(LogEntryCriteria)} with caching.
   * 
   * @param criteria
   * @return
   */
  List<LogEntry> findBy(LogEntryCriteria criteria);

  /**
   * Wraps {@link LogEntry#findBy(LogEntryCriteria)} with caching.
   * 
   * @param criteria
   * @return
   */
  PagedList<LogEntry> pagedBy(LogEntryCriteria criteria);
}
