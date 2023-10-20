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

import io.maestro3.agent.dao.IVLANDao;
import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class VLANDao implements IVLANDao {

    public static final String COLLECTION_NAME = "VLANs";

    private MongoOperations mongoOperations;

    public VLANDao(@Autowired MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public void save(VLAN object) {
        Assert.notNull(object, "object can't be null.");
        mongoOperations.save(object, COLLECTION_NAME);
    }

    public void update(VLAN object) {
        Assert.notNull(object, "object can't be null.");
        mongoOperations.save(object, COLLECTION_NAME);
    }

    public void delete(String id) {
        Assert.hasText(id, "id can't be null or empty.");

        mongoOperations.remove(Query.query(Criteria.where("_id").is(new ObjectId(id))), COLLECTION_NAME);
    }

    public List<VLAN> findAll() {
        return mongoOperations.findAll(VLAN.class, COLLECTION_NAME);
    }

    @Override
    public VLAN findByName(String vlanName, String tenantId, String regionId) {
        Assert.hasText(vlanName, "VLAN name should not be null or empty.");
        Assert.hasText(tenantId, "tenantId should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");
        return mongoOperations.findOne(query(where("operationalSearchId").is(vlanName.toLowerCase())
            .and("regionId").is(regionId)
            .and("tenantId").is(tenantId)), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public VLAN findByNameForRegion(String vlanName, String regionId) {
        Assert.hasText(vlanName, "VLAN name should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");

        Query searchQuery = query(where("operationalSearchId").is(vlanName.toLowerCase())
            .and("regionId").is(regionId)
            .orOperator(where("tenantId").is(null), where("tenantId").exists(false)));
        return mongoOperations.findOne(searchQuery, VLAN.class, COLLECTION_NAME);
    }

    @Override
    public VLAN findByID(String vlanID) {
        Assert.hasText(vlanID, "VLAN ID should not be null or empty.");
        return mongoOperations.findOne(query(where("_id").is(vlanID)), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public List<VLAN> findForTenant(String tenantId, String regionId) {
        Assert.hasText(tenantId, "tenantId should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");
        return mongoOperations.find(query(where("regionId").is(regionId)
            .orOperator(where("tenantId").is(tenantId),
                where("tenantId").exists(false), where("tenantId").is(null))), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public VLAN findForTenantByName(String vlanName, String tenantId, String regionId) {
        Assert.hasText(vlanName, "vlanName should not be null or empty.");
        Assert.hasText(tenantId, "tenantId should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");

        return mongoOperations.findOne(query(where("regionId").is(regionId).and("operationalSearchId").is(vlanName.toLowerCase())
            .orOperator(where("tenantId").is(tenantId),
                where("tenantId").exists(false), where("tenantId").is(null))), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public List<VLAN> findOnlyForTenant(String tenantId, String regionId) {
        Assert.hasText(tenantId, "tenantId should not be null or empty.");
        Assert.hasText(regionId, "regionId should not be null or empty.");
        return mongoOperations.find(query(where("regionId").is(regionId)
            .and("tenantId").is(tenantId)), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public List<VLAN> findDmzForRegion(String regionId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");

        return mongoOperations.find(query(where("regionId").is(regionId)
            .and("isDmz").is(true)), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public List<VLAN> findByName(List<String> names) {
        Assert.notEmpty(names, "should be provided at least one VLAN name");
        return mongoOperations.find(query(where("name").in(names)), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public List<VLAN> findByOpenStackNetworkId(String regionId, String networkId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(networkId, "networkId should not be null or empty.");

        Query searchQuery = query(where("regionId").is(regionId)
            .and("openStackNetworkId").is(networkId));
        return mongoOperations.find(searchQuery, VLAN.class, COLLECTION_NAME);
    }

    @Override
    public List<VLAN> findTenantByOpenStackNetworkId(String regionId, String tenantId, String networkId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(networkId, "networkId should not be null or empty.");

        Query searchQuery = query(where("regionId").is(regionId)
            .and("tenantId").is(tenantId)
            .and("openStackNetworkId").is(networkId));
        return mongoOperations.find(searchQuery, VLAN.class, COLLECTION_NAME);
    }

    @Override
    public List<VLAN> findByRegionId(String regionId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Criteria criteria = where("regionId").is(regionId);
        return mongoOperations.find(query(criteria), VLAN.class, COLLECTION_NAME);
    }

    @Override
    public void updateByOpenStackNetworkId(String regionId, String oldNetworkId, String newNetworkId) {
        Assert.hasText(regionId, "regionId should not be null or empty.");
        Assert.hasText(oldNetworkId, "oldNetworkId should not be null or empty.");
        Assert.hasText(newNetworkId, "newNetworkId should not be null or empty.");

        Criteria criteria = where("regionId").is(regionId).and("openStackNetworkId").is(oldNetworkId);
        mongoOperations.updateMulti(query(criteria), Update.update("openStackNetworkId", newNetworkId), COLLECTION_NAME);
    }

    @Override
    public Collection<VLAN> findTenantVLANs(Collection<OpenStackTenant> tenants) {
        Assert.notEmpty(tenants, "tenant can not be null");

        Set<String> tenantIds = tenants.stream()
            .map(OpenStackTenant::getId)
            .collect(Collectors.toSet());
        Criteria searchCriteria = where("tenantId").in(tenantIds);
        return mongoOperations.find(query(searchCriteria), VLAN.class, COLLECTION_NAME);
    }
}
