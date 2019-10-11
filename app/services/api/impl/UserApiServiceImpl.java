package services.api.impl;

import com.avaje.ebean.PagedList;
import com.fasterxml.jackson.databind.JsonNode;
import criterias.LogEntryCriteria;
import criterias.UserCriteria;
import dto.DtoPagedList;
import dto.NotFoundException;
import mappers.AggregateMapper;
import mappers.UserMapper;
import models.Scope;
import models.User;
import play.libs.Json;
import services.LogEntryService;
import services.PermissionService;
import services.UserService;
import services.api.UserApiService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * @author resamsel
 * @version 29 Jan 2017
 */
@Singleton
public class UserApiServiceImpl extends
    AbstractApiService<User, UUID, UserCriteria, UserService, dto.User>
    implements UserApiService {

  private final LogEntryService logEntryService;

  @Inject
  protected UserApiServiceImpl(
      UserService userService,
      PermissionService permissionService,
      LogEntryService logEntryService) {
    super(userService, dto.User.class, UserMapper::toDto,
        new Scope[]{Scope.UserRead},
        new Scope[]{Scope.UserWrite},
        permissionService);

    this.logEntryService = logEntryService;
  }

  @Override
  public dto.User byUsername(String username, String... propertiesToFetch) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    return Optional.ofNullable(service.byUsername(username, propertiesToFetch))
        .map(dtoMapper)
        .orElseThrow(() -> new NotFoundException(dto.User.class.getSimpleName(), username));
  }

  @Override
  public PagedList<dto.Aggregate> activity(UUID id) {
    permissionService.checkPermissionAll("Access token not allowed", readScopes);

    return new DtoPagedList<>(
        logEntryService.getAggregates(new LogEntryCriteria().withUserId(id)),
        AggregateMapper::toDto);
  }

  @Override
  public dto.User me() {
    return dtoMapper.apply(User.loggedInUser());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected User toModel(JsonNode json) {
    dto.User dto = Json.fromJson(json, dto.User.class);

    return UserMapper.toModel(dto, service.byId(dto.id));
  }
}
