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
import io.maestro3.agent.converter.M3ApiActionInverter;
import io.maestro3.agent.converter.M3SDKModelConverter;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.lock.VoidOperation;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.agent.service.VolumeDbService;
import io.maestro3.agent.tasks.ITaskExecutor;
import io.maestro3.agent.tasks.impl.BaseTaskData;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;
import io.maestro3.sdk.v3.model.instance.SdkInstances;
import io.maestro3.sdk.v3.model.instance.SdkOpenStackInstance;
import io.maestro3.sdk.v3.request.instance.TerminateInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component("osTerminateInstanceHandler")
public class OsTerminateInstanceHandler extends AbstractInstanceHandler implements IM3ApiHandler {

    private final ITaskExecutor taskExecutor;
    private final DbServicesProvider dbServicesProvider;
    private final long taskDelay;

    @Autowired
    public OsTerminateInstanceHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                      ServerDbService serverDbService, OpenStackApiProvider openStackApiProvider,
                                      @Qualifier("instanceLocker") Locker locker, ITaskExecutor taskExecutor,
                                      DbServicesProvider dbServicesProvider,
                                      @Value("${internal.task.delay.millis}") long taskDelay) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker);
        this.taskExecutor = taskExecutor;
        this.dbServicesProvider = dbServicesProvider;
        this.taskDelay = taskDelay;
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        TerminateInstanceRequest request = M3ApiActionInverter.toTerminateInstanceRequest(action);

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());
        OpenStackServerConfig server = serverDbService.findServer(region.getId(), tenant.getId(), request.getInstanceId());

        killServer(region, tenant, server);

        locker.executeOperation(tenant.getId(), (VoidOperation<M3PrivateAgentException>) () ->
            updateServerConfigurationState(server, ServerStateEnum.TERMINATING));  // mark server state in db as terminating

        SdkOpenStackInstance m3SdkInstance = M3SDKModelConverter.toSdkOpenStackInstance(
            region, tenant, server, SdkInstanceState.TERMINATED);

        Set<String> attachedVolumesIds = server.getAttachedVolumes();
        deleteVolumes(attachedVolumesIds, tenant, region);

        SdkInstances result = new SdkInstances();
        result.setSdkInstances(Collections.singletonList(m3SdkInstance));
        return M3Result.success(action.getId(), result);
    }

    private void deleteVolumes(Set<String> volumeIds, OpenStackTenant tenant, OpenStackRegionConfig region) {
        if (CollectionUtils.isEmpty(volumeIds)){
            return;
        }
        VolumeDbService volumeDbService = dbServicesProvider.getVolumeDbService();
        List<CinderVolume> volumesToDelete = volumeDbService.findByIds(new ArrayList<>(volumeIds));
        long executionTime = System.currentTimeMillis() + taskDelay;
        for (CinderVolume volume : volumesToDelete) {
            taskExecutor.addTask(new BaseTaskData(volume.getId(), tenant.getNativeId(), region.getId(),
                () -> deleteVolume(region, tenant, volume), executionTime));
        }
    }

    private void deleteVolume(OpenStackRegionConfig region,
                              OpenStackTenant tenant, CinderVolume volume) throws M3PrivateAgentException {
        try {
            openStackApiProvider.openStack(tenant, region).blockStorage().volumes().delete(volume.getId());
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    private void killServer(OpenStackRegionConfig region,
                            OpenStackTenant tenant, OpenStackServerConfig server) throws M3PrivateAgentException {
        try {
            openStackApiProvider.openStack(tenant, region).compute().servers().delete(server.getNativeId());
        } catch (OSClientException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.TERMINATE_INSTANCE));
    }
}
