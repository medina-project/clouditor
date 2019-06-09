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
    service = "Persistence",
    group = "EU-SEC Audit API",
    assetType = "StorageObject",
    assetIcon = "fas fa-archive")
public class EuSecStorageObjectScanner extends EuSecScanner<StorageObject> {

  @Override
  protected List<StorageObject> list() throws ScanException {
    var objects = new ArrayList<StorageObject>();

    try {
      for (var scope : this.api.scopes().getScopes().getScopes()) {
        objects.addAll(
            this.api.objects().getObjects(scope, null, null, null).getObjects().stream()
                .map(object -> new StorageObject(object.getId(), object.getType(), scope))
                .collect(Collectors.toList()));
      }
    } catch (ApiException e) {
      throw new ScanException(e);
    }

    return objects;
  }

  @Override
  protected Asset transform(StorageObject object) throws ScanException {
    var asset = super.transform(object);

    try {
      asset.setProperty(
          "encryption",
          MAPPER.convertValue(
              this.api
                  .persistence()
                  .getEncryptionInfo(object.getId(), object.getScope())
                  .getEncryption(),
              HashMap.class));
    } catch (ApiException e) {
      throw new ScanException(e);
    }

    try {
      asset.setProperty(
          "location",
          MAPPER.convertValue(
              this.api.persistence().getLocation(object.getId(), object.getScope()),
              HashMap.class));
    } catch (ApiException e) {
      throw new ScanException(e);
    }

    return asset;
  }
}
