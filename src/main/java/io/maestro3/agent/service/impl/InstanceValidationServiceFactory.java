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

package io.maestro3.agent.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.service.IServiceFactory;
import io.maestro3.agent.service.IOpenStackClientProvider;
import io.maestro3.agent.service.IOpenStackInstanceProvisioningValidationService;
import io.maestro3.agent.service.IOpenStackProjectCustomSecurityGroupService;
import io.maestro3.agent.service.IOpenStackSecurityGroupService;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import io.maestro3.agent.service.IOpenStackVLANService;
import io.maestro3.agent.service.ServerDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class InstanceValidationServiceFactory implements IServiceFactory<IOpenStackInstanceProvisioningValidationService> {
    private final IOpenStackClientProvider clientProvider;
    private final ServerDbService instanceService;
    private final IOpenStackStaticIpService staticIpService;
    private final IOpenStackTenantRepository projectService;
    private final IOpenStackVLANService vlanService;
    private final IOpenStackProjectCustomSecurityGroupService projectCustomSecurityGroupService;
    private final IOpenStackSecurityGroupService openStackSecurityGroupService;
    private final boolean floatingIpPreferable;
    private final Cache<String, IOpenStackInstanceProvisioningValidationService> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();

    @Autowired
    public InstanceValidationServiceFactory(IOpenStackClientProvider clientProvider, ServerDbService instanceService,
                                            IOpenStackStaticIpService staticIpService, IOpenStackTenantRepository projectService, IOpenStackVLANService vlanService,
                                            IOpenStackProjectCustomSecurityGroupService projectCustomSecurityGroupService,
                                            IOpenStackSecurityGroupService openStackSecurityGroupService,
                                            @Value("${openstack.use.floating.ip:true}") boolean floatingIpPreferable) {
        this.clientProvider = clientProvider;
        this.instanceService = instanceService;
        this.staticIpService = staticIpService;
        this.floatingIpPreferable = floatingIpPreferable;
        this.projectService = projectService;
        this.vlanService = vlanService;
        this.projectCustomSecurityGroupService = projectCustomSecurityGroupService;
        this.openStackSecurityGroupService = openStackSecurityGroupService;
    }

    @Override
    public IOpenStackInstanceProvisioningValidationService get(OpenStackRegionConfig region) {
        IOpenStackInstanceProvisioningValidationService virtService = cache.getIfPresent(region.getRegionAlias());
        if (virtService == null) {
            synchronized (this) {
                virtService = cache.getIfPresent(region.getRegionAlias());
                if (virtService == null) {
                    virtService = new OpenStackInstanceProvisioningValidationService(region, clientProvider,
                        instanceService, staticIpService, projectService, vlanService,
                        projectCustomSecurityGroupService, openStackSecurityGroupService, floatingIpPreferable);
                    cache.put(region.getRegionAlias(), virtService);
                }
            }
        }
        return virtService;
    }
}
