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
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackMoveToVlanService;
import io.maestro3.agent.service.IStaticIpService;
import io.maestro3.agent.service.IVLANService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.UtilsReadableAssert;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.network.SdkVlanResponse;
import io.maestro3.sdk.v3.request.agent.MoveToDmzRequest;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OsMoveInstanceToVLANHandler extends AbstractM3ApiHandler<MoveToDmzRequest, SdkVlanResponse> {

    private IVLANService vlanService;
    private IOpenStackMoveToVlanService openStackMoveToVlanService;
    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private OpenStackApiProvider apiProvider;
    private ServerDbService serverDbService;
    private IStaticIpService staticIpService;

    @Autowired
    public OsMoveInstanceToVLANHandler(IVLANService vlanService, IOpenStackMoveToVlanService openStackMoveToVlanService,
                                       IOpenStackRegionRepository regionRepository, IOpenStackTenantRepository tenantRepository,
                                       OpenStackApiProvider apiProvider, ServerDbService serverDbService, IStaticIpService staticIpService) {
        super(MoveToDmzRequest.class, ActionType.MOVE_VM_TO_VLAN);
        this.vlanService = vlanService;
        this.openStackMoveToVlanService = openStackMoveToVlanService;
        this.regionRepository = regionRepository;
        this.tenantRepository = tenantRepository;
        this.apiProvider = apiProvider;
        this.serverDbService = serverDbService;
        this.staticIpService = staticIpService;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public SdkVlanResponse handlePayload(ActionType type, MoveToDmzRequest request) {
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(request.getRegion());
        String tenantName = request.getTenantName();
        String regionId = region.getId();

        OpenStackTenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(tenantName, regionId);
        String instanceId = request.getInstanceId();
        String tenantId = tenant.getId();

        OpenStackServerConfig instance = serverDbService.findServer(regionId, tenantId, instanceId);

        VLAN vlan = vlanService.getVLANByName(request.getVlanName(), tenant.getId(), region.getId());
        if (!(vlan instanceof OpenStackVLAN)) {
            throw new ReadableAgentException("No VLAN was found.");
        }
        OpenStackVLAN openStackVLAN = (OpenStackVLAN) vlan;
        assertIpOperationsAllowed(region, instance, null);
        UtilsReadableAssert.isTrue(instance.getState() == ServerStateEnum.STOPPED,
            "Instance should be in stopped state");

        if (vlan.isDmz()) {
            throw new ReadableAgentException("Can not move instance to DMZ VLAN.");
        }
        if (request.getVlanName().equals(instance.getNetworkInterfaceInfo().getVlanName())) {
            throw new ReadableAgentException(String.format("Instance is already on %s VLAN", request.getVlanName()));
        }

        List<StaticIpAddress> staticIpAddresses = staticIpService.findStaticIpAddressByInstanceId(region.getId(), tenant.getId(), instance.getNativeId());
        if (CollectionUtils.isNotEmpty(staticIpAddresses)) {
            throw new ReadableAgentException("Instance can not has static IP");
        }

        String targetNetworkId = openStackVLAN.getOpenStackNetworkId();
        try {
            openStackMoveToVlanService.updateInstanceVLAN(tenant, region, instance, request.getVlanName(), targetNetworkId, null);
        } catch (Exception e) {
            String err = String.format("Failed to update instance vlan. Reason: %s", e.getMessage());
            LOG.error(err, e);
            throw new ReadableAgentException(err);
        }
        SdkVlanResponse response = new SdkVlanResponse();
        response.addSuccessMessage("Instance was successfully moved to new vlan");
        return response;
    }

    private void assertIpOperationsAllowed(OpenStackRegionConfig region, OpenStackServerConfig instance, OpenStackStaticIpAddress staticIpAddress) {
        if (staticIpAddress != null && staticIpAddress.isFixed()) {
            // Always allow for fixed IP addresses because they are fake
            return;
        }
        if (instance != null) {
            DateTime lastIpOperationDateTime = instance.getLastIpOperationDate() != null ? new DateTime(instance.getLastIpOperationDate()) : null;
            int allowedIpOperationsMinutes = region.getAllowedIpOperationsMinutes() > 0 ? region.getAllowedIpOperationsMinutes() : 2;
            if (lastIpOperationDateTime != null && lastIpOperationDateTime.plusMinutes(allowedIpOperationsMinutes).isAfter(DateTime.now())) {
                throw new ReadableAgentException("IP operations are not allowed now.");
            }
        }
    }
}
