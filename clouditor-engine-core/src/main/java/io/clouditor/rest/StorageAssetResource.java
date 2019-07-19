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

package io.clouditor.rest;

import static io.clouditor.auth.AuthenticationService.ROLE_USER;

import io.clouditor.Engine;
import io.clouditor.discovery.StorageAsset;
import io.clouditor.discovery.StorageAssetService;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A storage asset discovery endpoint */
@Path("storageAsset")
@RolesAllowed(ROLE_USER)
public class StorageAssetResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageAssetResource.class);

  private final Engine engine;
  private final StorageAssetService service;

  /**
   * Constructs a new resource.
   *
   * @param engine the Clouditor Engine
   */
  @Inject
  public StorageAssetResource(Engine engine, StorageAssetService service) {
    this.engine = engine;
    this.service = service;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, StorageAsset> getStorageAssets() {
    return this.service.getStorageAssets();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}/")
  public StorageAsset getStorageAsset(@PathParam(value = "id") String storageAssetId) {
    var storageAssets = this.service.getStorageAssets();

    var storageAsset = storageAssets.get(storageAssetId);

    if (storageAsset == null) {
      throw new NotFoundException();
    }

    return storageAsset;
  }
}
