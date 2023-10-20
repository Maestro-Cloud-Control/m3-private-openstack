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

package io.maestro3.agent.openstack;

import io.maestro3.agent.converter.M3PrivateAgentModelConverter;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.model.general.ServerConfig;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.setup.OpenStackServersImport;
import io.maestro3.agent.model.setup.OpenStackTenantServersImport;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.internal.util.Assert;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class OpenStackServerImportSupport implements IOpenStackServerImportSupport {

    private final OpenStackApiProvider apiProvider;
    private final IOpenStackRegionRepository regionDbService;
    private final TenantDbService tenantDbService;
    private final ServerDbService serverDbService;

    @Autowired
    public OpenStackServerImportSupport(OpenStackApiProvider apiProvider, IOpenStackRegionRepository regionDbService,
                                        TenantDbService tenantDbService, ServerDbService serverDbService) {
        this.apiProvider = apiProvider;
        this.regionDbService = regionDbService;
        this.tenantDbService = tenantDbService;
        this.serverDbService = serverDbService;
    }

    @Override
    public void importServers(OpenStackServersImport serversImport) throws M3PrivateAgentException {
        Assert.notNull(serversImport, "request cannot be null");
        Assert.hasText(serversImport.getRegionAlias(), "RegionAlias cannot be null");

        OpenStackRegionConfig regionConfig = regionDbService.findByAliasInCloud(serversImport.getRegionAlias());

        Collection<OpenStackTenantServersImport> tenantServersImport = Optional.ofNullable(serversImport.getTenantServersImport())
                .orElse(Collections.emptyList());

        Map<String/*tenant alias*/, Collection<String>/* server aliases list*/> serversOnTenantMapping = tenantServersImport.stream()
                .collect(Collectors.toMap(OpenStackTenantServersImport::getTenantAlias, OpenStackTenantServersImport::getServerAliases));

        Collection<OpenStackTenant> tenantConfigs;
        if (CollectionUtils.isEmpty(tenantServersImport)) {
            tenantConfigs = tenantDbService.findAllByRegion(regionConfig.getId());
        } else {
            tenantConfigs = tenantDbService.findAllByRegion(regionConfig.getId(), serversOnTenantMapping.keySet());
        }

        // used foreach cycle to fail on error behaviour
        for (OpenStackTenant tenantConfig : tenantConfigs) {
            handleTenantServersInternal(apiProvider, regionConfig, serversOnTenantMapping, tenantConfig);
        }

    }

    private void handleTenantServersInternal(OpenStackApiProvider apiProvider,
                                             OpenStackRegionConfig regionConfig,
                                             Map<String, Collection<String>> serversOnTenantMapping,
                                             OpenStackTenant tenantConfig) throws M3PrivateAgentException {
        List<Server> servers = listServers(apiProvider, regionConfig, tenantConfig);
        filterServersInTenant(serversOnTenantMapping, tenantConfig, servers);
        List<ServerConfig> serverConfigs = servers.stream()
                .map(server -> M3PrivateAgentModelConverter.toOpenStackServerConfig(tenantConfig, server))
                .collect(Collectors.toList());
        serverDbService.insertServers(serverConfigs);
    }

    private void filterServersInTenant(Map<String, Collection<String>> serversOnTenantMapping,
                                       OpenStackTenant tenantConfig,
                                       List<Server> servers) {
        Collection<String> serverAliasesToProcess = serversOnTenantMapping.get(tenantConfig.getTenantAlias());
        if (CollectionUtils.isNotEmpty(serverAliasesToProcess)) {
            servers.removeIf(server -> serverAliasesToProcess.contains(server.getName()));
        }
    }

    private List<Server> listServers(OpenStackApiProvider apiProvider,
                                     OpenStackRegionConfig regionConfig,
                                     OpenStackTenant tenantConfig) throws M3PrivateAgentException {
        try {
            return apiProvider.openStack(tenantConfig, regionConfig)
                    .compute().servers().list();
        } catch (OSClientException e) {
            throw new M3PrivateAgentException(e.getMessage());
        }
    }
}
