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
import io.maestro3.agent.converter.M3ApiActionInverter;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.api.storage.bean.CreateCinderVolumeParameters;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.agent.service.VolumeDbService;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.instance.SdkVolume;
import io.maestro3.sdk.v3.request.volume.CreateVolumeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Component("osCreateVolumeHandler")
public class OsCreateVolumeHandler extends AbstractVolumeHandler implements IM3ApiHandler {

    @Autowired
    public OsCreateVolumeHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                 ServerDbService serverDbService, VolumeDbService volumeDbService,
                                 OpenStackApiProvider openStackApiProvider, @Qualifier("instanceLocker") Locker locker,
                                 ICadfAuditEventSender sender) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker, volumeDbService, sender);
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        CreateVolumeRequest request = M3ApiActionInverter.toCreateVolumeRequest(action);

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());

        CinderVolume volume = createVolume(region, tenant, request);
        volumeDbService.save(volume);

        SdkVolume sdkVolume = new SdkVolume();
        sdkVolume.setVolumeId(volume.getId());
        sdkVolume.setVolumeName(volume.getName());
        sdkVolume.setSizeLabel(volume.getSize() + " GB");
        sdkVolume.setState("available");
        LOG.info("Result : {}", volume);
        return M3Result.success(action.getId(), Collections.singletonList(sdkVolume));
    }

    private CinderVolume createVolume(OpenStackRegionConfig region, OpenStackTenant tenant,
                                      CreateVolumeRequest request) throws M3PrivateAgentException {
        try {
            CreateCinderVolumeParameters createVolumeParameters = CreateCinderVolumeParameters.builder()
                    .name(request.getVolumeName()).sizeGb(request.getSizeInGB())
                    .build();

            return openStackApiProvider.openStack(tenant, region).blockStorage().volumes().create(createVolumeParameters);

        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.CREATE_VOLUME));
    }
}
