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
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.api.networking.bean.Router;
import io.maestro3.agent.openstack.api.networking.request.RemoveRouterInterface;
import io.maestro3.agent.openstack.api.networking.request.UpdateRouter;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.util.OpenStackNetworkUtils;
import io.maestro3.agent.util.UtilsReadableAssert;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.network.SdkVlanResponse;
import io.maestro3.sdk.v3.request.agent.DeactivateVLANRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OsDeactivateVLANHandler extends AbstractM3ApiHandler<DeactivateVLANRequest, SdkVlanResponse> {

    private static final Logger LOG = LogManager.getLogger(OsDeactivateVLANHandler.class);

    private IOpenStackRegionRepository regionRepository;
    private IOpenStackTenantRepository tenantRepository;
    private OpenStackApiProvider apiProvider;
    private IOpenStackVLANService vlanService;

    @Autowired
    public OsDeactivateVLANHandler(IOpenStackRegionRepository regionRepository, IOpenStackVLANService vlanService,
                                   IOpenStackTenantRepository tenantRepository, OpenStackApiProvider apiProvider) {
        super(DeactivateVLANRequest.class, ActionType.DEACTIVATE_VLAN);
        this.regionRepository = regionRepository;
        this.vlanService = vlanService;
        this.apiProvider = apiProvider;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public SdkVlanResponse handlePayload(ActionType action, DeactivateVLANRequest request) {
        OpenStackRegionConfig region = regionRepository.findByAliasInCloud(request.getRegion());
        IOpenStackApi openStackApi = apiProvider.adminOpenStack(region);
        Network openStackNetwork;

        try {
            List<Network> networks = openStackApi.networking().networks().listByName(request.getVlanName());
            if (CollectionUtils.isEmpty(networks)) {
                throw new ReadableAgentException(String.format("Network %s not found in the region %s",
                    request.getVlanName(), request.getRegion()));
            }
            openStackNetwork = networks.get(0);
        } catch (OSClientException e) {
            LOG.error("Failed to deactivate network", e);
            throw new ReadableAgentException("Failed to deactivate network.");
        }

        OpenStackVLAN openStackVLAN;
        String tenantName = request.getTenantName();
        if (StringUtils.isNotBlank(tenantName)) {
            OpenStackTenant tenant = tenantRepository.findByTenantAliasAndRegionIdInCloud(tenantName, region.getId());
            if (tenant == null) {
                throw new ReadableAgentException(String.format("Active tenant %s not found in region %s", tenantName, region.getRegionAlias()));
            }
            openStackVLAN = getProjectVLAN(region, tenant, openStackNetwork.getName());
            if (!request.getForce()) {
                vlanService.delete(openStackVLAN);
            } else {
                if (!openStackVLAN.isSdn()) {
                    throw new ReadableAgentException("Only SDN could be force deleted");
                }
                assertNotDefaultProjectVlan(tenant, openStackNetwork.getId());
                try {
                    cleanUpSDNRelatedConfiguration(openStackNetwork, openStackApi);
                } catch (OSClientException e) {
                    LOG.error("Failed to clean up sdn related configuration", e);
                    throw new ReadableAgentException("Failed to clean up sdn related configuration.");
                }
            }

        } else {
            openStackVLAN = getZoneVLAN(region, openStackNetwork.getName());
            vlanService.delete(openStackVLAN);
        }
        SdkVlanResponse response = new SdkVlanResponse();
        response.addSuccessMessage(String.format("VLAN %s was successfully deactivated", openStackNetwork.getName()));
        return response;
    }

    private void assertNotDefaultProjectVlan(OpenStackTenant tenant, String osNetworkId) {
        UtilsReadableAssert.isFalse(tenant.getNetworkId().equals(osNetworkId),
            "Specified VLAN is default project network. Please change default.");
    }

    private OpenStackVLAN getProjectVLAN(OpenStackRegionConfig region, OpenStackTenant tenant, String vlanName) {
        VLAN vlanByName = vlanService.getVLANByName(vlanName, tenant.getId(), region.getId());
        UtilsReadableAssert.notNull(vlanByName, String.format("Tenant VLAN %s not found for tenant %s in region %s",
            vlanName, tenant.getTenantAlias(), region.getRegionAlias()));

        return (OpenStackVLAN) vlanByName;
    }

    private OpenStackVLAN getZoneVLAN(OpenStackRegionConfig region, String vlanName) {
        VLAN zoneVLANByName = vlanService.getRegionVLANByName(vlanName, region.getId());
        UtilsReadableAssert.notNull(zoneVLANByName, String.format("Region VLAN %s not found for region %s", vlanName,
            region.getRegionAlias()));

        return (OpenStackVLAN) zoneVLANByName;
    }

    private void cleanUpSDNRelatedConfiguration(Network openStackNetwork, IOpenStackApi openStackApi) throws OSClientException {
        String routerName = OpenStackNetworkUtils.generateRouterName(openStackNetwork.getName());
        List<Router> routers = openStackApi.networking().routers().listByName(routerName);
        UtilsReadableAssert.notEmpty(routers, "Failed to get network router");
        Router router = routers.get(0);

        // unset external gateway
        openStackApi.networking().routers().update(router.getId(), UpdateRouter.builder().get());
        LOG.debug("External gateway was unset");
        // remove subnet
        List<Port> ports = openStackApi.networking().ports().listByDeviceId(router.getId());
        for (Port port : ports) {
            RemoveRouterInterface removeRouterInterfaceConfig = RemoveRouterInterface.builder().port(port.getId()).get();
            openStackApi.networking().routers().removeInterface(router.getId(), removeRouterInterfaceConfig);
        }
        LOG.debug("Router interfaces were removed");
        // delete router
        openStackApi.networking().routers().delete(router.getId());
        LOG.debug("Router was deleted");
        // delete network
        openStackApi.networking().networks().delete(openStackNetwork.getId());
    }
}
