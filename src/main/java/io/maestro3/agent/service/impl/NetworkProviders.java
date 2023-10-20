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

import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.model.network.NetworkingPolicy;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackNetworkingProvider;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.service.ServerDbService;


public class NetworkProviders {

    public static IOpenStackNetworkingProvider networking(
        OpenStackRegionConfig zone,
        IOpenStackClientProvider clientProvider, ServerDbService instanceService,
        IOpenStackStaticIpService staticIpService,
        IOpenStackTenantRepository projectService, IOpenStackVLANService vlanService,
        IOpenStackSecurityGroupService openStackSecurityGroupService,
        boolean floatingIps) {
        NetworkingPolicy networkingPolicy = zone.getNetworkingPolicy();
        switch (networkingPolicy.getNetworkingType()) {
            case AUTO:
                return new CompoundNetworkingProvider(staticIpService, instanceService, vlanService, clientProvider,
                    new OpenStackBasedOnPortsNetworkService(clientProvider, instanceService, staticIpService,
                        new OpenStackUpdatePortsService(clientProvider, instanceService, staticIpService, zone), projectService, zone,
                        vlanService, openStackSecurityGroupService),
                    new OpenStackBasedOnFloatingIpsNetworkService(clientProvider, instanceService, staticIpService,
                        new OpenStackUpdateFloatingIpsService(clientProvider, instanceService, staticIpService, zone), projectService, zone,
                        vlanService, openStackSecurityGroupService),
                    zone, floatingIps);
            case MANUAL:
                return new ManualModeNetworkingProvider(zone, staticIpService, instanceService, vlanService, clientProvider,
                    new OpenStackBasedOnFloatingIpsNetworkService(clientProvider, instanceService, staticIpService,
                        new OpenStackUpdateFloatingIpsService(clientProvider, instanceService, staticIpService, zone), projectService, zone,
                        vlanService, openStackSecurityGroupService));
            default:
                throw new IllegalStateException("Mode " + networkingPolicy.getNetworkingType() + " is not supported.");
        }
    }
}
