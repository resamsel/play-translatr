package utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import criterias.ProjectUserCriteria;
import dto.PermissionException;
import models.AccessToken;
import models.Project;
import models.ProjectRole;
import models.ProjectUser;
import models.Scope;
import models.User;

/**
 * @author resamsel
 * @version 21 Oct 2016
 */
public class PermissionUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionUtils.class);

  public static boolean hasPermissionAll(Scope... scopes) {
    return hasPermissionAll(ContextKey.AccessToken.get(), scopes);
  }

  public static boolean hasPermissionAll(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null)
      return User.loggedInUser() != null;

    // TODO: allow admin scopes also
    // !PermissionUtils.hasPermissionAny(Scope.ProjectRead, Scope.ProjectAdmin)
    // || !PermissionUtils.hasPermissionAny(Scope.LocaleRead, Scope.LocaleAdmin))

    return accessToken.getScopeList().containsAll(Arrays.asList(scopes));
  }

  public static boolean hasPermissionAny(Scope... scopes) {
    return hasPermissionAny(ContextKey.AccessToken.get(), scopes);
  }

  public static boolean hasPermissionAny(AccessToken accessToken, Scope... scopes) {
    LOGGER.debug("Scopes of access token: {}, needed: {}",
        accessToken != null ? accessToken.getScopeList() : "-", scopes);

    if (accessToken == null)
      return false;

    List<Scope> scopeList = accessToken.getScopeList();
    scopeList.retainAll(Arrays.asList(scopes));

    return !scopeList.isEmpty();
  }

  /**
   * @param project
   * @param roles
   * @return
   */
  public static boolean hasPermissionAny(Project project, ProjectRole... roles) {
    return hasPermissionAny(project.id, User.loggedInUser(), roles);
  }

  /**
   * @param projectId
   * @param roles
   * @return
   */
  public static boolean hasPermissionAny(UUID projectId, ProjectRole... roles) {
    return hasPermissionAny(projectId, User.loggedInUser(), roles);
  }

  /**
   * @param project
   * @param loggedInUser
   * @param roles
   * @return
   */
  public static boolean hasPermissionAny(UUID projectId, User loggedInUser, ProjectRole... roles) {
    return hasPermissionAny(projectId, User.loggedInUser(), Arrays.asList(roles));
  }

  /**
   * @param project
   * @param user
   * @param roles
   * @return
   */
  public static boolean hasPermissionAny(UUID projectId, User user, Collection<ProjectRole> roles) {
    LOGGER.debug("Members needed: {}", roles);

    for (ProjectUser member : ProjectUser.findBy(new ProjectUserCriteria().withProjectId(projectId))
        .getList())
      if (user.id.equals(member.user.id) && roles.contains(member.role))
        return true;

    return false;
  }

  /**
   * @param errorMessage
   * @param scopes
   */
  public static void checkPermissionAll(String errorMessage, Scope... scopes) {
    if (!hasPermissionAll(scopes))
      throw new PermissionException(errorMessage, scopes);
  }
}