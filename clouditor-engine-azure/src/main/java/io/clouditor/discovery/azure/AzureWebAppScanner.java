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

package io.clouditor.discovery.azure;

import com.microsoft.azure.management.appservice.WebApp;
import io.clouditor.discovery.Asset;
import io.clouditor.discovery.ScanException;
import io.clouditor.discovery.ScannerInfo;
import java.util.List;

@ScannerInfo(assetType = "WebApp", group = "Azure", service = "App Services")
public class AzureWebAppScanner extends AzureScanner<WebApp> {

  public AzureWebAppScanner() {
    super(WebApp::id, WebApp::name);
  }

  @Override
  protected List<WebApp> list() {
    return this.api.azure().webApps().list();
  }

  @Override
  protected Asset transform(WebApp webapp) throws ScanException {
    var asset = super.transform(webapp);

    asset.setProperty("httpsOnly", webapp.httpsOnly());

    asset.setProperty("ftpsState", webapp.ftpsState());

    asset.setProperty("http20Enabled", webapp.http20Enabled());

    asset.setProperty("websocketsEnabled", webapp.webSocketsEnabled());

    asset.setProperty("remoteDebuggingEnabled", webapp.remoteDebuggingEnabled());

    /* This call requires special permissions
     asset.setProperty("authenticationEnabled", webapp.getAuthenticationConfig().inner().enabled());
    */

    return asset;
  }
}
