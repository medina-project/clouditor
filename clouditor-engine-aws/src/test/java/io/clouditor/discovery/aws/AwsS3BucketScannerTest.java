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

package io.clouditor.discovery.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.clouditor.assurance.RuleService;
import io.clouditor.util.FileSystemManager;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

class AwsS3BucketScannerTest extends AwsScannerTest {

  @BeforeAll
  static void setUpOnce() {
    discoverAssets(
        S3Client.class,
        AwsS3BucketScanner::new,
        api -> {
          when(api.listBuckets())
              .thenReturn(
                  ListBucketsResponse.builder()
                      .buckets(
                          Bucket.builder().name("Bucket-A").build(),
                          Bucket.builder().name("Bucket-B").build(),
                          Bucket.builder().name("Bucket-C").build())
                      .build());

          when(api.getBucketEncryption(
                  GetBucketEncryptionRequest.builder().bucket("Bucket-A").build()))
              .thenReturn(
                  GetBucketEncryptionResponse.builder()
                      .serverSideEncryptionConfiguration(
                          ServerSideEncryptionConfiguration.builder()
                              .rules(
                                  ServerSideEncryptionRule.builder()
                                      .applyServerSideEncryptionByDefault(
                                          ServerSideEncryptionByDefault.builder()
                                              .kmsMasterKeyID("key")
                                              .sseAlgorithm(ServerSideEncryption.AES256)
                                              .build())
                                      .build())
                              .build())
                      .build());

          when(api.getBucketEncryption(
                  GetBucketEncryptionRequest.builder().bucket("Bucket-B").build()))
              .thenReturn(
                  GetBucketEncryptionResponse.builder()
                      .serverSideEncryptionConfiguration(
                          ServerSideEncryptionConfiguration.builder()
                              .rules(
                                  ServerSideEncryptionRule.builder()
                                      .applyServerSideEncryptionByDefault(
                                          ServerSideEncryptionByDefault.builder()
                                              .kmsMasterKeyID("key")
                                              .sseAlgorithm(ServerSideEncryption.AWS_KMS)
                                              .build())
                                      .build())
                              .build())
                      .build());

          when(api.getBucketEncryption(
                  GetBucketEncryptionRequest.builder().bucket("Bucket-C").build()))
              .thenThrow(AwsServiceException.builder().statusCode(404).build());

          when(api.getPublicAccessBlock(ArgumentMatchers.any(GetPublicAccessBlockRequest.class)))
              .thenReturn(
                  GetPublicAccessBlockResponse.builder()
                      .publicAccessBlockConfiguration(
                          PublicAccessBlockConfiguration.builder().build())
                      .build());

          when(api.getBucketReplication(
                  GetBucketReplicationRequest.builder().bucket("Bucket-A").build()))
              .thenReturn(
                  GetBucketReplicationResponse.builder()
                      .replicationConfiguration(
                          ReplicationConfiguration.builder()
                              .role("test-role")
                              .rules(
                                  ReplicationRule.builder()
                                      .id("234")
                                      .status(ReplicationRuleStatus.ENABLED)
                                      .priority(1)
                                      .deleteMarkerReplication(
                                          DeleteMarkerReplication.builder()
                                              .status(DeleteMarkerReplicationStatus.DISABLED)
                                              .build())
                                      .filter(
                                          ReplicationRuleFilter.builder().prefix("test").build())
                                      .destination(Destination.builder().bucket("Bucket-A").build())
                                      .build(),
                                  ReplicationRule.builder()
                                      .id("123")
                                      .status(ReplicationRuleStatus.ENABLED)
                                      .priority(1)
                                      .deleteMarkerReplication(
                                          DeleteMarkerReplication.builder()
                                              .status(DeleteMarkerReplicationStatus.DISABLED)
                                              .build())
                                      .filter(
                                          ReplicationRuleFilter.builder().prefix("test").build())
                                      .destination(Destination.builder().bucket("Bucket-B").build())
                                      .build())
                              .build())
                      .build());

          when(api.getBucketReplication(
                  GetBucketReplicationRequest.builder().bucket("Bucket-B").build()))
              .thenReturn(
                  GetBucketReplicationResponse.builder()
                      .replicationConfiguration(
                          ReplicationConfiguration.builder()
                              .role("test-role")
                              .rules(
                                  ReplicationRule.builder()
                                      .id("234")
                                      .status(ReplicationRuleStatus.ENABLED)
                                      .priority(1)
                                      .deleteMarkerReplication(
                                          DeleteMarkerReplication.builder()
                                              .status(DeleteMarkerReplicationStatus.DISABLED)
                                              .build())
                                      .filter(
                                          ReplicationRuleFilter.builder().prefix("test").build())
                                      .destination(Destination.builder().bucket("Bucket-A").build())
                                      .build())
                              .build())
                      .build());

          when(api.getBucketReplication(
                  GetBucketReplicationRequest.builder().bucket("Bucket-C").build()))
              .thenReturn(
                  GetBucketReplicationResponse.builder()
                      .replicationConfiguration(
                          ReplicationConfiguration.builder()
                              .role("test-role")
                              .rules(
                                  ReplicationRule.builder()
                                      .id("234")
                                      .status(ReplicationRuleStatus.ENABLED)
                                      .priority(1)
                                      .deleteMarkerReplication(
                                          DeleteMarkerReplication.builder()
                                              .status(DeleteMarkerReplicationStatus.DISABLED)
                                              .build())
                                      .filter(
                                          ReplicationRuleFilter.builder().prefix("test").build())
                                      .destination(Destination.builder().bucket("Bucket-A").build())
                                      .build(),
                                  ReplicationRule.builder()
                                      .id("123")
                                      .status(ReplicationRuleStatus.ENABLED)
                                      .priority(1)
                                      .deleteMarkerReplication(
                                          DeleteMarkerReplication.builder()
                                              .status(DeleteMarkerReplicationStatus.DISABLED)
                                              .build())
                                      .filter(
                                          ReplicationRuleFilter.builder().prefix("test").build())
                                      .destination(Destination.builder().bucket("Bucket-B").build())
                                      .build())
                              .build())
                      .build());

          when(api.getBucketLocation(GetBucketLocationRequest.builder().bucket("Bucket-A").build()))
              .thenReturn(
                  GetBucketLocationResponse.builder()
                      .locationConstraint(BucketLocationConstraint.EU_WEST_1)
                      .build());

          when(api.getBucketLocation(GetBucketLocationRequest.builder().bucket("Bucket-B").build()))
              .thenReturn(
                  GetBucketLocationResponse.builder()
                      .locationConstraint(BucketLocationConstraint.EU_WEST_1)
                      .build());

          when(api.getBucketLocation(GetBucketLocationRequest.builder().bucket("Bucket-C").build()))
              .thenReturn(
                  GetBucketLocationResponse.builder()
                      .locationConstraint(BucketLocationConstraint.EU_WEST_1)
                      .build());

          when(api.getBucketLifecycleConfiguration(
                  (GetBucketLifecycleConfigurationRequest) ArgumentMatchers.any()))
              .thenThrow(AwsServiceException.builder().statusCode(404).build());
        });
  }

  @Test
  void testBucketReplicationDestinationCheck() throws IOException {
    var rule =
        this.engine
            .getService(RuleService.class)
            .loadRule(
                FileSystemManager.getInstance()
                    .getPathForResource("rules/aws/s3/bucket-replication-destination.md"));

    assertNotNull(rule);

    var bucketA = assets.get("arn:aws:s3:::Bucket-A");

    assertNotNull(bucketA);
    assertTrue(rule.evaluate(bucketA).isOk());
  }

  @Test
  void testBucketDefaultEncryptionCheck() throws IOException {
    var rule =
        this.engine
            .getService(RuleService.class)
            .loadRule(
                FileSystemManager.getInstance()
                    .getPathForResource("rules/aws/s3/bucket-default-encryption.md"));

    assertNotNull(rule);

    var bucketA = assets.get("arn:aws:s3:::Bucket-A");

    assertNotNull(bucketA);
    assertTrue(rule.evaluate(bucketA).isOk());

    var bucketB = assets.get("arn:aws:s3:::Bucket-B");

    assertNotNull(bucketB);
    assertTrue(rule.evaluate(bucketB).isOk());

    var bucketC = assets.get("arn:aws:s3:::Bucket-C");

    assertNotNull(bucketC);
    assertFalse(rule.evaluate(bucketC).isOk());
  }
}
