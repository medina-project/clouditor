/*
 * Copyright 2016-2019 Fraunhofer AISEC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *            $$\                           $$\ $$\   $$\
 *            $$ |                          $$ |\__|  $$ |
 *   $$$$$$$\ $$ | $$$$$$\  $$\   $$\  $$$$$$$ |$$\ $$$$$$\    $$$$$$\   $$$$$$\
 *  $$  _____|$$ |$$  __$$\ $$ |  $$ |$$  __$$ |$$ |\_$$  _|  $$  __$$\ $$  __$$\
 *  $$ /      $$ |$$ /  $$ |$$ |  $$ |$$ /  $$ |$$ |  $$ |    $$ /  $$ |$$ |  \__|
 *  $$ |      $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$ |  $$ |$$\ $$ |  $$ |$$ |
 *  \$$$$$$\  $$ |\$$$$$   |\$$$$$   |\$$$$$$  |$$ |  \$$$   |\$$$$$   |$$ |
 *   \_______|\__| \______/  \______/  \_______|\__|   \____/  \______/ \__|
 *
 * This file is part of Clouditor Community Edition.
 */

package io.clouditor.credentials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.clouditor.Engine;
import io.clouditor.data_access_layer.HibernatePersistence;
import io.clouditor.discovery.DiscoveryService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsAsyncClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

class AwsAccountTest {

  private static final Engine engine = new Engine();

  private static final Logger LOGGER = LogManager.getLogger();
  private static final String ASSET_TYPE = "fake";

  private String token;

  @AfterAll
  static void cleanUpOnce() {
    engine.shutdown();
  }

  @BeforeAll
  static void startUpOnce() {
    engine.setDbInMemory(true);

    engine.setDBName("AwsAccountTestDB");

    // init db
    engine.initDB();

    // initialize every else
    engine.init();

    // start the DiscoveryService
    engine.getService(DiscoveryService.class).start();
  }

  @Test
  void testResolveCredentials() {
    var account = new AwsAccount();
    account.setAccessKeyId("my-key-id");
    account.setSecretAccessKey("my-secret");
    account.setAutoDiscovered(false);

    var credentials = account.resolveCredentials();

    assertNotNull(credentials);

    assertEquals("my-key-id", credentials.accessKeyId());
    assertEquals("my-secret", credentials.secretAccessKey());

    account = new AwsAccount();
    account.setAutoDiscovered(true);

    // system properties are the first in the discovery chain so we override them
    System.setProperty("aws.accessKeyId", "my-discovered-key-id");
    System.setProperty("aws.secretAccessKey", "my-discovered-secret");

    assertTrue(account.isAutoDiscovered());

    credentials = account.resolveCredentials();

    assertNotNull(credentials);

    assertEquals("my-discovered-key-id", credentials.accessKeyId());
    assertEquals("my-discovered-secret", credentials.secretAccessKey());
  }

  @Disabled
  @Test
  void testMultipleAccounts() throws ExecutionException, InterruptedException, IOException {
    AwsAccount awsAccount = AwsAccount.discover();
    assertNotNull(awsAccount);
    System.out.println("Id: " + awsAccount.getId());
    System.out.println("Account Id: " + awsAccount.getAccountId());

    // Clouditor (account which will assume roles)
    // ToDo: Check difference between sync. and async variants
    var clouditorClient =
        StsAsyncClient.builder().credentialsProvider(DefaultCredentialsProvider.create()).build();

    // Request AssumeRole
    AssumeRoleRequest assumeRoleRequest =
        AssumeRoleRequest.builder()
            .durationSeconds(3600)
            .roleArn(System.getenv("ROLE_ARN"))
            .roleSessionName("TempSessionArn1")
            .build();

    Future<AssumeRoleResponse> responseFuture = clouditorClient.assumeRole(assumeRoleRequest);
    AssumeRoleResponse assumeRoleResponse = responseFuture.get();
    software.amazon.awssdk.services.sts.model.Credentials credentials =
        assumeRoleResponse.credentials();

    AwsSessionCredentials sessionCredentials =
        AwsSessionCredentials.create(
            credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken());
    var assumedRoleCredentials =
        AwsCredentialsProviderChain.builder()
            .credentialsProviders(StaticCredentialsProvider.create(sessionCredentials))
            .build();

    var assumedRoleClient = StsClient.builder().credentialsProvider(assumedRoleCredentials).build();
    AwsAccount assumedRoleAwsAccount = new AwsAccount();
    assumedRoleAwsAccount.setAutoDiscovered(true);
    assumedRoleAwsAccount.setAccountId(assumedRoleClient.getCallerIdentity().account());
    assumedRoleAwsAccount.setUser(assumedRoleClient.getCallerIdentity().arn());

    // Test validation of assumed Role
    assumedRoleAwsAccount.validate();
  }

  @Disabled
  @Test
  void testAddAccount() throws IOException {
    System.out.println(new HibernatePersistence().listAll(AwsAccount.class));
    var accountService = engine.getService(AccountService.class);
    AwsAccount awsAccount1 = new AwsAccount();
    awsAccount1.setAccessKeyId(
        getCredentialsFromAuditingAccount().resolveCredentials().accessKeyId());
    awsAccount1.setSecretAccessKey(
        getCredentialsFromAuditingAccount().resolveCredentials().secretAccessKey());
    awsAccount1.setAccountId("Account1");
    awsAccount1.setAutoDiscovered(false);
    awsAccount1.setRegion(Region.US_EAST_2.toString());
    accountService.addAccount("AWS", awsAccount1);

    //    AwsAccount awsAccount = AwsAccount.discover();
    System.out.println(new HibernatePersistence().listAll(AwsAccount.class).get(0).getId());
    System.out.println("Region: " + Region.US_EAST_2.id());
  }

  // Test passes when no Exception are thrown
  @Disabled
  @Test
  void testCredentials() {
    var auditorAccountId =
        StsClient.builder()
            .credentialsProvider(getCredentialsFromAuditingAccount())
            .region(Region.US_EAST_2)
            .build()
            .getCallerIdentity();
    LOGGER.info(
        "Auditor Account {} validated with user {}.",
        auditorAccountId.account(),
        auditorAccountId.arn());
    var assumedRoleAccountId =
        StsClient.builder()
            .credentialsProvider(getCredentialsFromAssumedRoleAccount())
            .region(Region.US_EAST_2)
            .build()
            .getCallerIdentity();
    LOGGER.info(
        "Assumed Role Account {} validated with user {}.",
        assumedRoleAccountId.account(),
        assumedRoleAccountId.arn());
  }

  private ProfileCredentialsProvider getCredentialsFromAuditingAccount() {
    // in .aws/config the profile name must be "auditor"
    return ProfileCredentialsProvider.create("auditor");
  }

  private ProfileCredentialsProvider getCredentialsFromAssumedRoleAccount() {
    // in .aws/config the profile name must be "arnaccess" for the role_arn
    return ProfileCredentialsProvider.create("arnaccess");
  }
}
