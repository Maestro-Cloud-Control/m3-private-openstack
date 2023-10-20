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
import io.maestro3.agent.cadf.openstack.CadfUtils;
import io.maestro3.agent.converter.M3ApiActionInverter;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.agent.service.VolumeDbService;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import io.maestro3.sdk.v3.request.volume.AttachVolumeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Component("osAttachVolumeHandler")
public class OsAttachVolumeHandler extends AbstractVolumeHandler implements IM3ApiHandler {

    @Autowired
    public OsAttachVolumeHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                 ServerDbService serverDbService, VolumeDbService volumeDbService,
                                 OpenStackApiProvider openStackApiProvider, @Qualifier("instanceLocker") Locker locker,
                                 ICadfAuditEventSender sender) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker, volumeDbService, sender);
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        AttachVolumeRequest request = M3ApiActionInverter.toAttachVolumeRequest(action);
        try {
            OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
            OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());
            OpenStackServerConfig server = serverDbService.findServer(region.getId(), tenant.getId(), request.getInstanceId());

            CinderVolume volume = volumeDbService.findById(request.getVolumeId());

            attachVolume(region, tenant, volume, server);
            volume.setStatus("in-use");
            volume.setHost(server.getNativeId());
            volumeDbService.save(volume);

            LOG.info("Result : {}", volume);
            return M3Result.success(action.getId(), null);
        } catch (Exception ex) {
            CadfAuditEvent cadfAuditEvent = CadfUtils.generateVolumeErrorAuditEvent(request.getTenantName(), request.getRegion(),
                request.getVolumeId());
            sender.sendCadfAuditEvent(cadfAuditEvent, Collections.singletonList(AuditEventGroupType.VOLUME_DATA));
            throw ex;
        }
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.ATTACH_VOLUME));
    }
}
