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
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.service.IVirtOpenStackBasedOnPortsNetworkService;
import io.maestro3.agent.service.IVirtOpenStackNetworkService;
import io.maestro3.agent.service.ServerDbService;


public class CompoundNetworkingProvider extends AutoModeNetworkingProvider {
    private final OpenStackBasedOnFloatingIpsNetworkService floatingIpsNetworkService;

    public CompoundNetworkingProvider(IOpenStackStaticIpService staticIpService, ServerDbService instanceService, IOpenStackVLANService vlanService,
                                      IOpenStackClientProvider openStackClientProvider, IVirtOpenStackBasedOnPortsNetworkService networkService,
                                      OpenStackBasedOnFloatingIpsNetworkService floatingIpsNetworkService, OpenStackRegionConfig zone, boolean floatingIps) {
        super(zone, staticIpService, instanceService, vlanService, openStackClientProvider, networkService, floatingIps);
        this.floatingIpsNetworkService = floatingIpsNetworkService;
    }

    @Override
    protected IVirtOpenStackNetworkService networkServiceForStaticIpsProcessing() {
        return floatingIpsNetworkService;
    }
}

