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

import io.maestro3.agent.dao.ServerConfigDao;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.general.ServerConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.util.ConversionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Service
public class ServerDbServiceImpl implements ServerDbService {

    private ServerConfigDao serverConfigDao;

    public ServerDbServiceImpl(@Autowired ServerConfigDao serverConfigDao) {
        this.serverConfigDao = serverConfigDao;
    }

    @Override
    public void saveServerConfig(ServerConfig serverConfig) {
        serverConfigDao.saveServerConfig(serverConfig);
    }

    @Override
    public void updateServerConfig(String dbId, ServerStateEnum serverStateEnum, String ipAddress, Boolean runSuccess) {
        serverConfigDao.updateServerConfig(dbId, serverStateEnum, ipAddress, runSuccess);
    }

    @Override
    public void updateServerConfig(String dbId, ServerStateEnum serverStateEnum, String ipAddress, Set<String> volumeIps, Boolean runSuccess) {
        serverConfigDao.updateServerConfig(dbId, serverStateEnum, ipAddress, volumeIps, runSuccess);
    }

    @Override
    public void insertServers(Collection<ServerConfig> serverConfigs) {
        serverConfigDao.insertServers(serverConfigs);
    }

    @Override
    public OpenStackServerConfig findServer(String regionId, String tenantId, String nameAlias) {
        return (OpenStackServerConfig) serverConfigDao.findServer(regionId, tenantId, nameAlias);
    }

    @Override
    public Collection<OpenStackServerConfig> findAllTenantServers(String regionId, String tenantId) {
        Collection<ServerConfig> tenantServers = serverConfigDao.findTenantServers(regionId, tenantId);
        return ConversionUtils.convertToSubclass(tenantServers, OpenStackServerConfig.class);
    }

    @Override
    public Collection<OpenStackServerConfig> findAllAvailableTenantServers(String regionId, String tenantId) {
        Collection<ServerConfig> tenantServers = serverConfigDao.findAvailableTenantServers(regionId, tenantId);
        return ConversionUtils.convertToSubclass(tenantServers, OpenStackServerConfig.class);
    }

    @Override
    public Collection<OpenStackServerConfig> findTenantServersNotInState(String regionId, String tenantId, Collection<String> states) {
        Collection<ServerConfig> tenantServers = serverConfigDao.findTenantServersNotInState(regionId, tenantId, states);
        return ConversionUtils.convertToSubclass(tenantServers, OpenStackServerConfig.class);
    }

    @Override
    public OpenStackServerConfig findServerByNativeId(String regionId, String tenantId, String nativeId) {
        return (OpenStackServerConfig) serverConfigDao.findServerByNativeId(regionId, tenantId, nativeId);
    }

    @Override
    public Collection<OpenStackServerConfig> findServersByNativeIds(String regionId, String tenantId, List<String> ids) {
        Collection<ServerConfig> tenantServers = serverConfigDao.findServersByIdsInTenant(regionId, tenantId, ids);
        return ConversionUtils.convertToSubclass(tenantServers, OpenStackServerConfig.class);
    }

    @Override
    public void deleteServer(String id) {
        serverConfigDao.deleteServer(id);
    }
}
