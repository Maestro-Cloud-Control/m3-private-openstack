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
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.PlatformShapeMapping;
import io.maestro3.agent.model.base.PlatformType;
import io.maestro3.agent.model.compute.ImageStateMapping;
import io.maestro3.agent.model.compute.ImageVisibility;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.MachineImageDbService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.image.SdkImage;
import io.maestro3.sdk.v3.model.image.SdkImageState;
import io.maestro3.sdk.v3.request.image.CreateImageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component("osCreateImageHandler")
public class OsCreateImageHandler extends AbstractInstanceHandler implements IM3ApiHandler {

    private final MachineImageDbService machineImageDbService;

    @Autowired
    public OsCreateImageHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                MachineImageDbService machineImageDbService, ServerDbService serverDbService,
                                OpenStackApiProvider openStackApiProvider, @Qualifier("instanceLocker") Locker locker) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker);
        this.machineImageDbService = machineImageDbService;
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        CreateImageRequest request = M3ApiActionInverter.toCreateImageRequest(action);

        String regionName = request.getRegion();
        String tenantName = request.getTenantName();

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(regionName);
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(tenantName, region.getId());

        String imageName = request.getName();
        String serverName = request.getInstanceId();

        List<Server> servers = listServers(region, tenant);
        Server server = servers.stream()
                .filter(s -> serverName.equals(s.getName()))
                .findFirst()
                .orElse(null);
        LOG.info("Current server : {}", server);

        if (server == null) {
            throw new IllegalArgumentException(String.format("Server for region:%s and tenant:%s does not found", region.getNativeRegionName(), tenant.getNativeName()));
        }
        String serverId = server.getId();
        String imageId = createImage(region, tenant, serverId, imageName);

        OpenStackMachineImage currentServerMachineImage = machineImageDbService.findByNativeId(server.getImageId());

        int requiredStorageSizeGb = 0;
        double requiredMinRamSizeGb = 0;

        if (currentServerMachineImage != null) {
            Set<PlatformShapeMapping> imagePlatformMapping = region.getAdminProjectMeta().getAvailablePublicImagesPlatformMapping();
            PlatformShapeMapping platformShapeMapping = null;
            for (PlatformShapeMapping mapping : imagePlatformMapping) {
                if (mapping.getName().equals(currentServerMachineImage.getNativeName())){
                    platformShapeMapping = mapping;
                    break;
                }
            }

            if (platformShapeMapping != null) {
                requiredStorageSizeGb = platformShapeMapping.getMinStorageSizeGb();
                requiredMinRamSizeGb = platformShapeMapping.getMinMemoryGb();
            }

        }

        SdkImage sdkImage = new SdkImage();
        sdkImage.setImageId(imageId);
        sdkImage.setTenant(tenantName);
        sdkImage.setRegion(regionName);
        sdkImage.setCloud(SdkCloud.OPEN_STACK);
        sdkImage.setName(imageName);
        sdkImage.setDescription(request.getDescription());
        sdkImage.setCreatedDate(System.currentTimeMillis());
        sdkImage.setImageType(ImageVisibility.PRIVATE.getName());
        sdkImage.setImageState(SdkImageState.IN_PROGRESS.getName());
        sdkImage.setAlias(imageName);
        sdkImage.setOsType("o");
        sdkImage.setMinStorageSizeGb(requiredStorageSizeGb);
        sdkImage.setMinMemoryGb(requiredMinRamSizeGb);

        OpenStackMachineImage machineImage = new OpenStackMachineImage();
        machineImage.setNativeId(imageId);
        machineImage.setNativeName(imageName);
        machineImage.setNameAlias(imageName);
        machineImage.setRegionId(region.getId());
        machineImage.setPlatformType(PlatformType.OTHER);
        machineImage.setImageStatus(ImageStateMapping.IN_PROGRESS.getName());
        machineImage.setImageVisibility(ImageVisibility.PRIVATE);
        machineImage.setRequiredMinStorageSizeGb(requiredStorageSizeGb);
        machineImage.setRequiredMinMemoryGb(requiredMinRamSizeGb);

        machineImageDbService.insert(Collections.singletonList(machineImage));

        LOG.info("Result : {}", sdkImage);
        return M3Result.success(action.getId(), Collections.singletonList(sdkImage));
    }

    private String createImage(OpenStackRegionConfig region, OpenStackTenant tenant,
                               String serverId, String name) throws M3PrivateAgentException {
        try {
            return openStackApiProvider.openStack(tenant, region).compute().images().create(serverId, name);

        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    private List<Server> listServers(OpenStackRegionConfig regionConfig,
                                     OpenStackTenant tenantConfig) throws M3PrivateAgentException {
        try {
            return openStackApiProvider.openStack(tenantConfig, regionConfig)
                    .compute().servers().list();
        } catch (OSClientException e) {
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.CREATE_IMAGE));
    }
}
