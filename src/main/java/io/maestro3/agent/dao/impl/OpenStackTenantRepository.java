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

package io.maestro3.agent.dao.impl;

import io.maestro3.agent.dao.BaseTenantDao;
import io.maestro3.agent.dao.IOpenStackTenantRepository;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Repository
public class OpenStackTenantRepository extends BaseTenantDao<OpenStackTenant> implements IOpenStackTenantRepository {

    @Autowired
    public OpenStackTenantRepository(MongoTemplate template) {
        super(template, PrivateCloudType.OPEN_STACK);
    }

    @Override
    public OpenStackTenant findByNativeId(String nativeId) {
        Assert.hasLength(nativeId, "nativeId cannot be null or empty");
        Criteria searchCriteria = Criteria.where("nativeId").is(nativeId)
            .and("cloud").is(cloudType.toString());
        return template.findOne(Query.query(searchCriteria), OpenStackTenant.class, COLLECTION);
    }

    @Override
    public List<OpenStackTenant> findAllByRegion(String regionId, Collection<String> tenantAliases) {
        Assert.hasLength(regionId, "regionId cannot be null or empty");
        Assert.notEmpty(tenantAliases, "tenantAliases cannot be null or empty");

        Criteria searchCriteria = Criteria.where("regionId").is(regionId)
            .and("nameAlias").in(tenantAliases)
            .and("cloud").is(cloudType.toString());

        return template.find(Query.query(searchCriteria), OpenStackTenant.class, COLLECTION);
    }

    @Override
    public void updateProjectsNetworkId(String regionId, String oldNetworkId, String newNetworkId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(oldNetworkId, "oldNetworkId should not be null or empty.");
        Assert.hasText(newNetworkId, "newNetworkId should not be null or empty.");

        Criteria criteria = where("regionId").is(regionId).and("networkId").is(oldNetworkId);
        template.updateMulti(query(criteria), Update.update("networkId", newNetworkId), COLLECTION);
    }
}
