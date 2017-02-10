package dto;

import java.util.function.Function;

import com.avaje.ebean.PagedList;

/**
 * @author resamsel
 * @version 10 Feb 2017
 */
public class ProjectsPaged extends DtoPagedList<models.Project, Project> {
  /**
   * @param delegate
   * @param mapper
   * 
   */
  public ProjectsPaged(PagedList<models.Project> delegate,
      Function<models.Project, Project> mapper) {
    super(delegate, mapper);
  }
}
