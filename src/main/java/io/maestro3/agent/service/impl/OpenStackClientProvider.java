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

package io.maestro3.agent.service.impl;

import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


@Service
class OpenStackClientProvider implements IOpenStackClientProvider {

    private static final Logger LOG = LogManager.getLogger(OpenStackClientProvider.class);

    private static final int MAX_CLIENTS_SIZE = 500;
    private static final int MAX_HTTP_CLIENTS_SIZE = 20;

    @Autowired
    private OpenStackApiProvider apiProvider;

    @Override
    public IOpenStackClient getClient(final OpenStackRegionConfig zone, final OpenStackTenant project) {
        Assert.notNull(project, "project cannot be null.");

        return OpenStackClient.wrap(apiProvider.openStack(project, zone));
    }

    @Override
    public IOpenStackClient getAdminClient(final OpenStackRegionConfig zone, OpenStackTenant project) {
        Assert.notNull(project, "project cannot be null.");
        return OpenStackClient.wrap(apiProvider.adminOpenStack(zone));
    }
}
