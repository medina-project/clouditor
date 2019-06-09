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

package io.clouditor.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.IOException;

@JsonTypeName("STARWatch")
public class STARWatchAccount extends CloudAccount<STARWatchAccount> {

  @JsonProperty private String token;
  @JsonProperty private String url;
  @JsonProperty private String assessmentId;

  @Override
  public void validate() {
    if (this.isAutoDiscovered()) {
      this.url = System.getenv().get("STAR_WATCH_URL");
      this.token = System.getenv().get("STAR_WATCH_TOKEN");
      this.assessmentId = System.getenv().get("STAR_WATCH_ASSESSMENT_ID");
    }
  }

  public static STARWatchAccount discover() throws IOException {
    var url = System.getenv().get("STAR_WATCH_URL");
    var token = System.getenv().get("STAR_WATCH_TOKEN");
    var assessmentId = System.getenv().get("STAR_WATCH_ASSESSMENT_ID");

    if (url != null && token != null && assessmentId != null) {
      var account = new STARWatchAccount();
      account.setUrl(url);
      account.setToken(token);
      account.setAssessmentId(assessmentId);

      return account;
    }

    throw new IOException("STARWatch can not be auto-discovered.");
  }

  private void setAssessmentId(String assessmentId) {
    this.assessmentId = assessmentId;
  }

  private void setToken(String token) {
    this.token = token;
  }

  private void setUrl(String url) {
    this.url = url;
  }

  @Override
  public STARWatchAccount resolveCredentials() {
    return this;
  }

  public String getAssessmentId() {
    return this.assessmentId;
  }

  public String getUrl() {
    return this.url;
  }

  public String getToken() {
    return this.token;
  }
}
