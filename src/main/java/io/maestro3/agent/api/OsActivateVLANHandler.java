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
import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.NovaSubnet;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.filter.impl.SubnetApiFilter;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IAdminVLANService;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.network.SdkVlanResponse;
import io.maestro3.sdk.v3.request.agent.ActivateVLANRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OsActivateVLANHandler extends AbstractM3ApiHandler<ActivateVLANRequest, SdkVlanResponse> {

    private IAdminVLANService adminVLANService;
    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private OpenStackApiProvider apiProvider;

    @Autowired
    public OsActivateVLANHandler(IAdminVLANService adminVLANService,
                                 IOpenStackRegionRepository regionRepository, IOpenStackTenantRepository tenantRepository,
                                 OpenStackApiProvider apiProvider) {
        super(ActivateVLANRequest.class, ActionType.ACTIVATE_VLAN);
        this.adminVLANService = adminVLANService;
        this.regionRepository = regionRepository;
        this.tenantRepository = tenantRepository;
        this.apiProvider = apiProvider;
    }

    @Override
    public SdkVlanResponse handlePayload(ActionType actionType, ActivateVLANRequest request) {
        OpenStackRegionConfig openStackZone = regionRepository.findByAliasInCloud(request.getRegion());
        IOpenStackApi openStackApi = apiProvider.adminOpenStack(openStackZone);
        String targetNetworkId;
        Set<String> cidrs = null;

        try {
            List<Network> networks = openStackApi.networking().networks().listByName(request.getVlanName());
            if (CollectionUtils.isEmpty(networks)) {
                throw new ReadableAgentException("Not found networks in specified region.");
            }
            targetNetworkId = networks.get(0).getId();
            List<NovaSubnet> subnets = openStackApi.networking().subnets().list(new SubnetApiFilter().inNetwork(targetNetworkId));
            if (CollectionUtils.isNotEmpty(subnets)) {
                cidrs = subnets.stream()
                    .map(NovaSubnet::getCidr)
                    .collect(Collectors.toSet());
            }
        } catch (OSClientException e) {
            LOG.error("Failed to activate network", e);
            throw new ReadableAgentException("Failed to activate network.");
        }

        if (CollectionUtils.isNotEmpty(request.getTenantNames())) {
            return activateVLANsForProjects(request, openStackZone, targetNetworkId, cidrs);
        } else {
            return activateVLANForZone(request, openStackZone, targetNetworkId, cidrs);
        }
    }

    private SdkVlanResponse activateVLANForZone(ActivateVLANRequest params, OpenStackRegionConfig region,
                                             String targetNetworkId, Set<String> cidrs) {
        String regionId = region.getId();
        String vlanName = params.getVlanName();
        String regionName = params.getRegion();
        SdkVlanResponse response = new SdkVlanResponse();
        VLAN vlan = adminVLANService.getRegionVLANByName(vlanName, regionId);
        if (vlan != null) {
            response.addSuccessMessage("VLAN with such name has already been activated for region " + regionName);
            return response;
        }
        OpenStackVLAN openStackZoneVLAN = constructOpenStackVLAN(params, regionId, targetNetworkId, null, cidrs);
        adminVLANService.addRegionVLAN(openStackZoneVLAN, region);
        response.addSuccessMessage(String.format("VLAN with name: %s and ID: %s was added for region %s.",
            vlanName, targetNetworkId, regionName));
        return response;
    }

    private SdkVlanResponse activateVLANsForProjects(ActivateVLANRequest params, OpenStackRegionConfig region,
                                                  String targetNetworkId, Set<String> cidrs) {
        String regionId = region.getId();
        String vlanName = params.getVlanName();
        String regionName = params.getRegion();
        SdkVlanResponse response = new SdkVlanResponse();
        for (String tenantName : params.getTenantNames()) {
            OpenStackTenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(tenantName, regionId);
            if (tenant == null) {
                response.addFailMessage(String.format("Tenant %s for region %s not found.",
                    tenantName, regionName));
                continue;
            }

            String tenantId = tenant.getId();
            VLAN vlan = adminVLANService.getTenantVLANByName(vlanName, tenantId, regionId);
            if (vlan != null) {
                response.addSuccessMessage(String.format("VLAN with such name has already been activated for tenant %s in region %s.", tenantName, regionName));
                continue;
            }
            OpenStackVLAN openStackVLAN = constructOpenStackVLAN(params, regionId, targetNetworkId, tenantId, cidrs);
            adminVLANService.addTenantVLAN(openStackVLAN, region, tenant);
            response.addSuccessMessage(String.format("VLAN with name: %s and ID: %s was added for tenant %s in region %s.",
                vlanName, targetNetworkId, tenantName, regionName));
        }
        return response;
    }

    private OpenStackVLAN constructOpenStackVLAN(ActivateVLANRequest params, String regionId, String targetNetworkId,
                                                 String tenantId, Set<String> cidrs) {
        String vlanName = params.getVlanName();
        OpenStackVLAN openStackVLAN = new OpenStackVLAN();
        openStackVLAN.setOpenStackNetworkId(targetNetworkId);
        openStackVLAN.setRegionId(regionId);
        openStackVLAN.setTenantId(tenantId);
        openStackVLAN.setDescription(params.getDescription());
        openStackVLAN.setName(vlanName);
        openStackVLAN.setCidrs(cidrs);
        openStackVLAN.setDmz(params.getDmz());
        openStackVLAN.setOperationalSearchId(vlanName.toLowerCase());
        openStackVLAN.setSecurityGroupDisabled(params.getSecurityGroupDisabled());
        return openStackVLAN;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }
}
