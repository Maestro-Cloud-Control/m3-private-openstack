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

import io.maestro3.agent.api.handler.IM3ApiHandler;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.M3SdkVersion;
import io.maestro3.sdk.v3.model.SdkCloud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractInstanceHandler implements IM3ApiHandler {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected IOpenStackRegionRepository regionDbService;
    protected TenantDbService tenantDbService;
    protected ServerDbService serverDbService;
    protected OpenStackApiProvider openStackApiProvider;
    protected Locker locker;

    public AbstractInstanceHandler(IOpenStackRegionRepository regionDbService,
                                   TenantDbService tenantDbService,
                                   ServerDbService serverDbService,
                                   OpenStackApiProvider openStackApiProvider, Locker locker) {
        this.regionDbService = regionDbService;
        this.tenantDbService = tenantDbService;
        this.serverDbService = serverDbService;
        this.openStackApiProvider = openStackApiProvider;
        this.locker = locker;
    }

    protected void updateServerConfigurationState(OpenStackServerConfig serverConfig,
                                                  ServerStateEnum stateEnum) {
        serverDbService.updateServerConfig(serverConfig.getId(), stateEnum, null, null);
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public M3SdkVersion getSupportedVersion() {
        return M3SdkVersion.V3;
    }
}
