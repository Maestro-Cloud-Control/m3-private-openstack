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
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.image.SdkImage;
import io.maestro3.sdk.v3.model.image.SdkImageState;
import io.maestro3.sdk.v3.request.image.DeleteImageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Component("osDeleteImageHandler")
public class OsDeleteImageHandler extends AbstractInstanceHandler implements IM3ApiHandler {

    @Autowired
    public OsDeleteImageHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                ServerDbService serverDbService,
                                OpenStackApiProvider openStackApiProvider, @Qualifier("instanceLocker") Locker locker) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker);
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        DeleteImageRequest request = M3ApiActionInverter.toDeleteImageRequest(action);

        String tenantName = request.getTenantName();
        String regionName = request.getRegion();
        String imageId = request.getImageId();

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(regionName);
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(tenantName, region.getId());


        Image image = getImage(region, tenant, imageId);
        deleteImage(region, tenant, imageId);

        SdkImage sdkImage = new SdkImage();
        sdkImage.setRegion(regionName);
        sdkImage.setTenant(tenantName);
        sdkImage.setImageId(imageId);
        sdkImage.setRegionId(region.getId());
        sdkImage.setCloud(SdkCloud.OPEN_STACK);
        sdkImage.setImageState(SdkImageState.DELETING.getName());
        sdkImage.setCreatedDate(image.getCreated().getTime());
        sdkImage.setImageType("PRIVATE");
        sdkImage.setDescription(image.getDescription());
        sdkImage.setAlias(image.getName());
        sdkImage.setName(image.getName());

        LOG.info("Sdk image : {}", sdkImage);
        return M3Result.success(action.getId(), Collections.singletonList(sdkImage));
    }

    private Image getImage(OpenStackRegionConfig region, OpenStackTenant tenant, String imageId) throws M3PrivateAgentException {
        try {
            return openStackApiProvider.openStack(tenant, region).images().image().get(imageId);
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    private void deleteImage(OpenStackRegionConfig region, OpenStackTenant tenant, String imageId) throws M3PrivateAgentException {
        try {
            openStackApiProvider.openStack(tenant, region).images().image().delete(imageId);

        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.DELETE_IMAGE));
    }
}
