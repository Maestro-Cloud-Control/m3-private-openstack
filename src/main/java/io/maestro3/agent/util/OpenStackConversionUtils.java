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

package io.maestro3.agent.util;

import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.OpenStackFloatingIp;
import io.maestro3.agent.model.network.impl.ip.OpenStackPort;
import io.maestro3.agent.model.network.impl.vlan.OpenStackSubnet;
import io.maestro3.agent.model.network.impl.vlan.SubnetModel;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.networking.bean.FloatingIp;
import io.maestro3.agent.openstack.api.networking.bean.NovaSubnet;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;


public class OpenStackConversionUtils {

    public static List<OpenStackFloatingIp> toOpenStackFloatingIps(List<FloatingIp> floatingIps, OpenStackTenant project, OpenStackRegionConfig zone) {
        Assert.notNull(project, "project cannot be null.");
        Assert.notNull(zone, "zone cannot be null.");

        List<OpenStackFloatingIp> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(floatingIps)) {
            return result;
        }
        for (FloatingIp ip : floatingIps) {
            result.add(toOpenStackFloatingIp(ip, project, zone));
        }
        return result;
    }

    public static OpenStackFloatingIp toOpenStackFloatingIp(FloatingIp floatingIp, OpenStackTenant project, OpenStackRegionConfig zone) {
        Assert.notNull(project, "project cannot be null.");
        Assert.notNull(zone, "zone cannot be null.");
        Assert.notNull(floatingIp, "floatingIp cannot be null.");

        OpenStackFloatingIp output = new OpenStackFloatingIp();
        output.setPortId(floatingIp.getPortId());
        output.setFixedIp(floatingIp.getFixedIpAddress());
        output.setExternalId(floatingIp.getId());
        output.setIpAddress(floatingIp.getFloatingIpAddress());
        output.setTenantId(project.getId());
        output.setTenantName(project.getTenantAlias());
        output.setZoneId(zone.getId());
        output.setRegionName(zone.getRegionAlias());
        output.setPublic(true);
        output.setIpState(convertToIpState(floatingIp.getStatus()));
        return output;
    }

    public static List<OpenStackPort> toOpenStackPorts(List<Port> ports, OpenStackTenant project, OpenStackRegionConfig zone) {
        Assert.notNull(project, "project cannot be null.");
        Assert.notNull(zone, "zone cannot be null.");

        List<OpenStackPort> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ports)) {
            return result;
        }
        for (Port port : ports) {
            result.add(toOpenStackPort(port, project, zone));
        }
        return result;
    }

    public static OpenStackPort toOpenStackPort(Port port, OpenStackTenant project, OpenStackRegionConfig zone) {
        Assert.notNull(project, "project cannot be null.");
        Assert.notNull(zone, "zone cannot be null.");
        Assert.notNull(port, "port cannot be null.");

        OpenStackPort output = new OpenStackPort();
        output.setIpAddress(PortUtils.getIpAddress(port));

        output.setPortId(port.getId());
        output.setTenantId(project.getId());
        output.setTenantName(project.getTenantAlias());
        output.setZoneId(zone.getId());
        output.setRegionName(zone.getRegionAlias());
        output.setPublic(false);
        output.setIpState(convertToIpState(port.getStatus()));
        return output;
    }

    private static IPState convertToIpState(String name) {
        // ACTIVE means that IP is associated to instance
        // DOWN means that IP is just allocated
        if ("ACTIVE".equals(name) || "DOWN".equals(name) || "DETACHED".equals(name)) {
            return IPState.READY;
        }
        return null;
    }

    public static List<SubnetModel> toOurSubnets(List<NovaSubnet> subnets) {
        return ConversionUtils.convertCollection(subnets, input -> {
            OpenStackSubnet subnet = new OpenStackSubnet();
            subnet.setSubnetId(input.getId());
            subnet.setName(input.getName());
            subnet.setTenantId(input.getTenantId());
            subnet.setNetworkId(input.getNetworkId());
            subnet.setCidr(input.getCidr());
            subnet.setGatewayIp(input.getGateway());
            subnet.setDhcpEnabled(input.isEnableDHCP());
            return subnet;
        });
    }
}
