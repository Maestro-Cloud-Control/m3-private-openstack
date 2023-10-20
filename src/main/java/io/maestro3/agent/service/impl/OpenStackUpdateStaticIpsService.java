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

package io.maestro3.agent.service.impl;

import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IOpenStackUpdateStaticIpsService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.ConversionUtils;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


abstract class OpenStackUpdateStaticIpsService implements IOpenStackUpdateStaticIpsService {

    private static final Logger LOG = LogManager.getLogger(OpenStackUpdateStaticIpsService.class);

    protected final IOpenStackClientProvider clientProvider;
    protected final ServerDbService instanceService;
    protected final OpenStackRegionConfig zone;
    private final IOpenStackStaticIpService staticIpService;

    public OpenStackUpdateStaticIpsService(IOpenStackClientProvider clientProvider, ServerDbService instanceService,
                                           IOpenStackStaticIpService staticIpService, OpenStackRegionConfig zone) {
        this.clientProvider = clientProvider;
        this.zone = zone;
        this.instanceService = instanceService;
        this.staticIpService = staticIpService;
    }

    @Override
    public void updateStaticIps(OpenStackTenant project) throws OSClientException {
        Collection<? extends OpenStackStaticIpAddress> incomingIps = getIncomingIps(project);
        List<OpenStackStaticIpAddress> existingIps = findStaticIps(project, null);

        updateStaticIps(project, incomingIps, existingIps);
    }

    @Override
    public void updateStaticIp(OpenStackTenant project, OpenStackStaticIpAddress incoming, OpenStackStaticIpAddress existing) {
        Collection<OpenStackStaticIpAddress> incomingIps = CollectionUtils.singletonOrEmptyList(incoming);
        Collection<OpenStackStaticIpAddress> existingIps = CollectionUtils.singletonOrEmptyList(existing);
        updateStaticIps(project, incomingIps, existingIps);
    }

    protected abstract Collection<? extends OpenStackStaticIpAddress> getIncomingIps(OpenStackTenant project) throws OSClientException;

    private void updateStaticIps(OpenStackTenant project, Collection<? extends OpenStackStaticIpAddress> incomingIps, Collection<OpenStackStaticIpAddress> existingIps) {
        Map<String, OpenStackStaticIpAddress> incomingIpsMap = mapOnUniqueId(incomingIps);
        if (CollectionUtils.isEmpty(existingIps) && MapUtils.isEmpty(incomingIpsMap)) {
            return; // as nothing to update
        }

        if (CollectionUtils.isNotEmpty(existingIps)) {
            for (OpenStackStaticIpAddress existingIp : existingIps) {
                if (!existingIp.isFixed()) {
                    OpenStackStaticIpAddress incomingIp = incomingIpsMap.remove(existingIp.getUniqueNotEmptyId());
                    processSingleIpAddress(project, incomingIp, existingIp);
                }
            }
        }
        for (OpenStackStaticIpAddress incomingIp : incomingIpsMap.values()) {
            addNewStaticIp(project, incomingIp);
        }
    }

    private void processSingleIpAddress(OpenStackTenant project, OpenStackStaticIpAddress incomingIp, OpenStackStaticIpAddress existingIp) {
        if (incomingIp != null) {
            updateStaticIpAddress(project, existingIp, incomingIp);
        } else {
            deleteStaticIp(existingIp);
        }
    }

    private void deleteStaticIp(OpenStackStaticIpAddress ipAddress) {
        staticIpService.delete(ipAddress);
    }

    private void updateStaticIpAddress(OpenStackTenant project, OpenStackStaticIpAddress existing, OpenStackStaticIpAddress incoming) {
        String currentPortId = incoming.getPortId();
        if (currentPortId != null) {
            injectOwner(project, incoming);
        }

        existing.setTenantId(incoming.getTenantId());
        existing.setZoneId(incoming.getZoneId());
        existing.setTenantName(incoming.getTenantName());
        existing.setRegionName(incoming.getRegionName());
        existing.setIpAddress(incoming.getIpAddress());
        existing.setInstanceId(incoming.getInstanceId());
        existing.setPublic(incoming.isPublic());
        existing.setPortId(incoming.getPortId());
        existing.setFixedIp(incoming.getFixedIp());

        handleStaticIpState(existing, incoming);
        staticIpService.update(existing);
    }

    private void handleStaticIpState(OpenStackStaticIpAddress existing, OpenStackStaticIpAddress incoming) {
        IPState incomingState = incoming.getIpState();
        if (incomingState != null) {
            existing.setIpState(incomingState);
        }
    }

    private Map<String, OpenStackStaticIpAddress> mapOnUniqueId(Collection<? extends OpenStackStaticIpAddress> staticIpAddresses) {
        Map<String, OpenStackStaticIpAddress> result = new HashMap<>();
        if (CollectionUtils.isEmpty(staticIpAddresses)) {
            return result;
        }
        for (OpenStackStaticIpAddress ip : staticIpAddresses) {
            result.put(ip.getUniqueNotEmptyId(), ip);
        }
        return result;
    }

    private void addNewStaticIp(OpenStackTenant project, OpenStackStaticIpAddress staticIp) {
        if (staticIp.getPortId() != null) {
            injectOwner(project, staticIp);
        }
        staticIpService.save(staticIp);
    }

    private List<OpenStackStaticIpAddress> findStaticIps(OpenStackTenant project, List<String> instanceIds) {
        Collection<OpenStackStaticIpAddress> fromDB = ConversionUtils.castCollection(
            staticIpService.findStaticIpAddresses(zone.getId(), project.getId(), null, null, instanceIds),
            OpenStackStaticIpAddress.class);
        return CollectionUtils.isEmpty(fromDB) ? null : new ArrayList<>(fromDB);
    }

    private void injectOwner(OpenStackTenant project, OpenStackStaticIpAddress staticIpAddress) {
        IOpenStackClient client = clientProvider.getClient(zone, project);
        Assert.hasText(staticIpAddress.getPortId(), "staticIpAddress.port can not be blank");
        try {
            Port port = client.getPort(staticIpAddress.getPortId());
            if (port == null || StringUtils.isBlank(port.getDeviceId())) {
                return;
            }
            OpenStackServerConfig owner = instanceService.findServerByNativeId(zone.getId(), project.getId(), port.getDeviceId());
            if (owner == null) {
                return;
            }
            staticIpAddress.setInstanceId(owner.getNativeId());
        } catch (OSClientException e) {
            LOG.error("Failed to detect static ip owner. IP={} -> {}", staticIpAddress, e.getMessage());
        }
    }
}
