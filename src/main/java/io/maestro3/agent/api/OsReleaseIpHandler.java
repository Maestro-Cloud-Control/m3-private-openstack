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
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.IOpenStackNetworkingProvider;
import io.maestro3.agent.service.IServiceFactory;
import io.maestro3.agent.service.IVirtOpenStackNetworkService;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.request.agent.DeallocateStaticIpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OsReleaseIpHandler extends AbstractM3ApiHandler<DeallocateStaticIpRequest, Boolean> {

    private static final Logger LOG = LogManager.getLogger(OsReleaseIpHandler.class);

    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private IServiceFactory<IOpenStackNetworkingProvider> networkingProviderFactory;

    @Autowired
    public OsReleaseIpHandler(IOpenStackRegionRepository regionRepository, IOpenStackTenantRepository tenantRepository,
                              IServiceFactory<IOpenStackNetworkingProvider> networkingProviderFactory) {
        super(DeallocateStaticIpRequest.class, ActionType.RELEASE_IP);
        this.networkingProviderFactory = networkingProviderFactory;
        this.regionRepository = regionRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Boolean handlePayload(ActionType actionType, DeallocateStaticIpRequest request) {
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(request.getTenantName(), region.getId());
        IOpenStackNetworkingProvider networkingProvider = networkingProviderFactory.get(region);
        IVirtOpenStackNetworkService networkService = networkingProvider.networkingService();
        return networkService.releaseStaticIp(tenant, request.getIp());
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }
}
