package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import criterias.AbstractSearchCriteria;
import dto.Dto;
import dto.PermissionException;
import java.util.UUID;
import models.ProjectRole;
import models.User;
import play.Configuration;
import play.cache.CacheApi;
import play.inject.Injector;
import services.api.ApiService;
import utils.PermissionUtils;

public abstract class AbstractApi<DTO extends Dto, ID, CRITERIA extends AbstractSearchCriteria<CRITERIA>>
    extends AbstractBaseApi {
  protected final ApiService<DTO, ID, CRITERIA> api;
  protected final Configuration configuration;

  protected AbstractApi(Injector injector, CacheApi cache, PlayAuthenticate auth,
      ApiService<DTO, ID, CRITERIA> api) {
    super(injector, cache, auth);

    this.configuration = injector.instanceOf(Configuration.class);
    this.api = api;
  }

  protected void checkProjectRole(UUID projectId, User user, ProjectRole... roles) {
    if (!permissionService.hasPermissionAny(projectId, user, roles))
      throw new PermissionException("User not allowed in project");
  }
}
