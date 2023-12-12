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
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.compute.bean.VncConsole;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackMoveToVlanService;
import io.maestro3.agent.service.IVLANService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.UtilsReadableAssert;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.network.SdkVlanResponse;
import io.maestro3.sdk.v3.request.agent.MoveToDmzRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OsMoveToDmzHandler extends AbstractM3ApiHandler<MoveToDmzRequest, SdkVlanResponse> {

    // for setup VLAN name to instances, turned back to default VLAN
    private static final String DEFAULT_VLAN_NAME = "Server Network";

    private IVLANService vlanService;
    private IOpenStackMoveToVlanService openStackMoveToVlanService;
    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private OpenStackApiProvider apiProvider;
    private ServerDbService serverDbService;

    @Autowired
    public OsMoveToDmzHandler(IVLANService vlanService, IOpenStackMoveToVlanService openStackMoveToVlanService,
                              IOpenStackRegionRepository regionRepository, IOpenStackTenantRepository tenantRepository,
                              OpenStackApiProvider apiProvider, ServerDbService serverDbService) {
        super(MoveToDmzRequest.class, ActionType.MOVE_VM_TO_DMZ);
        this.vlanService = vlanService;
        this.openStackMoveToVlanService = openStackMoveToVlanService;
        this.regionRepository = regionRepository;
        this.tenantRepository = tenantRepository;
        this.apiProvider = apiProvider;
        this.serverDbService = serverDbService;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public SdkVlanResponse handlePayload(ActionType actionType, MoveToDmzRequest request) {

        String regionName = request.getRegion();
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(request.getRegion());
        String tenantName = request.getTenantName();
        String regionId = region.getId();

        OpenStackTenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(tenantName, regionId);
        String instanceId = request.getInstanceId();
        String tenantId = tenant.getId();

        OpenStackServerConfig instance = serverDbService.findServer(regionId, tenantId, instanceId);

//        if (instance.getMoveToProjectState() == MoveToProjectState.MOVING) {
//            throw new UtilsException("Instance is moving to another project: " + instance.getOperationalSearchId());
//        }
//        UtilsReadableAssert.isFalse(InstanceUtils.isInstanceIsolated(instance), String.format("Instance %s is isolated", instance.getOperationalSearchId()));
        String ipAddress = request.getIpAddress();
        boolean isBackToInternal = request.getBackToInternal();
        String vlanName = request.getVlanName();

        IOpenStackApi openStackApi = apiProvider.adminOpenStack(region);

        String targetNetworkId;
        if (isBackToInternal) {
            targetNetworkId = tenant.getNetworkId();
            UtilsReadableAssert.hasLength(targetNetworkId,
                "Project default network ID is absent. Please reconfigure project.");
            vlanName = DEFAULT_VLAN_NAME;
        } else {
            VLAN vlan = getVlan(regionId, tenantId, vlanName);
            UtilsReadableAssert.isTrue(vlan instanceof OpenStackVLAN,
                "Invalid type of VLAN was found for region " + regionName);
            OpenStackVLAN openStackVLAN = (OpenStackVLAN) vlan;
            targetNetworkId = openStackVLAN.getOpenStackNetworkId();
        }

        try {
            openStackMoveToVlanService.updateInstanceVLAN(tenant, region, instance, vlanName, targetNetworkId, ipAddress);
        } catch (Exception e) {
            LOG.error("Failed to update instance vlans", e);
            throw new ReadableAgentException("Failed to update instance vlans");
        }

        String successMsg = String.format("OpenStack server %s (%s) has been attached to network %s (%s) with new IP.",
            instance.getNameAlias(), instance.getNativeId(), vlanName, targetNetworkId);
        SdkVlanResponse response = new SdkVlanResponse();
        response.addSuccessMessage(successMsg);

        VncConsole vncConsole = null;
        try {
            vncConsole = openStackApi.compute().servers().getVncConsole(instance.getNativeId());
        } catch (OSClientException e) {
            LOG.error("Failed to receiving vnc console");
            response.addFailMessage("Failed to receiving vnc console");
        }
        if (vncConsole != null) {
            response.addSuccessMessage("VNC console to instance: " + vncConsole.getUrl() + "\n");
        }
        return response;
    }

    private VLAN getVlan(String regionId, String tenantId, String vlanName) {
        VLAN vlan = vlanService.getVLANByName(vlanName, tenantId, regionId);
        if (vlan == null) {
            vlan = vlanService.getRegionVLANByName(vlanName, regionId);
        }
        UtilsReadableAssert.notNull(vlan, "No VLAN was found.");
        return vlan;
    }
}
