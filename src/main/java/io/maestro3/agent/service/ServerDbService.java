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

package io.maestro3.agent.service;

import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.general.ServerConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface ServerDbService {

    void saveServerConfig(ServerConfig serverConfig);

    void updateServerConfig(String dbId, ServerStateEnum serverStateEnum, String ipAddress, Boolean runSuccess);

    void updateServerConfig(String dbId, ServerStateEnum serverStateEnum, String ipAddress, Set<String> volumeIps, Boolean runSuccess);

    void insertServers(Collection<ServerConfig> serverConfigs);

    OpenStackServerConfig findServer(String regionId, String tenantId, String nameAlias);

    Collection<OpenStackServerConfig> findAllTenantServers(String regionId, String tenantId);

    Collection<OpenStackServerConfig> findAllAvailableTenantServers(String regionId, String tenantId);

    Collection<OpenStackServerConfig> findTenantServersNotInState(String regionId, String tenantId, Collection<String> states);

    OpenStackServerConfig findServerByNativeId(String regionId, String tenantId, String nativeId);

    Collection<OpenStackServerConfig> findServersByNativeIds(String regionId, String tenantId, List<String> ids);

    void deleteServer(String id);
}
