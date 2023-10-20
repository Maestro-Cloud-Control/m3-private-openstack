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
import io.maestro3.agent.model.compute.RebootType;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.lock.VoidOperation;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;
import io.maestro3.sdk.v3.model.instance.SdkInstances;
import io.maestro3.sdk.v3.model.instance.SdkOpenStackInstance;
import io.maestro3.sdk.v3.request.instance.RebootInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Component("osRebootInstanceHandler")
public class OsRebootInstanceHandler extends AbstractInstanceHandler implements IM3ApiHandler {

    @Autowired
    public OsRebootInstanceHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                   ServerDbService serverDbService, OpenStackApiProvider openStackApiProvider,
                                   @Qualifier("instanceLocker") Locker locker) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker);
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        RebootInstanceRequest request = M3ApiActionInverter.toRebootInstanceRequest(action);

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());
OpenStackServerConfig server = serverDbService.findServer(region.getId(), tenant.getId(), request.getInstanceId());

        rebootServer(region, tenant, server);
        locker.executeOperation(tenant.getId(), (VoidOperation<M3PrivateAgentException>) () ->
                updateServerConfigurationState(server, ServerStateEnum.REBOOTING));

        SdkOpenStackInstance m3SdkInstance = M3SDKModelConverter.toSdkOpenStackInstance(
                region, tenant, server, SdkInstanceState.REBOOTING);
        SdkInstances result = new SdkInstances();
        result.setSdkInstances(Collections.singletonList(m3SdkInstance));

        return M3Result.success(action.getId(), result);
    }

    private void rebootServer(OpenStackRegionConfig region,
                              OpenStackTenant tenant,
                              OpenStackServerConfig server) throws M3PrivateAgentException {
        try {
            openStackApiProvider.openStack(tenant, region).compute().servers().reboot(server.getNativeId(), RebootType.HARD);
        } catch (OSClientException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.REBOOT_INSTANCE));
    }
}
