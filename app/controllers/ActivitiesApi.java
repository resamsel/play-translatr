package controllers;

import criterias.LogEntryCriteria;
import dto.Activity;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import play.inject.Injector;
import play.mvc.Http;
import play.mvc.Result;
import services.AuthProvider;
import services.api.ActivityApiService;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@OpenAPIDefinition(info = @Info(title = "Activities", version = "1.0"))
public class ActivitiesApi extends AbstractApi<Activity, UUID, LogEntryCriteria, ActivityApiService> {
  private static final String FIND = "Find activites";
  private static final String FIND_RESPONSE = "Found activities";
  private static final String ACTIVITY = "Find aggregated activites";
  private static final String ACTIVITY_RESPONSE = "Found aggregated activities";

  public static final String PARAM_TYPES = "types";

  private static final String SEARCH = "Part of the contents of the activity";
  private static final String TYPES = "List of types the activities need to match";

  @Inject
  protected ActivitiesApi(Injector injector, AuthProvider authProvider,
                          ActivityApiService activityApiService) {
    super(injector, authProvider, activityApiService);
  }

//  @Operation(summary = FIND,parameters = {
//          @Parameter(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//          dataType = "string", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
//                  paramType = "query"),
//          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_TYPES, value = TYPES, dataType = "string",
//                  paramType = "query")
//  }
//          authorizations = @Authorization(value = AUTHORIZATION,
//          scopes = @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)))
//  @ApiResponses({@ApiResponse(code = 200, message = FIND_RESPONSE, response = dto.ActivitiesPaged.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({
//          @ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN, required = true,
//                  dataType = "string", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_SEARCH, value = SEARCH, dataType = "string",
//                  paramType = "query"),
//          @ApiImplicitParam(name = PARAM_OFFSET, value = OFFSET, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_LIMIT, value = LIMIT, dataType = "int", paramType = "query"),
//          @ApiImplicitParam(name = PARAM_TYPES, value = TYPES, dataType = "string",
//                  paramType = "query")})
  public CompletionStage<Result> find(Http.Request request) {
    return toJsons(() -> api.find(LogEntryCriteria.from(request)));
  }

  /**
   * {@inheritDoc}
   */
//  @ApiOperation(value = ACTIVITY, authorizations = @Authorization(value = AUTHORIZATION,
//          scopes = @AuthorizationScope(scope = PROJECT_READ, description = PROJECT_READ_DESCRIPTION)))
//  @ApiResponses({@ApiResponse(code = 200, message = ACTIVITY_RESPONSE, response = dto.AggregatesPaged.class),
//          @ApiResponse(code = 403, message = PERMISSION_ERROR, response = PermissionError.class),
//          @ApiResponse(code = 500, message = INTERNAL_SERVER_ERROR, response = GenericError.class)})
//  @ApiImplicitParams({@ApiImplicitParam(name = PARAM_ACCESS_TOKEN, value = ACCESS_TOKEN,
//          required = true, dataType = "string", paramType = "query")})
  public CompletionStage<Result> activity(Http.Request request) {
    return toJsons(() -> api.getAggregates(LogEntryCriteria.from(request)));
  }
}
