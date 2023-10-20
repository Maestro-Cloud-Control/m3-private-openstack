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
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.M3SdkVersion;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.instance.SdkKeyPair;
import io.maestro3.sdk.v3.request.ssh.AddKeyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Component("osAddKeyHandler")
public class OsAddKeyHandler implements IM3ApiHandler {

    protected final Logger LOG = LoggerFactory.getLogger(OsAddKeyHandler.class);

    private IOpenStackRegionRepository regionDbService;
    private TenantDbService tenantDbService;
    private OpenStackApiProvider openStackApiProvider;

    @Autowired
    public OsAddKeyHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                           OpenStackApiProvider openStackApiProvider) {
        this.regionDbService = regionDbService;
        this.tenantDbService = tenantDbService;
        this.openStackApiProvider = openStackApiProvider;
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        AddKeyRequest request = M3ApiActionInverter.toAddKeyRequest(action);

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());
        String name = request.getName();
        String publicKey = request.getPublicKey();

        importKey(region, tenant, name, publicKey);

        SdkKeyPair result = new SdkKeyPair();

        result.setName(name);
        result.setPublicPart(publicKey);
        result.setTenant(request.getTenantName());
        result.setRegion(request.getRegion());

        return M3Result.success(action.getId(), result);
    }

    private void importKey(OpenStackRegionConfig region, OpenStackTenant tenant,
                           String name, String publicKey) throws M3PrivateAgentException {
        try {
            openStackApiProvider.openStack(tenant, region).compute().keyPairs().importKeyPair(name, publicKey);
        } catch (OSClientException e) {
            LOG.error(e.getMessage(), e);
            throw new ReadableAgentException(e.getMessage());
        }
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    @Override
    public M3SdkVersion getSupportedVersion() {
        return M3SdkVersion.V3;
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.ADD_KEY));
    }
}
