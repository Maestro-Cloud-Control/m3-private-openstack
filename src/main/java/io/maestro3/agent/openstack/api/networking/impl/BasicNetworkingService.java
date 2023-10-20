/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.openstack.api.networking.impl;

import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.helper.functions.EnforceVersionToUrl;

import java.net.URL;


public abstract class BasicNetworkingService extends BasicService {

    private static final EnforceVersionToUrl enforceVersionToUrl = EnforceVersionToUrl.to("/v2.0", true);

    protected BasicNetworkingService(IOSClient client) {
        super(ServiceType.NETWORK, client);
    }

    @Override
    protected URL endpoint() throws OSClientException {
        return this.endpoint(enforceVersionToUrl);
    }
}
