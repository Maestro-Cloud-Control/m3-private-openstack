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

import io.maestro3.agent.api.handler.IM3ApiHandler;
import io.maestro3.agent.converter.M3ApiActionInverter;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.MachineImageDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.M3SdkVersion;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.resource.SdkOpenStackResource;
import io.maestro3.sdk.v3.model.resource.SdkResource;
import io.maestro3.sdk.v3.request.resource.ResourceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;


@Component("OsListResourcesHandler")
public class OsListResourcesHandler implements IM3ApiHandler {
    private IOpenStackRegionRepository regionDbService;
    private TenantDbService tenantDbService;
    private MachineImageDbService imageDbService;

    @Autowired
    public OsListResourcesHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                  MachineImageDbService imageDbService) {
        this.regionDbService = regionDbService;
        this.imageDbService = imageDbService;
        this.tenantDbService = tenantDbService;
    }

    @Override
    public M3RawResult handle(M3ApiAction apiRequest) {
        ResourceRequest request = M3ApiActionInverter.toResourceRequest(apiRequest);
        String region = request.getRegion();
        String tenantName = request.getTenantName();
        OpenStackRegionConfig osRegion = regionDbService.findByAliasInCloud(region);
        if (osRegion == null){
            throw new M3PrivateAgentException("Region is not found by name " + region);
        }
        OpenStackTenant osTenant = tenantDbService.findOpenStackTenantByNameAndRegion(tenantName, osRegion.getId());
        if (osTenant == null){
            throw new M3PrivateAgentException("Region is not found by name " + region);
        }
        List<SdkResource> resources = new ArrayList<>();
        Collection<OpenStackMachineImage> images = imageDbService.findByRegionId(osRegion.getId());
        fillResources(region, tenantName, resources, images, OpenStackMachineImage::getNativeId, ApiConstants.MACHINE_IMAGES);
        return M3Result.success(apiRequest.getId(), resources);
    }

    private <T> void fillResources(String region, String tenant, List<SdkResource> resources,
                                   Collection<T> from, Function<T, String> idExtractor, String type) {
        for (T element : from) {
            resources.add(new SdkOpenStackResource()
                .setId(idExtractor.apply(element))
                .setCloud(SdkCloud.OPEN_STACK)
                .setTenant(tenant)
                .setRegion(region)
                .setService(ApiConstants.COMPUTE_SERVICE)
                .setType(type));
        }
    }


    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public M3SdkVersion getSupportedVersion() {
        return M3SdkVersion.V3;
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.LIST_RESOURCES));
    }
}
