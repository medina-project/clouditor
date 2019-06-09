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

package io.clouditor.discovery.eusec;

import eu.sec.cert.Configuration;
import eu.sec.cert.api.CaApiIamApi;
import eu.sec.cert.api.CaApiObjectsApi;
import eu.sec.cert.api.CaApiPersistenceApi;
import eu.sec.cert.api.CaApiScopeApi;
import io.clouditor.credentials.EuSecAccount;
import io.clouditor.util.PersistenceManager;
import java.io.IOException;

public class EuSecClients {

  private CaApiScopeApi scope;
  private CaApiObjectsApi objects;
  private CaApiIamApi iam;
  private CaApiPersistenceApi persistence;

  public CaApiObjectsApi objects() {
    return this.objects;
  }

  public CaApiScopeApi scopes() {
    return this.scope;
  }

  public CaApiIamApi iam() {
    return this.iam;
  }

  public CaApiPersistenceApi persistence() {
    return this.persistence;
  }

  public void init() throws IOException {
    authenticate();
  }

  private void authenticate() throws IOException {
    // fetch information about the account
    var account = PersistenceManager.getInstance().getById(EuSecAccount.class, "EU-SEC Audit API");

    if (account == null) {
      throw new IOException("EU-SEC Audit API not configured");
    }

    var defaultClient = Configuration.getDefaultApiClient().setBasePath(account.getUrl());

    if (account.getUser() != null) {
      // use basic authentication
      defaultClient.setUsername(account.getUser());
      defaultClient.setPassword(account.getPassword());
    }

    this.scope = new CaApiScopeApi();
    this.objects = new CaApiObjectsApi();
    this.iam = new CaApiIamApi();
    this.persistence = new CaApiPersistenceApi();
  }
}
