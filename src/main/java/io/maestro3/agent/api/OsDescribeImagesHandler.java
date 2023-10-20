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
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.M3SdkVersion;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.image.SdkImage;
import io.maestro3.sdk.v3.request.instance.DescribeInstanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component("osDescribeImagesHandler")
public class OsDescribeImagesHandler implements IM3ApiHandler {

    protected final Logger LOG = LoggerFactory.getLogger(OsDescribeImagesHandler.class);

    private IOpenStackRegionRepository regionDbService;
    private TenantDbService tenantDbService;
    private OpenStackApiProvider openStackApiProvider;

    @Autowired
    public OsDescribeImagesHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                   OpenStackApiProvider openStackApiProvider) {
        this.regionDbService = regionDbService;
        this.tenantDbService = tenantDbService;
        this.openStackApiProvider = openStackApiProvider;
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        DescribeInstanceRequest request = M3ApiActionInverter.toDescribeInstancesRequest(action);

        String tenantName = request.getTenantName();
        String regionName = request.getRegion();

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(regionName);
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(tenantName, region.getId());

        List<Image> result = describeImage(region, tenant);

        List<SdkImage> sdkImages = result.stream()
                .map(image -> {
                    String imageId = image.getId();

                    SdkImage sdkImage = new SdkImage();
                    sdkImage.setImageId(imageId);
                    sdkImage.setTenant(tenantName);
                    sdkImage.setRegion(regionName);
                    sdkImage.setImageId(image.getId());
                    sdkImage.setCloud(SdkCloud.OPEN_STACK);
                    sdkImage.setName(image.getName());
                    sdkImage.setCreatedDate(image.getCreated().getTime());
                    sdkImage.setImageType("PRIVATE");
                    sdkImage.setImageState(image.getStatus().name());
                    sdkImage.setAlias(image.getName());

                    return sdkImage;
                })
                .collect(Collectors.toList());

        LOG.info("Sdk images : {}", sdkImages);
        return M3Result.success(action.getId(), sdkImages);
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    private List<Image> describeImage(OpenStackRegionConfig region, OpenStackTenant tenant) throws M3PrivateAgentException {
        try {
            return openStackApiProvider.openStack(tenant, region).images().image().listProject(tenant.getId());

        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public M3SdkVersion getSupportedVersion() {
        return M3SdkVersion.V3;
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.DESCRIBE_IMAGE));
    }
}
