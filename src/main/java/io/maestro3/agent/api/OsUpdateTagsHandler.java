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
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.lock.VoidOperation;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.agent.util.TagUtils;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.instance.SdkResourceTagDto;
import io.maestro3.sdk.v3.request.tag.UpdateTagsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component("OsUpdateTagsHandler")
public class OsUpdateTagsHandler extends AbstractInstanceHandler implements IM3ApiHandler {

    @Autowired
    public OsUpdateTagsHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                               ServerDbService serverDbService, OpenStackApiProvider openStackApiProvider,
                               @Qualifier("instanceLocker") Locker locker) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker);
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        UpdateTagsRequest request = JsonUtils.parseMap(action.getParams(), UpdateTagsRequest.class);

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());
        OpenStackServerConfig server = serverDbService.findServer(region.getId(), tenant.getId(), request.getInstanceId());

        TagUtils.mergeTags(server, request.getTags());

        locker.executeOperation(tenant.getId(), (VoidOperation<M3PrivateAgentException>) () ->
            serverDbService.saveServerConfig(server));

        List<SdkResourceTagDto> sdkResourceTags = TagUtils.convertResourceTagToSdkResourceTagDto(server.getTags());
        return M3Result.success(action.getId(), sdkResourceTags);
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.UPDATE_TAGS));
    }
}
