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
import eu.sec.cert.evidence.AccessControlList;
import eu.sec.cert.evidence.AccessRule;
import eu.sec.cert.evidence.Evidence;
import eu.sec.cert.evidence.Owner;
import io.clouditor.assurance.Control;
import io.clouditor.assurance.EvaluationResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NuvlaPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);

  private String username;
  private String password;
  private WebTarget target;
  private String cookie;

  NuvlaPublisher(@NotNull String url, @NotNull String username, @NotNull String password) {
    this.username = username;
    this.password = password;

    var client = ClientBuilder.newClient();

    this.target = client.target(url);
  }

  private void login() {
    var form = new Form();
    form.param("href", "session-template/internal");
    form.param("username", this.username);
    form.param("password", this.password);

    var response = this.target.path("session").request().post(Entity.form(form));

    if (response.getStatus() == 201) {
      LOGGER.info("Successfully retrieved cookie from Nuvla.");
      this.cookie = response.getHeaderString("Set-Cookie");
    } else {
      LOGGER.error("Some error occurred while fetching cookie from Nuvla");
    }
  }

  Evidence publish(Control control, List<EvaluationResult> results) {
    var evidence = new Evidence();
    evidence.setType(Evidence.class.getName());
    evidence.setEndTime(Instant.now().toString());
    evidence.setStartTime(Instant.now().toString());
    evidence.setPassed(control.isGood());
    evidence.setResults(results);
    evidence.setLog(new ArrayList<>()); // empty for now
    evidence.setPlanID(control.getControlId());
    evidence.setAcl(
        new AccessControlList(
            new Owner("clouditor", "USER"),
            List.of(
                new AccessRule("ROLE", "ADMIN", "ALL"),
                new AccessRule("USER", "eusecca", "VIEW"))));

    var mapper = new ObjectMapper();
    try {
      // nuvla doesnt like maps...
      var json = mapper.writeValueAsString(evidence);

      LOGGER.debug("Sending {} to nuvla", json);

      if (this.cookie == null) {
        this.login();
      }

      var response =
          this.target
              .path("evidence-record")
              .request()
              .header("Cookie", this.cookie)
              .post(Entity.entity(json, MediaType.APPLICATION_JSON_TYPE));

      if (response.getStatus() == 201) {
        // yay!
        var evidenceUrl =
            this.target.path(response.getHeaderString("Location")).getUri().toString();

        LOGGER.info("Stored evidence as {}", evidenceUrl);

        // set evidence url
        evidence.setUrl(evidenceUrl);
      } else {
        LOGGER.error("Error: {}", response);
        // some error occurred, lets invalidate the cookie
        // TODO: this request gets lost, fix that
        this.cookie = null;
      }
    } catch (JsonProcessingException e) {
      LOGGER.error("Error: {}", e.getMessage());
    }

    return evidence;
  }
}
