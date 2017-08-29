package services;

import static assertions.LinkedAccountAssert.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static utils.LinkedAccountRepositoryMock.createLinkedAccount;

import criterias.HasNextPagedList;
import criterias.LinkedAccountCriteria;
import java.util.concurrent.ThreadLocalRandom;
import javax.validation.Validator;
import models.LinkedAccount;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repositories.LinkedAccountRepository;
import services.impl.CacheServiceImpl;
import services.impl.LinkedAccountServiceImpl;
import utils.CacheApiMock;
import utils.UserRepositoryMock;

public class LinkedAccountServiceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LinkedAccountServiceTest.class);

  private LinkedAccountRepository linkedAccountRepository;
  private LinkedAccountService linkedAccountService;
  private CacheService cacheService;
  private User johnSmith;

  @Test
  public void testById() {
    // mock linkedAccount
    LinkedAccount linkedAccount = createLinkedAccount(ThreadLocalRandom.current().nextLong(),
        johnSmith,
        "google");
    linkedAccountRepository.create(linkedAccount);

    // This invocation should feed the cache
    assertThat(cacheService.keys().keySet()).doesNotContain("linkedAccount:id:" + linkedAccount.id);
    assertThat(linkedAccountService.byId(linkedAccount.id))
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).byId(eq(linkedAccount.id));

    // This invocation should use the cache, not the repository
    assertThat(cacheService.keys().keySet()).contains("linkedAccount:id:" + linkedAccount.id);
    assertThat(linkedAccountService.byId(linkedAccount.id))
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).byId(eq(linkedAccount.id));

    // This should trigger cache invalidation
    linkedAccountService.update(createLinkedAccount(linkedAccount, "facebook"));

    assertThat(cacheService.keys().keySet()).contains("linkedAccount:id:" + linkedAccount.id);
    assertThat(linkedAccountService.byId(linkedAccount.id)).providerKeyIsEqualTo("facebook");
    verify(linkedAccountRepository, times(1)).byId(eq(linkedAccount.id));
  }

  @Test
  public void testFindBy() {
    // mock linkedAccount
    LinkedAccount linkedAccount = createLinkedAccount(ThreadLocalRandom.current().nextLong(),
        johnSmith,
        "google");
    linkedAccountRepository.create(linkedAccount);

    // This invocation should feed the cache
    LinkedAccountCriteria criteria = new LinkedAccountCriteria().withUserId(linkedAccount.user.id);
    assertThat(linkedAccountService.findBy(criteria).getList().get(0))
        .as("uncached")
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).findBy(eq(criteria));
    // This invocation should use the cache, not the repository
    assertThat(linkedAccountService.findBy(criteria).getList().get(0))
        .as("cached")
        .providerKeyIsEqualTo(linkedAccount.providerKey);
    verify(linkedAccountRepository, times(1)).findBy(eq(criteria));

    // This should trigger cache invalidation
    linkedAccountService.update(createLinkedAccount(linkedAccount, "facebook"));

    assertThat(linkedAccountService.findBy(criteria).getList().get(0))
        .as("uncached (invalidated)")
        .providerKeyIsEqualTo("facebook");
    verify(linkedAccountRepository, times(2)).findBy(eq(criteria));
  }

  @Before
  public void before() {
    linkedAccountRepository = mock(LinkedAccountRepository.class,
        withSettings().invocationListeners(i -> LOGGER.debug("{}", i.getInvocation())));
    cacheService = new CacheServiceImpl(new CacheApiMock());
    linkedAccountService = new LinkedAccountServiceImpl(
        mock(Validator.class),
        cacheService,
        linkedAccountRepository,
        mock(LogEntryService.class)
    );

    johnSmith = UserRepositoryMock.byUsername("johnsmith");

    when(linkedAccountRepository.create(any())).then(this::persist);
    when(linkedAccountRepository.update(any())).then(this::persist);
  }

  private LinkedAccount persist(InvocationOnMock a) {
    LinkedAccount t = a.getArgument(0);
    when(linkedAccountRepository.byId(eq(t.id), any())).thenReturn(t);
    when(linkedAccountRepository.findBy(any())).thenReturn(HasNextPagedList.create(t));
    return t;
  }
}
