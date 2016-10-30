package dto;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.Project;
import models.ProjectRole;
import models.User;

public class ProjectUser extends Dto {
  public UUID projectId;

  public String projectName;

  public UUID userId;

  public String userName;

  public ProjectRole role;

  @JsonIgnore
  public DateTime whenCreated;

  @JsonIgnore
  public DateTime whenUpdated;

  private ProjectUser(models.ProjectUser in) {
    this.projectId = in.project.id;
    this.projectName = in.project.name;
    this.userId = in.user.id;
    this.userName = in.user.username;
    this.role = in.role;
    this.whenCreated = in.whenCreated;
    this.whenUpdated = in.whenUpdated;
  }

  public models.ProjectUser toModel() {
    models.ProjectUser out = new models.ProjectUser();

    out.project = new Project().withId(projectId);
    out.user = new User().withId(userId);
    out.role = role;
    out.whenCreated = whenCreated;
    out.whenUpdated = whenUpdated;

    return out;
  }

  public static ProjectUser from(models.ProjectUser in) {
    return new ProjectUser(in);
  }
}
