package io.clouditor.credentials;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.clouditor.Engine;
import io.clouditor.data_access_layer.HibernatePersistence;
import io.clouditor.discovery.DiscoveryService;
import io.clouditor.discovery.Scan;
import io.clouditor.rest.AuthenticationFilter;
import io.clouditor.rest.EngineAPI;
import io.clouditor.rest.ObjectMapperResolver;
import java.io.IOException;
import java.util.HashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.*;

class MultipleAccountsServiceTest extends JerseyTest {
  private static final Engine engine = new Engine();

  MultipleAccountsService mAccService =
      new MultipleAccountsService(new AccountService(new DiscoveryService()));

  private static final String accountsPrefix = "/accounts/";
  private String token;

  @AfterAll
  static void cleanUpOnce() {
    engine.shutdown();
  }

  @BeforeAll
  static void startUpOnce() {
    engine.setDbInMemory(true);

    engine.setDBName("MultipleAccountsServiceTest");

    // init db
    engine.initDB();

    // initialize every else
    engine.init();

    // start the DiscoveryService
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

  @Override
  protected Application configure() {
    // CONTAINER_PORT = 0 means first available port is used
    forceSet(TestProperties.CONTAINER_PORT, "0");
    return new EngineAPI(engine);
  }

  @Test
  void testAddAccounts() throws Exception {
    var accountService = engine.getService(AccountService.class);
    CloudAccount mockAuditorAccount = new AwsAccount();
    mockAuditorAccount.provider = "AWS";

    var arns = new HashSet<String>();
    arns.add("arn1");
    arns.add("arn2");
    accountService.addAccount("AWS", mockAuditorAccount);

    var cloudAccount = new HibernatePersistence().get(AwsAccount.class, "AWS");
    System.out.println(cloudAccount.get());
    // ToDo: Weiter versuchen

    assertTrue(true);
  }

  @Test
  void testPutAccount() {
    System.out.println(
        "Number of scans from Hibernate: " + new HibernatePersistence().listAll(Scan.class).size());
    System.out.println("Hibernate: " + new HibernatePersistence().listAll(CloudAccount.class));
    // Create Account
    CloudAccount mockCloudAccount = new AwsAccount();
    mockCloudAccount.setAccountId("IdXYZ");
    mockCloudAccount.setAutoDiscovered(false);
    mockCloudAccount.setUser("UserXYZ");
    AccountService accountService = engine.getService(AccountService.class);
    try {
      accountService.addAccount("AWS", mockCloudAccount);
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("Hibernate: " + new HibernatePersistence().listAll(CloudAccount.class));

    // Request with account and provider as PathParam
    var response =
        target(accountsPrefix + "AWS")
            .request()
            .header(
                AuthenticationFilter.HEADER_AUTHORIZATION,
                AuthenticationFilter.createAuthorization(token))
            .put(javax.ws.rs.client.Entity.json(mockCloudAccount));

    Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

    // GET http://localhost:9999/engine/discovery/
    var discoveryResource = engine.getService(DiscoveryService.class);
    System.out.println(discoveryResource.getScans().values());
  }

  @Test
  void imitatingUIRequest() throws IOException {
    // PUT http://localhost:9999/engine/accounts/AWS
    var accountService = engine.getService(AccountService.class);
    var awsAccount = new AwsAccount();
    awsAccount.setAutoDiscovered(false);
    awsAccount.provider = "AWS";
    awsAccount.setAccountId("XXXXXXXXXXXX");
    accountService.addAccount("AWS", awsAccount);

    assertTrue(new HibernatePersistence().get(CloudAccount.class, "AWS").isPresent());

    // ToDo: GET http://localhost:9999/engine/statistics/ (How can I mock private @Inject fields?)

    // GET http://localhost:9999/engine/accounts
    var returnedAccounts = accountService.getAccounts();
    var returnedAccount = (AwsAccount) returnedAccounts.get("AWS");
    System.out.println("Provider:" + returnedAccount.provider);
    System.out.println("AccountId " + returnedAccount.accountId);
    System.out.println("User: " + returnedAccount.user);
    System.out.println("AutoDiscovered: " + returnedAccount.isAutoDiscovered());
    System.out.println("AccessKet: " + returnedAccount.accessKeyId);
    System.out.println("SecretKey: " + returnedAccount.secretAccessKey);
    System.out.println("Region: " + returnedAccount.getRegion());
    System.out.println("_id: " + returnedAccount.getId());

    // GET http://localhost:9999/engine/discovery/
    var discoveryResource = engine.getService(DiscoveryService.class);
    System.out.println(discoveryResource.getScans().values());
  }

  /* Helper classes and methods */

  @Table(name = "aws_account")
  @Entity(name = "aws_account")
  @JsonTypeName(value = "AWS")
  private static class AwsAccount extends CloudAccount {

    @Column(name = "access_key_id", nullable = false)
    @JsonProperty
    private String accessKeyId = "MockAccessKey";

    @Column(name = "secret_access_key")
    @JsonProperty
    private String secretAccessKey = "MockSecretKey";

    public String getRegion() {
      return region;
    }

    public void setRegion(String region) {
      this.region = region;
    }

    @Column(name = "region")
    @JsonProperty
    private String region = "us-east-2";

    public String accessKeyId() {
      return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
      this.accessKeyId = accessKeyId;
    }

    public String secretAccessKey() {
      return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
      this.secretAccessKey = secretAccessKey;
    }

    @Override
    public void validate() {
      setUser("USER-ARN");
      System.out.println("Mock AWS Cloud Account validated.");
    }

    @Override
    public Object resolveCredentials() {
      return this;
    }
  }
}
