package criterias;

import forms.SearchForm;
import java.util.UUID;
import play.mvc.Http.Request;

/**
 * @author resamsel
 * @version 26 Sep 2016
 */
public class ProjectCriteria extends AbstractProjectSearchCriteria<ProjectCriteria> {

  private UUID ownerId;

  private UUID memberId;

  public ProjectCriteria() {
    super("project");
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  private void setOwnerId(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public ProjectCriteria withOwnerId(UUID ownerId) {
    setOwnerId(ownerId);
    return this;
  }

  public UUID getMemberId() {
    return memberId;
  }

  private void setMemberId(UUID memberId) {
    this.memberId = memberId;
  }

  public ProjectCriteria withMemberId(UUID memberId) {
    setMemberId(memberId);
    return this;
  }

  @Override
  protected String getCacheKeyParticle() {
    return String.format("%s:%s", ownerId, memberId);
  }

  public static ProjectCriteria from(SearchForm form) {
    return new ProjectCriteria().with(form);
  }

  public static ProjectCriteria from(Request request) {
    return new ProjectCriteria().with(request);
  }
}
