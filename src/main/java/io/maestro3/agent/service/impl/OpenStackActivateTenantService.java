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

import com.google.common.collect.Lists;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.network.impl.ip.AddressPool;
import io.maestro3.agent.model.network.NetworkingPolicy;
import io.maestro3.agent.model.network.NetworkingType;
import io.maestro3.agent.model.network.impl.dns.RegionDnsConfiguration;
import io.maestro3.agent.model.network.impl.vlan.Subnet;
import io.maestro3.agent.model.network.impl.TenantNetworkCreationInputParameters;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.networking.bean.IpVersion;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.Networking;
import io.maestro3.agent.openstack.api.networking.bean.NovaAddressPool;
import io.maestro3.agent.openstack.api.networking.bean.NovaSubnet;
import io.maestro3.agent.openstack.api.networking.bean.Router;
import io.maestro3.agent.openstack.api.networking.request.AddRouterInterface;
import io.maestro3.agent.openstack.api.networking.request.CreateNetwork;
import io.maestro3.agent.openstack.api.networking.request.CreateRouter;
import io.maestro3.agent.openstack.api.networking.request.CreateSubnet;
import io.maestro3.agent.openstack.api.networking.request.RemoveRouterInterface;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackActivateTenantService;
import io.maestro3.agent.util.UtilsReadableAssert;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class OpenStackActivateTenantService implements IOpenStackActivateTenantService {

    private static final Logger LOG = LogManager.getLogger(OpenStackActivateTenantService.class);

    private static final String PROJECT_DESCRIPTION_TEMPLATE = "OpenStackTenant for \'%s\' tenant in \'%s\' region (%s node).";

    private static final String NETWORK_NAME_SUFFIX = "-network";
    private static final String SUBNET_NAME_SUFFIX = "-subnet";
    private static final String ROUTER_NAME_SUFFIX = "-router";

    private static final int USER_PASSWORD_LENGTH = 20;

    private static final String ROLE_USER = "user";
    private static final String ROLE_MEMBER = "_member_";
    private static final String ROLE_ADMIN = "admin";

    private static final String ZABBIX_USER_NAME = "zabbix";

    @Autowired
    private OpenStackApiProvider osClientProvider;

    @Override
    public Networking setupNetworking(OpenStackTenant tenant, OpenStackRegionConfig region) {
        IOpenStackApi api = osClientProvider.adminOpenStack(region);
        NetworkingPolicy networkingPolicy = region.getNetworkingPolicy();
        String networkId = networkingPolicy.getNetworkId();

        RegionDnsConfiguration regionDnsConfiguration = networkingPolicy.getRegionDnsConfiguration();
        List<String> dnsServices = regionDnsConfiguration.getServers();

        if (StringUtils.isBlank(networkId)) {
            throw new ReadableAgentException("Network ID is empty for region " + region.getRegionAlias() + ".");
        }

        if (networkingPolicy.getNetworkingType() == NetworkingType.AUTO) {
            tenant.setNetworkId(networkingPolicy.getNetworkId());
            return null;
        }

        Subnet subnet = getDefaultSubnet();

        // create network
        Network network;
        String networkName = tenant.getTenantAlias() + NETWORK_NAME_SUFFIX;
        try {
            CreateNetwork networkConfig = CreateNetwork.network()
                .forProject(tenant.getNativeId())
                .withName(networkName)
                .get();
            network = api.networking().networks().create(networkConfig);
            UtilsReadableAssert.notNull(network, "Got 404 (not found) from the server");
        } catch (OSClientException e) {
            throw new ReadableAgentException("Failed to create network " + networkName + ". " + e.getMessage(), e);
        }

        // create subnet
        NovaSubnet createdSubnet;
        String subnetName = tenant.getTenantAlias() + SUBNET_NAME_SUFFIX;
        try {
            NovaAddressPool addressPool =
                NovaAddressPool.of(subnet.getAddressPool().getStart(), subnet.getAddressPool().getEnd());
            CreateSubnet.Builder builder = CreateSubnet.subnet()
                .ofVersion(IpVersion.V4)
                .withName(subnetName)
                .inNetwork(network.getId())
                .withPool(addressPool)
                .inProject(tenant.getNativeId())
                .withGateway(subnet.getGateway())
                .withCidr(subnet.getCidr())
                .enableDHCP();
            if (!CollectionUtils.isEmpty(dnsServices)) {
                builder = builder.withDNSService(dnsServices.toArray(new String[0]));
            }
            CreateSubnet subnetConfig = builder.get();
            createdSubnet = api.networking().subnets().create(subnetConfig);
            UtilsReadableAssert.notNull(createdSubnet, "Got 404 (not found) from the server");
        } catch (Exception e) {
            // rollback network creation here
            rollbackNetwork(network, api);
            throw new ReadableAgentException("Failed to create subnet " + subnetName + ". " + e.getMessage(), e);
        }

        // create router
        Router router;
        String routerName = tenant.getTenantAlias() + ROUTER_NAME_SUFFIX;
        try {
            CreateRouter configuration = CreateRouter.router()
                .withName(routerName)
                .forProject(tenant.getNativeId())
                .withExternalGateway(networkId)
                .get();
            router = api.networking().routers().create(configuration);
            UtilsReadableAssert.notNull(router, "Got 404 (not found) from the server");
        } catch (Exception e) {
            // rollback network & subnet creation here
            rollbackSubnet(createdSubnet, api);
            rollbackNetwork(network, api);
            throw new ReadableAgentException("Failed to create network router " + routerName + ". " + e.getMessage(), e);
        }

        // attach subnet to the router
        try {
            AddRouterInterface addRouterInterface = AddRouterInterface.builder().subnet(createdSubnet.getId()).get();
            api.networking().routers().addInterface(router.getId(), addRouterInterface);
        } catch (Exception e) {
            rollbackRouter(router, createdSubnet, api);
            rollbackSubnet(createdSubnet, api);
            rollbackNetwork(network, api);
            throw new ReadableAgentException(
                "Failed to attach subnet " + subnetName + " to the router " + routerName + ". " + e.getMessage(), e);
        }
        tenant.setNetworkId(network.getId());

        return new Networking(router, createdSubnet, network);
    }

    @Override
    public Networking setupTenantLimitedNetworking(TenantNetworkCreationInputParameters inputParameters,
                                                   OpenStackTenant tenant, OpenStackRegionConfig region) {
        IOpenStackApi api = osClientProvider.adminOpenStack(region);
        NetworkingPolicy networkingPolicy = region.getNetworkingPolicy();

        RegionDnsConfiguration regionDnsConfiguration = networkingPolicy.getRegionDnsConfiguration();
        List<String> dnsServices = regionDnsConfiguration.getServers();

        Subnet subnet = getTenantCustomizedSubnet(inputParameters);

        String networkGeneralPart = Optional.ofNullable(inputParameters.getNetworkName())
            .orElse(String.format("%s_SDN_%s", StringUtils.upperCase(inputParameters.getTenantName()),
                RandomStringUtils.randomAlphanumeric(8)));

        // create network
        Network network;
        try {
            CreateNetwork networkConfig = CreateNetwork.network().
                forProject(tenant.getNativeId()).
                withName(networkGeneralPart).
                get();
            network = api.networking().networks().create(networkConfig);
            UtilsReadableAssert.notNull(network, "Got 404 (not found) from the server");
            LOG.info(String.format("Tenant network %s was created for tenant %s", networkGeneralPart, inputParameters.getTenantName()));
        } catch (OSClientException e) {
            throw new ReadableAgentException("Failed to create network " + networkGeneralPart + ". " + e.getMessage(), e);
        }

        // create subnet
        NovaSubnet createdSubnet;
        try {
            NovaAddressPool addressPool =
                NovaAddressPool.of(subnet.getAddressPool().getStart(), subnet.getAddressPool().getEnd());
            CreateSubnet.Builder builder = CreateSubnet.subnet()
                .ofVersion(IpVersion.V4)
                .withName(networkGeneralPart)
                .inNetwork(network.getId())
                .withPool(addressPool)
                .inProject(tenant.getNativeId())
                .withGateway(subnet.getGateway())
                .withCidr(subnet.getCidr())
                .enableDHCP();
            if (!CollectionUtils.isEmpty(dnsServices)) {
                builder = builder.withDNSService(dnsServices.toArray(new String[0]));
            }

            CreateSubnet subnetConfig = builder.get();
            createdSubnet = api.networking().subnets().create(subnetConfig);
            UtilsReadableAssert.notNull(createdSubnet, "Got 404 (not found) from the server");
            LOG.info(String.format("Tenant subnet %s was created in network %s for tenant %s",
                networkGeneralPart, networkGeneralPart, inputParameters.getTenantName()));
        } catch (Exception e) {
            // rollback network creation here
            rollbackNetwork(network, api);
            throw new ReadableAgentException("Failed to create subnet " + networkGeneralPart + ". " + e.getMessage(), e);
        }

        // create router
        String routerName = networkGeneralPart + ROUTER_NAME_SUFFIX;
        Router router;
        try {
            CreateRouter configuration = setupCreateRouterConfigurationInternally(inputParameters, tenant, routerName);
            router = api.networking().routers().create(configuration);
            UtilsReadableAssert.notNull(router, "Got 404 (not found) from the server");
            LOG.info(String.format("Tenant router %s was created to bind network %s for tenant %s to gateway %s.",
                routerName, networkGeneralPart, inputParameters.getTenantName(), inputParameters.getGatewayNetworkId()));
        } catch (Exception e) {
            // rollback network & subnet creation here
            rollbackSubnet(createdSubnet, api);
            rollbackNetwork(network, api);
            throw new ReadableAgentException("Failed to create network router " + routerName + ". " + e.getMessage(), e);
        }

        // attach subnet to the router
        try {
            AddRouterInterface addRouterInterface = AddRouterInterface.builder().subnet(createdSubnet.getId()).get();
            api.networking().routers().addInterface(router.getId(), addRouterInterface);
            LOG.info(String.format("Attach subnet %s to router %s.", networkGeneralPart, routerName));
        } catch (Exception e) {
            rollbackRouter(router, createdSubnet, api);
            rollbackSubnet(createdSubnet, api);
            rollbackNetwork(network, api);
            throw new ReadableAgentException(
                "Failed to attach subnet " + networkGeneralPart + " to the router " + routerName + ". " + e.getMessage(), e);
        }
        tenant.setNetworkId(network.getId());

        return new Networking(router, createdSubnet, network);
    }

    private CreateRouter setupCreateRouterConfigurationInternally(TenantNetworkCreationInputParameters inputParameters,
                                                                  OpenStackTenant tenant, String routerName) {
        CreateRouter.Builder configurationBuilder = CreateRouter.router()
            .withName(routerName)
            .forProject(tenant.getNativeId())
            .highlyAvailable(inputParameters.isHighlyAvailable());

        if (StringUtils.isNotBlank(inputParameters.getSubnetId()) && StringUtils.isNotBlank(inputParameters.getIpAddress()))
            configurationBuilder.withExternalGateway(inputParameters.getGatewayNetworkId(),
                inputParameters.getSubnetId(),
                inputParameters.getIpAddress());
        else
            configurationBuilder.withExternalGateway(inputParameters.getGatewayNetworkId());

        if (inputParameters.isDisableSnat()) {
            configurationBuilder.withSnatEnabled(false);
        }

        return configurationBuilder.get();
    }

    private Subnet getTenantCustomizedSubnet(TenantNetworkCreationInputParameters inputParameters) {
        Subnet subnet;
        String cidr = inputParameters.getCidr();
        if (StringUtils.isBlank(cidr)) {
            subnet = getDefaultSubnet();
        } else {
            subnet = new Subnet();
            SubnetUtils.SubnetInfo info = new SubnetUtils(cidr).getInfo();
            List<String> allAddresses = Lists.newArrayList(info.getAllAddresses());
            String lowAddress = info.getLowAddress();
            AddressPool addressPool = AddressPool.of(
                allAddresses.get(allAddresses.indexOf(lowAddress) + 1), info.getHighAddress());
            subnet.setAddressPool(addressPool);
            subnet.setCidr(info.getCidrSignature());
            subnet.setGateway(lowAddress);
        }
        return subnet;
    }

    private void rollbackSubnet(NovaSubnet subnet, IOpenStackApi api) {
        try {
            api.networking().subnets().delete(subnet.getId());
        } catch (Exception e) {
            LOG.debug(e);
        }
    }

    private void rollbackRouter(Router router, NovaSubnet subnet, IOpenStackApi api) {
        try {
            RemoveRouterInterface config = RemoveRouterInterface.builder().subnet(subnet.getId()).get();
            api.networking().routers().removeInterface(router.getId(), config);
        } catch (Exception e) {
            LOG.debug(e);
        }
    }

    private void rollbackNetwork(Network network, IOpenStackApi api) {
        try {
            api.networking().networks().delete(network.getId());
        } catch (Exception e) {
            LOG.debug(e);
        }
    }


    private Subnet getDefaultSubnet() {
        Subnet subnet = new Subnet();
        AddressPool addressPool = AddressPool.of("172.25.0.2", "172.25.0.254");
        subnet.setAddressPool(addressPool);
        subnet.setCidr("172.25.0.0/24");
        subnet.setGateway("172.25.0.1");
        return subnet;
    }
}
