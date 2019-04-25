package controllers;

import actions.ApiAction;
import com.feth.play.module.pa.PlayAuthenticate;
import criterias.AccessTokenCriteria;
import dto.AccessToken;
import dto.errors.ConstraintViolationError;
import dto.errors.GenericError;
import dto.errors.NotFoundError;
import dto.errors.PermissionError;
import io.swagger.annotations.*;
import play.inject.Injector;
import play.mvc.Result;
import play.mvc.With;
import services.CacheService;
import services.api.AccessTokenApiService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@io.swagger.annotations.Api(value = "Access Tokens", produces = "application/json")
@With(ApiAction.class)
public class AccessTokensApi extends AbstractApi<AccessToken, Long, AccessTokenCriteria, AccessTokenApiService> {

  private static final String TYPE = "dto.User";

  private static final String FIND = "Find access tokens";
  private static final String FIND_RESPONSE = "Found access tokens";
  private static final String GET = "Get access token by ID";
  private static final String GET_RESPONSE = "Found access token";
  private static final String CREATE = "Create access token";
  private static final String CREATE_RESPONSE = "Created access token";
  private static final String CREATE_REQUEST = "The access token to create";
  private static final String UPDATE = "Update access token";
  private static final String UPDATE_RESPONSE = "Updated access token";
  private static final String UPDATE_REQUEST = "The access token to update";
  private static final String DELETE = "Delete access token";
  private static final String DELETE_RESPONSE = "Deleted access token";

  private static final String SEARCH = "Part of the name of the access token";
  private static final String NOT_FOUND_ERROR = "Access token not found";

  @Inject
  public AccessTokensApi(Injector injector, CacheService cache, PlayAuthenticate auth, AccessTokenApiService accessTokenApiService) {
    super(injector, cache, auth, accessTokenApiService);
  }

  @ApiOperation(value = FIND, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.UsersPaged.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
          paramType = "query"),
      @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
      @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query")})
  public CompletionStage<Result> find() {
    return toJsons(() -> api.find(AccessTokenCriteria.from(request())));
  }

  @ApiOperation(value = GET, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = @AuthorizationScope(scope = USER_READ, description = USER_READ_DESCRIPTION)))
  @ApiResponses({@ApiResponse(code = 200, message = GET_RESPONSE, response = dto.User.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> get(@ApiParam(value = ACCESS_TOKEN_ID) long id) {
    return toJson(() -> api.get(id));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = CREATE, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = CREATE_RESPONSE, response = dto.User.class),
      @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = CREATE_REQUEST, required = true, dataType = TYPE,
          paramType = "body"),
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> create() {
    return toJson(() -> api.create(request().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = UPDATE, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = UPDATE_RESPONSE, response = dto.User.class),
      @ApiResponse(code = 400, message = INPUT_ERROR, response = ConstraintViolationError.class),
      @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "body", value = UPDATE_REQUEST, required = true, dataType = TYPE,
          paramType = "body"),
      @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
          dataType = "string", paramType = "query")})
  public CompletionStage<Result> update() {
    return toJson(() -> api.update(request().body().asJson()));
  }

  /**
   * {@inheritDoc}
   */
  @ApiOperation(value = DELETE, authorizations = @Authorization(value = AUTHORIZATION,
      scopes = {@AuthorizationScope(scope = USER_WRITE, description = USER_WRITE_DESCRIPTION)}))
  @ApiResponses({@ApiResponse(code = 200, message = DELETE_RESPONSE, response = dto.User.class),
      @ApiResponse(code = 403, message = INPUT_ERROR, response = PermissionError.class),
      @ApiResponse(code = 404, message = NOT_FOUND_ERROR, response = NotFoundError.class),
      @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
      required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> delete(@ApiParam(value = PROJECT_ID) long id) {
    return toJson(() -> api.delete(id));
  }
}