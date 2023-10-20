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
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.util.UtilsReadableAssert;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.network.SdkVlanDescribeInfo;
import io.maestro3.sdk.v3.request.agent.ManageVLANRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OsDescribeVLANHandler extends AbstractM3ApiHandler<ManageVLANRequest, Set<SdkVlanDescribeInfo>> {

    private static final String ZONE_TYPE = "Zone";
    private static final String PERSONAL_PROJECT_TYPE = "Personal project";
    private static final String VLAN_TYPE = "VLAN";

    private IOpenStackVLANService openStackVLANService;
    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private OpenStackApiProvider apiProvider;

    @Autowired
    public OsDescribeVLANHandler(IOpenStackVLANService openStackVLANService, IOpenStackRegionRepository regionRepository,
                                 OpenStackApiProvider apiProvider, IOpenStackTenantRepository tenantRepository) {
        super(ManageVLANRequest.class, ActionType.DESCRIBE_VLAN);
        this.openStackVLANService = openStackVLANService;
        this.tenantRepository = tenantRepository;
        this.regionRepository = regionRepository;
        this.apiProvider = apiProvider;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public Set<SdkVlanDescribeInfo> handlePayload(ActionType actionType, ManageVLANRequest request) {
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(request.getTenantName(), region.getId());
        String regionId = region.getId();
        Set<String> networkIds = getNetworkIds(region, tenant);

        List<OpenStackVLAN> openStackVLANs = openStackVLANService.findByRegionAndTenantId(regionId, tenant.getId());
        Set<SdkVlanDescribeInfo> resultOutput = getResultOutput(openStackVLANs, region, networkIds);

        return CollectionUtils.isEmpty(resultOutput) ? new HashSet<>() : resultOutput;
    }

    private Set<String> getNetworkIds(OpenStackRegionConfig regionConfig, OpenStackTenant tenant) {
        List<Network> networks;
        try {
            networks = apiProvider.adminOpenStack(regionConfig).networking().networks().list();
        } catch (OSClientException e) {
            throw new ReadableAgentException("Error occurred during getting network", e);
        }
        UtilsReadableAssert.notEmpty(networks, "Networks in OpenStack not found.");
        return networks.stream()
            .map(Network::getId)
            .collect(Collectors.toSet());
    }


    private Set<SdkVlanDescribeInfo> getResultOutput(List<OpenStackVLAN> openStackVLANs, OpenStackRegionConfig zone, Set<String> networkIds) {
        Set<SdkVlanDescribeInfo> describes = new LinkedHashSet<>();
        String zoneNetworkId = zone.getNetworkingPolicy().getNetworkId();
        if (StringUtils.isNotBlank(zoneNetworkId)) {
            describes.add(new SdkVlanDescribeInfo(ZONE_TYPE, zone.getRegionAlias(), zoneNetworkId, networkIds.contains(zoneNetworkId)));
        }

        for (OpenStackVLAN vlan : openStackVLANs) {
            String openStackNetworkId = vlan.getOpenStackNetworkId();
            describes.add(new SdkVlanDescribeInfo(VLAN_TYPE, vlan.getName(), openStackNetworkId,
                networkIds.contains(openStackNetworkId)));
        }
        return describes;
    }
}
