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

package eu.sec.cert.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.clouditor.assurance.EvaluationResult;
import java.util.ArrayList;
import java.util.List;

public class Evidence {

  @JsonProperty("class")
  private String type;

  private String endTime;

  private boolean passed;

  private String planID;

  private String startTime;

  private AccessControlList acl;

  @JsonProperty("evidence-details:log")
  private List<String> log;

  /** Link to the evidence store. Will be filled after storing. */
  @JsonIgnore private String url;

  @JsonProperty("evidence-details:results")
  private List<EvaluationResult> results = new ArrayList<>();

  public String getEndTime() {
    return endTime;
  }

  public boolean isPassed() {
    return passed;
  }

  public String getPlanID() {
    return planID;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public void setPlanID(String planID) {
    this.planID = planID;
  }

  public void setPassed(boolean passed) {
    this.passed = passed;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getLog() {
    return log;
  }

  public void setLog(List<String> log) {
    this.log = log;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setResults(List<EvaluationResult> results) {
    this.results = results;
  }

  public AccessControlList getAcl() {
    return acl;
  }

  public void setAcl(AccessControlList acl) {
    this.acl = acl;
  }
}
