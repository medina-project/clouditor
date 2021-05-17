package io.clouditor.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.clouditor.Engine;
import io.clouditor.credentials.AccountService;
import io.clouditor.credentials.CloudAccount;
import io.clouditor.credentials.MockCloudAccount;
import io.clouditor.data_access_layer.HibernatePersistence;
import io.clouditor.discovery.DiscoveryService;
import io.clouditor.discovery.Scan;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.*;

// ToDo: Add removeAccounts method, to clean up accounts after each tests -> No ordering needed
// ToDo: Cover discover method
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountsResourceTest extends JerseyTest {

  private static final Engine engine = new Engine();
  private final String MOCK = "MOCK";
  private String token;
  private static final String accountsPrefix = "/accounts/";

  /* Test Settings */
  @BeforeAll
  static void startUp() {
    // Init DB
    engine.setDbInMemory(true);
    engine.setDBName("AccountsResourceTestDB");
    engine.initDB();

    // Init everything else
    engine.init();

    // Start DiscoveryService
    engine.getService(DiscoveryService.class).start();
  }

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();

    client().register(ObjectMapperResolver.class);

    if (this.token == null) {
      this.token = engine.authenticateAPI(target(), "clouditor", "clouditor");
    }
  }

  @AfterEach
  public void cleanUp() {
    engine.shutdown();
  }

  @Override
  protected Application configure() {
    // CONTAINER_PORT = 0 means first available port is used
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new EngineAPI(engine);
  }

  /* Tests */
  @Test
  @Order(1)
  void testGetAccountsWhenNoAccountsAvailableThenStatusOkAndResponseEmpty() {
    // Request
    var response =
        target(accountsPrefix)
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();

    // Assertions
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Map<?, ?> accountsResource = response.readEntity(Map.class);
    Assertions.assertTrue(accountsResource.isEmpty());
  }

  @Test
  void testGetAccountsWhenOneAccountAvailableThenRespondWithAccount() {
    // Create and add new mock account
    AccountService accService = engine.getService(AccountService.class);
    CloudAccount<String> mockCloudAccount = new MockCloudAccount();
    try {
      accService.addAccount(MOCK, mockCloudAccount);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Request
    var response =
        target(accountsPrefix)
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();

    // Assertions
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Map<?, Map> accounts = response.readEntity(Map.class);
    assertTrue(accounts.containsKey(mockCloudAccount.getId()));
  }

  @Test
  void testGetAccountWhenNoAccountAvailableWithGivenProviderThen404AndNull() {
    // Request
    final String nonExistingProviderName = "UnknownProvider";
    var response =
        target(accountsPrefix + nonExistingProviderName)
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();

    // Assertions
    Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    Assertions.assertNull(response.readEntity(CloudAccount.class));
    assertThrows(
        NotFoundException.class,
        () ->
            target(accountsPrefix + "provider")
                .request()
                .header(
                    AuthenticationFilter.HEADER_AUTHORIZATION,
                    AuthenticationFilter.createAuthorization(token))
                .get(CloudAccount.class));
  }

  @Test
  void testGetAccountWhenOneAccountAvailableThen200AndResponseWithAccount() {
    // Create account
    AccountService accService = engine.getService(AccountService.class);
    CloudAccount mockCloudAccount = new MockCloudAccount();
    try {
      accService.addAccount(MOCK, mockCloudAccount);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Request
    var response =
        target(accountsPrefix + mockCloudAccount.getId())
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .get();

    CloudAccount responseCloudAccount = response.readEntity(MockCloudAccount.class);
    Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Assertions.assertEquals(mockCloudAccount.getId(), responseCloudAccount.getId());
  }

  @Test
  void testDiscoverWhenNoAccountAvailableThen404AndNull() {
    // Request
    var response =
        target(accountsPrefix + "discover/Mock Cloud")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .post(javax.ws.rs.client.Entity.json("{}"));

    // Assertions
    Assertions.assertNull(response.readEntity(CloudAccount.class));
    Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  void testPutAccountWhenAccountAddedSuccessfullyThenStatus204AndAccountInDB() {
    var response =
        target(accountsPrefix + "AWS")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .put(javax.ws.rs.client.Entity.json(new MockCloudAccount()));

    assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    var cloudAccounts = new HibernatePersistence().listAll(CloudAccount.class);
    // Collect accounts with default id of MockCloudAccount instances
    List<CloudAccount> collect =
        cloudAccounts.stream()
            .filter(cloudAccount -> cloudAccount.getId().equals(new MockCloudAccount().getId()))
            .collect(Collectors.toList());
    assertFalse(collect.isEmpty());
  }

  @Test
  void testPutAccountWhenTwoAccountsAddedSuccessfullyThenStatus204AndAccountsInDB() {
    var response =
        target(accountsPrefix + "AWS")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .put(javax.ws.rs.client.Entity.json(new MockCloudAccount()));

    assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    var cloudAccounts = new HibernatePersistence().listAll(CloudAccount.class);
    // Collect accounts with default id of MockCloudAccount instances
    List<CloudAccount> collect =
        cloudAccounts.stream()
            .filter(cloudAccount -> cloudAccount.getId().equals(new MockCloudAccount().getId()))
            .collect(Collectors.toList());
    assertFalse(collect.isEmpty());

    // Add 2nd account
    MockCloudAccount mockCloudAccount = new MockCloudAccount();
    mockCloudAccount.setId("2nd-Mock-ACC");
    var response2 =
        target(accountsPrefix + "AWS")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .put(javax.ws.rs.client.Entity.json(mockCloudAccount));

    assertEquals(Status.NO_CONTENT.getStatusCode(), response2.getStatus());

    cloudAccounts = new HibernatePersistence().listAll(CloudAccount.class);
    // Collect accounts with default id of MockCloudAccount instances
    collect =
        cloudAccounts.stream()
            .filter(cloudAccount -> cloudAccount.getId().equals(new MockCloudAccount().getId()))
            .collect(Collectors.toList());
    assertFalse(collect.isEmpty());

    System.out.println(new HibernatePersistence().listAll(Scan.class));
    List<CloudAccount> accounts = new HibernatePersistence().listAll(CloudAccount.class);
    System.out.println("Account 1 Scans: " + accounts.get(0).getScans());
    System.out.println("Account 2 Scans: " + accounts.get(1).getScans());
  }

  @Test
  void testPutAccountWhenIdIsEmptyThenValidatingFailsAndStatus400() {
    var objectMapper = new ObjectMapper();
    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("myId", "");
    requestBody.put("provider", MOCK);
    requestBody.put("autoDiscovered", false);

    var response =
        target(accountsPrefix + MOCK)
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .put(javax.ws.rs.client.Entity.json(requestBody));

    assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }
}
