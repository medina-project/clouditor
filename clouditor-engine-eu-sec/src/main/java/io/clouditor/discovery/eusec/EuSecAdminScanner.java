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

import eu.sec.cert.ApiException;
import io.clouditor.discovery.Asset;
import io.clouditor.discovery.ScanException;
import io.clouditor.discovery.ScannerInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ScannerInfo(
    service = "IAM",
    group = "EU-SEC Audit API",
    assetType = "Administrator",
    assetIcon = "fas fa-user")
public class EuSecAdminScanner extends EuSecScanner<Administrator> {

  @Override
  protected List<Administrator> list() throws ScanException {
    var users = new ArrayList<Administrator>();

    try {
      for (var scope : this.api.scopes().getScopes().getScopes()) {
        users.addAll(
            this.api.iam().getAdmins(scope).getAdmins().stream()
                .map(id -> new Administrator(id, scope))
                .collect(Collectors.toList()));
      }
    } catch (ApiException e) {
      throw new ScanException(e);
    }

    return users;
  }

  @Override
  protected Asset transform(Administrator administrator) throws ScanException {
    var asset = super.transform(administrator);

    try {
      asset.setProperty(
          "authenticator",
          MAPPER.convertValue(
              this.api.iam().getUserAuthType(administrator.getId(), administrator.getScope()),
              HashMap.class));
    } catch (ApiException e) {
      throw new ScanException(e);
    }

    try {
      asset.setProperty(
          "passwordRequirements",
          MAPPER.convertValue(
              this.api
                  .iam()
                  .getPasswordRequirements(administrator.getId(), administrator.getScope())
                  .getPasswordRequirements(),
              HashMap.class));
    } catch (ApiException e) {
      throw new ScanException(e);
    }

    try {
      asset.setProperty(
          "logins",
          MAPPER.convertValue(
              this.api
                  .iam()
                  .getUserAccesses(
                      administrator.getId(), administrator.getScope(), null, null, null),
              HashMap.class));
    } catch (ApiException e) {
      throw new ScanException(e);
    }

    return asset;
  }
}
