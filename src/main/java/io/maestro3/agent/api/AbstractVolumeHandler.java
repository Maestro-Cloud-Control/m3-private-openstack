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
import io.maestro3.agent.cadf.ICadfAuditEventSender;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.agent.service.VolumeDbService;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;


public abstract class AbstractVolumeHandler extends AbstractInstanceHandler implements IM3ApiHandler {

    protected VolumeDbService volumeDbService;
    protected ICadfAuditEventSender sender;

    public AbstractVolumeHandler(IOpenStackRegionRepository regionDbService,
                                 TenantDbService tenantDbService,
                                 ServerDbService serverDbService,
                                 OpenStackApiProvider openStackApiProvider,
                                 Locker locker, VolumeDbService volumeDbService,
                                 ICadfAuditEventSender sender) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker);
        this.volumeDbService = volumeDbService;
        this.sender = sender;
    }

    protected void attachVolume(OpenStackRegionConfig region, OpenStackTenant tenant,
                                CinderVolume volume, OpenStackServerConfig server) throws M3PrivateAgentException {
        try {
            openStackApiProvider.openStack(tenant, region).compute().servers().attachVolume(server.getNativeId(), volume.getId());
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

}
