package integration.controllers;

import static assertions.ResultAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static play.inject.Bindings.bind;
import static play.test.Helpers.route;
import static utils.TestFactory.requestAs;
import static utils.UserRepositoryMock.byUsername;

import controllers.LocalesApi;
import controllers.routes;
import criterias.HasNextPagedList;
import dto.NotFoundException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import models.AccessToken;
import models.Locale;
import models.Project;
import models.User;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.Binding;
import play.mvc.Result;
import services.AccessTokenService;
import services.ProjectUserService;
import services.api.LocaleApiService;
import utils.AccessTokenRepositoryMock;
import utils.LocaleRepositoryMock;
import utils.ProjectRepositoryMock;

public class LocalesApiTest extends ApiControllerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalesApiTest.class);

  private User johnSmith;
  private AccessToken accessToken;
  private Project project1;
  private Locale locale;

  @Test
  public void testDownloadByDenied() {
    assertAccessDenied(
        routes.LocalesApi.downloadBy("a", "b", "c", "java_properties"),
        "invalid",
        "download by denied"
    );
  }

  @Test
  public void testDownloadBy() {
    Result result = route(app,
        requestAs(routes.LocalesApi.downloadBy("a", "b", "c", "java_properties"), accessToken)
    );

    assertThat(result)
        .as("download by")
        .statusIsEqualTo(LocalesApi.NOT_FOUND);

    result = route(app,
        requestAs(routes.LocalesApi
                .downloadBy(johnSmith.username, project1.name, locale.name, "java_properties"),
            accessToken)
    );

    assertThat(result)
        .as("download by")
        .statusIsEqualTo(LocalesApi.OK)
        .contentIsEqualTo("", mat);
  }

  @Override
  protected Binding<?>[] createBindings() {
    johnSmith = byUsername("johnsmith");
    accessToken = AccessTokenRepositoryMock
        .createAccessToken(ThreadLocalRandom.current().nextLong(), johnSmith, "Access Token Test");
    project1 = ProjectRepositoryMock.byOwnerAndName(johnSmith.username, "project1");
    locale = LocaleRepositoryMock.createLocale(UUID.randomUUID(), project1, "de");

    AccessTokenService accessTokenService = mock(
        AccessTokenService.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );
    LocaleApiService localeApiService = mock(
        LocaleApiService.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );
    ProjectUserService projectUserService = mock(
        ProjectUserService.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation()))
    );

    when(accessTokenService.byKey(eq(accessToken.key))).thenReturn(accessToken);
    when(projectUserService.findBy(any()))
        .thenReturn(HasNextPagedList.create(project1.members));
    when(localeApiService
        .byOwnerAndProjectAndName(eq(johnSmith.username), eq(project1.name), eq(locale.name)))
        .thenReturn(dto.Locale.from(locale));
    when(localeApiService.byOwnerAndProjectAndName(eq("a"), eq("b"), eq("c")))
        .thenThrow(new NotFoundException(dto.Locale.class.getName(), "c"));
    when(localeApiService.download(eq(locale.id), eq("java_properties"), any()))
        .thenReturn("".getBytes());

    //noinspection unchecked
    return ArrayUtils.addAll(
        super.createBindings(),
        bind(AccessTokenService.class).toInstance(accessTokenService),
        bind(ProjectUserService.class).toInstance(projectUserService),
        bind(LocaleApiService.class).toInstance(localeApiService)
    );
  }
}
