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
import io.maestro3.agent.model.network.NetworkingPolicy;
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.util.UtilsReadableAssert;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.network.SdkVlanUpdateInfo;
import io.maestro3.sdk.v3.request.agent.ManageVLANRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OsUpdateVLANHandler extends AbstractM3ApiHandler<ManageVLANRequest, List<SdkVlanUpdateInfo>> {

    private static final String ZONE_TYPE = "Zone";
    private static final String PERSONAL_PROJECT_TYPE = "Personal project";
    private static final String VLAN_TYPE = "VLAN";

    private IOpenStackVLANService openStackVLANService;
    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private OpenStackApiProvider apiProvider;

    @Autowired
    public OsUpdateVLANHandler(IOpenStackVLANService openStackVLANService, IOpenStackRegionRepository regionRepository,
                               IOpenStackTenantRepository tenantRepository, OpenStackApiProvider apiProvider) {
        super(ManageVLANRequest.class, ActionType.UPDATE_VLAN);
        this.openStackVLANService = openStackVLANService;
        this.regionRepository = regionRepository;
        this.tenantRepository = tenantRepository;
        this.apiProvider = apiProvider;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public List<SdkVlanUpdateInfo> handlePayload(ActionType actionType, ManageVLANRequest request) {
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(request.getRegion());
        Set<String> networkIds = getNetworkIds(region);

        String newId = request.getNewId();
        UtilsReadableAssert.isTrue(networkIds.contains(newId), "New id is not found in OpenStack networks.");
        List<SdkVlanUpdateInfo> infos = updateNetworks(region, request.getOldId(), newId);
        return CollectionUtils.isEmpty(infos) ? Collections.emptyList() : infos;
    }

    private Set<String> getNetworkIds(OpenStackRegionConfig regionConfig) {
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

    private List<SdkVlanUpdateInfo> updateNetworks(OpenStackRegionConfig region, String oldNetworkId, String newNetworkId) {
        Set<SdkVlanUpdateInfo> updateZoneVLANsResult = updateZoneVLANs(region, oldNetworkId, newNetworkId);
        tenantRepository.updateProjectsNetworkId(region.getId(), oldNetworkId, newNetworkId);
        Set<SdkVlanUpdateInfo> updateVLANsResult = updateVLANs(region, oldNetworkId, newNetworkId);
        return Stream.of(updateZoneVLANsResult, updateVLANsResult)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private Set<SdkVlanUpdateInfo> updateVLANs(OpenStackRegionConfig region, String oldNetworkId, String newNetworkId) {
        List<OpenStackVLAN> openStackVLANs = openStackVLANService.findByRegionId(region.getId());
        openStackVLANService.updateByOpenStackNetworkId(region.getId(), oldNetworkId, newNetworkId);
        return openStackVLANs.stream()
            .filter(vlan -> oldNetworkId.equals(vlan.getOpenStackNetworkId()))
            .map(vlan -> new SdkVlanUpdateInfo(VLAN_TYPE, vlan.getName(), newNetworkId))
            .collect(Collectors.toSet());
    }

    private Set<SdkVlanUpdateInfo> updateZoneVLANs(OpenStackRegionConfig region, String oldNetworkId, String newNetworkId) {
        Set<SdkVlanUpdateInfo> infos = new LinkedHashSet<>();
        boolean isChanged = false;

        NetworkingPolicy networkingPolicy = region.getNetworkingPolicy();
        String policyNetworkId = networkingPolicy.getNetworkId();
        if (policyNetworkId != null && policyNetworkId.equals(oldNetworkId)) {
            networkingPolicy.setNetworkId(newNetworkId);
            isChanged = true;
            infos.add(new SdkVlanUpdateInfo(ZONE_TYPE, region.getRegionAlias(), newNetworkId));
        }

        if (isChanged) {
            regionRepository.save(region);
        }
        return infos;
    }
}
