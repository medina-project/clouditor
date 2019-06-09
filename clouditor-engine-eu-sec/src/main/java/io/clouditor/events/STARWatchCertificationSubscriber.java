/*
 * Copyright (c) 2016-2019, Fraunhofer AISEC. All rights reserved.
 *
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
 *
 * Clouditor Community Edition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Clouditor Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * long with Clouditor Community Edition.  If not, see <https://www.gnu.org/licenses/>
 */

package io.clouditor.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sec.cert.evidence.Evidence;
import eu.sec.cert.starwatch.UpdateAssessmentRequest;
import io.clouditor.assurance.Certification;
import io.clouditor.assurance.Control.Fulfillment;
import io.clouditor.credentials.STARWatchAccount;
import io.clouditor.util.PersistenceManager;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class STARWatchCertificationSubscriber extends CertificationSubscriber {

  private final Client client;

  private List<NuvlaPublisher> nuvlaPublishers =
      List.of(new NuvlaPublisher("https://nuv.la/api/", null, null));

  public STARWatchCertificationSubscriber() {
    this.client = ClientBuilder.newClient();
  }

  @Override
  public void handle(Certification item) {
    // fetch information about the account
    var account = PersistenceManager.getInstance().getById(STARWatchAccount.class, "STARWatch");

    if (account == null) {
      return;
    }

    var target = client.target(account.getUrl());

    for (var control : item.getControls()) {
      // don't send inactive controls or controls that are not yet evaluated
      if (!control.isActive() || control.getFulfilled() == Fulfillment.NOT_EVALUATED) {
        continue;
      }

      var results = control.getResults();

      var evidences =
          this.nuvlaPublishers.stream()
              .map(publisher -> publisher.publish(control, results))
              .collect(Collectors.toList());

      for (var rule : control.getRules()) {
        var request = new UpdateAssessmentRequest();
        request.setAssessmentId(account.getAssessmentId());
        request.setAssessedAt(new Date().toString());
        request.setObjectiveId(control.getControlId() + "/" + rule.getId());
        request.setResult(control.isGood());

        // list of evidences is currently the same for all objectives
        // TODO: split evidences per rules
        request.setEvidence(evidences.stream().map(Evidence::getUrl).collect(Collectors.toList()));

        var mapper = new ObjectMapper();
        try {
          LOGGER.debug(
              "Sending the following evidence to STARWatch: {}",
              mapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
          LOGGER.error("An error occurred while serializing evidence: {}", e);
        }

        var resp =
            target
                .path("/continuous_assessment/" + account.getAssessmentId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + account.getToken())
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        var responseEntity = resp.readEntity(String.class);

        LOGGER.info("Got result from STARWatch: {} {}", resp, responseEntity);
      }
    }
  }
}
