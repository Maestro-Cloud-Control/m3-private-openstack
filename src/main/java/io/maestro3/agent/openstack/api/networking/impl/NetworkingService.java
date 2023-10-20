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

package io.maestro3.agent.openstack.api.networking.impl;

import io.maestro3.agent.openstack.api.networking.INetworkService;
import io.maestro3.agent.openstack.api.networking.INetworkingService;
import io.maestro3.agent.openstack.api.networking.ISubnetService;
import io.maestro3.agent.openstack.api.networking.extension.IFloatingIpExtension;
import io.maestro3.agent.openstack.api.networking.extension.IPortExtension;
import io.maestro3.agent.openstack.api.networking.extension.IQuotasExtension;
import io.maestro3.agent.openstack.api.networking.extension.IRouterExtension;
import io.maestro3.agent.openstack.api.networking.extension.ISecurityGroupExtension;
import io.maestro3.agent.openstack.api.networking.extension.ISecurityGroupRuleExtension;
import io.maestro3.agent.openstack.api.networking.extension.impl.FloatingIpExtension;
import io.maestro3.agent.openstack.api.networking.extension.impl.PortExtension;
import io.maestro3.agent.openstack.api.networking.extension.impl.QuotasExtension;
import io.maestro3.agent.openstack.api.networking.extension.impl.RouterExtension;
import io.maestro3.agent.openstack.api.networking.extension.impl.SecurityGroupExtension;
import io.maestro3.agent.openstack.api.networking.extension.impl.SecurityGroupRuleExtension;
import io.maestro3.agent.openstack.client.IOSClient;


public class NetworkingService implements INetworkingService {

    private final INetworkService networks;
    private final IRouterExtension routers;
    private final ISubnetService subnets;
    private final ISecurityGroupExtension securityGroups;
    private final ISecurityGroupRuleExtension securityGroupRules;
    private final IFloatingIpExtension floatingIpExtension;
    private final IPortExtension portExtension;
    private final IQuotasExtension quotasExtension;

    public NetworkingService(IOSClient client) {
        networks = new NetworkService(client);
        routers = new RouterExtension(client);
        subnets = new SubnetService(client);
        securityGroups = new SecurityGroupExtension(client);
        securityGroupRules = new SecurityGroupRuleExtension(client);
        floatingIpExtension = new FloatingIpExtension(client);
        portExtension = new PortExtension(client);
        quotasExtension = new QuotasExtension(client);
    }

    @Override
    public INetworkService networks() {
        return networks;
    }

    @Override
    public IRouterExtension routers() {
        return routers;
    }

    @Override
    public ISubnetService subnets() {
        return subnets;
    }

    @Override
    public ISecurityGroupExtension securityGroups() {
        return securityGroups;
    }

    @Override
    public ISecurityGroupRuleExtension securityGroupRules() {
        return securityGroupRules;
    }

    @Override
    public IFloatingIpExtension floatingIps() {
        return floatingIpExtension;
    }

    @Override
    public IPortExtension ports() {
        return portExtension;
    }

    @Override
    public IQuotasExtension quotas() {
        return quotasExtension;
    }
}
