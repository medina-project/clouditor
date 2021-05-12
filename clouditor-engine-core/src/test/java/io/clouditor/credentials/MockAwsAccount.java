package io.clouditor.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "mock_aws_account")
@Entity(name = "mock_aws_account")
@JsonTypeName(value = "MOCK_AWS")
public class MockAwsAccount extends CloudAccount {

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
    setUser("Mock-USER-ARN");
    System.out.println("Mock AWS Cloud Account validated.");
  }

  @Override
  public Object resolveCredentials() {
    return this;
  }
}
