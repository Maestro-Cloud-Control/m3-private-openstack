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

import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.networking.bean.FloatingIp;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.OpenStackConversionUtils;

import java.util.Collection;
import java.util.List;


class OpenStackUpdateFloatingIpsService extends OpenStackUpdateStaticIpsService {

    public OpenStackUpdateFloatingIpsService(IOpenStackClientProvider clientProvider, ServerDbService instanceService,
                                             IOpenStackStaticIpService staticIpService, OpenStackRegionConfig zone) {
        super(clientProvider, instanceService, staticIpService, zone);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<? extends OpenStackStaticIpAddress> getIncomingIps(OpenStackTenant project) throws OSClientException {
        String tenantId = project.getNativeId();
        IOpenStackClient client = clientProvider.getClient(zone, project);
        List<FloatingIp> floatingIps = client.listFloatingIps(tenantId);
        return OpenStackConversionUtils.toOpenStackFloatingIps(floatingIps, project, zone);
    }
}
