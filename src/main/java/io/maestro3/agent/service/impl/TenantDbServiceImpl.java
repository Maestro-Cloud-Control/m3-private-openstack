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

import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.TenantDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
public class TenantDbServiceImpl implements TenantDbService {

    private IOpenStackTenantRepository tenantDao;

    @Autowired
    public TenantDbServiceImpl(IOpenStackTenantRepository tenantDao) {
        this.tenantDao = tenantDao;
    }

    @Override
    public Collection<OpenStackTenant> findAllByRegion(String regionId) {
        return tenantDao.findByRegionIdInCloud(regionId);
    }

    @Override
    public void save(OpenStackTenant config) {
        tenantDao.save(config);
    }

    @Override
    public Collection<OpenStackTenant> findAll() {
        return tenantDao.findAllInCloud();
    }

    @Override
    public Collection<OpenStackTenant> findAllByRegion(String regionId, Collection<String> tenantAliases) {
        return tenantDao.findAllByRegion(regionId, tenantAliases);
    }

    @Override
    public OpenStackTenant findOpenStackTenantByNativeId(String tenantId) {
        return tenantDao.findByNativeId(tenantId);
    }

    @Override
    public OpenStackTenant findOpenStackTenantByNameAndRegion(String tenantName, String regionId) {
        return tenantDao.findByTenantAliasAndRegionIdInCloud(tenantName, regionId);
    }
}
