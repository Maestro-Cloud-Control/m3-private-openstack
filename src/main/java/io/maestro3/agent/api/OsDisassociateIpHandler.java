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

package io.maestro3.agent.api;

import io.maestro3.agent.api.handler.AbstractM3ApiHandler;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateStaticIpAddressParameters;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.IOpenStackNetworkingProvider;
import io.maestro3.agent.service.IServiceFactory;
import io.maestro3.agent.service.IVirtOpenStackNetworkService;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.network.SdkStaticIpAddress;
import io.maestro3.sdk.v3.request.agent.DisassociateStaticIpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OsDisassociateIpHandler extends AbstractM3ApiHandler<DisassociateStaticIpRequest, SdkStaticIpAddress> {

    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private IServiceFactory<IOpenStackNetworkingProvider> networkingProviderFactory;

    @Autowired
    public OsDisassociateIpHandler(IOpenStackRegionRepository regionRepository, IOpenStackTenantRepository tenantRepository,
                                   IServiceFactory<IOpenStackNetworkingProvider> networkingProviderFactory) {
        super(DisassociateStaticIpRequest.class, ActionType.DISASSOCIATE_IP);
        this.networkingProviderFactory = networkingProviderFactory;
        this.regionRepository = regionRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public SdkStaticIpAddress handlePayload(ActionType actionType, DisassociateStaticIpRequest request) {
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(request.getTenantName(), region.getId());

        IOpenStackNetworkingProvider networkingProvider = networkingProviderFactory.get(region);
        IVirtOpenStackNetworkService networkService = networkingProvider.networkingService();
        StaticIpAddress staticIpAddress = networkService.disassociateStaticIp(tenant, DisassociateStaticIpAddressParameters.builder()
            .staticIp(request.getIp())
            .build());
        SdkStaticIpAddress sdkStaticIpAddress = new SdkStaticIpAddress();
        sdkStaticIpAddress.setIpAddress(staticIpAddress.getIpAddress());
        sdkStaticIpAddress.setPublic(staticIpAddress.isPublic());
        sdkStaticIpAddress.setInstanceId(staticIpAddress.getInstanceId());
        sdkStaticIpAddress.setRegionName(region.getRegionAlias());
        sdkStaticIpAddress.setTenantName(tenant.getTenantAlias());
        sdkStaticIpAddress.setTenantId(tenant.getNativeId());
        sdkStaticIpAddress.setZoneId(region.getId());
        return sdkStaticIpAddress;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }
}
