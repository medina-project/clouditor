package io.clouditor.credentials;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.IOException;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Mock class for {@link CloudAccount}, since, e.g., AwsAccount and AzureAccount are out of scope
 * here
 */
@Table(name = "mock_account")
@Entity(name = "mock_account")
@JsonTypeName(value = "MOCK")
public class MockCloudAccount extends CloudAccount<String> {

  // Initialize id since it has to be non-null
  public MockCloudAccount() {
    myId = "MOCK_ACCOUNT_1";
  }

  @Override
  public void validate() throws IOException {
    if (myId.equals("")) {
      LOGGER.error("MockAccount Validation threw IO-Exception (for test purposes)");
      throw new IOException();
    }
    setUser("Mock-USER");
    System.out.println("Mock Cloud Account validated.");
  }

  @Override
  public String resolveCredentials() {
    return "FakeCredentials";
  }
}
