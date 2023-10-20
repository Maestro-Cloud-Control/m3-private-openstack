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
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.compute.bean.NovaKeyPair;
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
import io.maestro3.sdk.v3.request.ssh.DescribeKeysRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component("osDescribeKeyHandler")
public class OsDescribeKeyHandler implements IM3ApiHandler {

    protected final Logger LOG = LoggerFactory.getLogger(OsDescribeKeyHandler.class);

    private IOpenStackRegionRepository regionDbService;
    private TenantDbService tenantDbService;
    private OpenStackApiProvider openStackApiProvider;

    @Autowired
    public OsDescribeKeyHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                OpenStackApiProvider openStackApiProvider) {
        this.regionDbService = regionDbService;
        this.tenantDbService = tenantDbService;
        this.openStackApiProvider = openStackApiProvider;
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {
        DescribeKeysRequest request = M3ApiActionInverter.toDescribeKeyRequest(action);

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());
List<NovaKeyPair> keyPairs = describeKeys(region, tenant);

        List<SdkKeyPair> result = keyPairs.stream().map(keyPair -> {
            SdkKeyPair sdkPair = new SdkKeyPair();
            sdkPair.setName(keyPair.getName());
            sdkPair.setPublicPart(keyPair.getPublicKey());
            sdkPair.setTenant(request.getTenantName());
            sdkPair.setRegion(request.getRegion());
            return sdkPair;
        }).collect(Collectors.toList());

        return M3Result.success(action.getId(), result);
    }

    @Override
    public SdkCloud getSupportedCloud() {
        return SdkCloud.OPEN_STACK;
    }

    private List<NovaKeyPair> describeKeys(OpenStackRegionConfig region, OpenStackTenant tenant) throws M3PrivateAgentException {
        try {
            return openStackApiProvider.openStack(tenant, region).compute().keyPairs().list();
        } catch (OSClientException e) {
            LOG.error(e.getMessage(), e);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public M3SdkVersion getSupportedVersion() {
        return M3SdkVersion.V3;
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.DESCRIBE_KEYS));
    }

}
