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

import io.maestro3.agent.dao.ServerConfigDao;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.general.ServerConfig;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Repository
public class ServerConfigDaoImpl implements ServerConfigDao {

    private final static String COLLECTION_NAME = "ServerConfigs";

    private MongoOperations mongoOperations;

    public ServerConfigDaoImpl(@Autowired MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void saveServerConfig(ServerConfig serverConfig) {
        Assert.notNull(serverConfig, "keyName cannot be null");

        mongoOperations.save(serverConfig, COLLECTION_NAME);
    }

    @Override
    public void updateServerConfig(String dbId, ServerStateEnum serverStateEnum, String ipAddress, Set<String> volumeIps, Boolean runSuccess) {
        Assert.notNull(dbId, "keyName cannot be null");
        Assert.notNull(serverStateEnum, "keyName cannot be null");

        Query searchQuery = Query.query(Criteria.where("_id").is(new ObjectId(dbId)));
        Update update = Update.update("state", serverStateEnum);
        update.set("attachedVolumes", volumeIps);
        if (StringUtils.isNotBlank(ipAddress)) {
            update.set("networkInterfaceInfo.privateIP", ipAddress);
        }
        if (runSuccess != null) {
            update.set("instanceRunSuccess", runSuccess);
        }
        mongoOperations.updateFirst(searchQuery, update, COLLECTION_NAME);
    }

    @Override
    public void updateServerConfig(String dbId, ServerStateEnum serverStateEnum, String ipAddress, Boolean runSuccess) {
        Assert.notNull(dbId, "keyName cannot be null");
        Assert.notNull(serverStateEnum, "keyName cannot be null");

        Query searchQuery = Query.query(Criteria.where("_id").is(new ObjectId(dbId)));
        Update update = Update.update("state", serverStateEnum);
        if (StringUtils.isNotBlank(ipAddress)) {
            update.set("networkInterfaceInfo.privateIP", ipAddress);
        }
        if (runSuccess != null) {
            update.set("instanceRunSuccess", runSuccess);
        }
        mongoOperations.updateFirst(searchQuery, update, COLLECTION_NAME);
    }

    @Override
    public void insertServers(Collection<ServerConfig> serverConfig) {
        Assert.notEmpty(serverConfig, "serverConfig cannot be null or empty");

        mongoOperations.insert(serverConfig, COLLECTION_NAME);
    }

    @Override
    public ServerConfig findServer(String regionId, String tenantId, String nameAlias) {
        Assert.hasText(regionId, "regionId cannot be null or empty");
        Assert.hasText(tenantId, "tenantId cannot be null or empty");
        Assert.hasText(nameAlias, "nameAlias cannot be null or empty");

        CriteriaDefinition searchCriteria = Criteria.where("nameAlias").is(nameAlias)
            .and("tenantId").is(tenantId)
            .and("regionId").is(regionId);

        return mongoOperations.findOne(Query.query(searchCriteria), ServerConfig.class, COLLECTION_NAME);
    }

    @Override
    public Collection<ServerConfig> findTenantServers(String regionId, String tenantId) {
        Assert.hasText(regionId, "regionId cannot be null or empty");
        Assert.hasText(tenantId, "tenantId cannot be null or empty");

        CriteriaDefinition searchCriteria = Criteria.where("tenantId").is(tenantId)
            .and("regionId").is(regionId);

        return mongoOperations.find(Query.query(searchCriteria), ServerConfig.class, COLLECTION_NAME);
    }

    @Override
    public Collection<ServerConfig> findAvailableTenantServers(String regionId, String tenantId) {
        Assert.hasText(regionId, "regionId cannot be null or empty");
        Assert.hasText(tenantId, "tenantId cannot be null or empty");

        CriteriaDefinition searchCriteria = Criteria.where("tenantId").is(tenantId)
            .and("regionId").is(regionId)
            .and("state").ne("TERMINATED");

        return mongoOperations.find(Query.query(searchCriteria), ServerConfig.class, COLLECTION_NAME);
    }

    @Override
    public Collection<ServerConfig> findServersByIdsInTenant(String regionId, String tenantId, List<String> ids) {
        Assert.hasText(regionId, "regionId cannot be null");
        Assert.hasText(tenantId, "tenantId cannot be null");
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        CriteriaDefinition searchCriteria = Criteria.where("tenantId").is(tenantId)
            .and("regionId").is(regionId)
            .and("nativeId").in(ids);

        return mongoOperations.find(Query.query(searchCriteria), ServerConfig.class, COLLECTION_NAME);
    }

    @Override
    public void deleteServer(String id) {
        Assert.hasText(id, "regionId cannot be null or empty");

        mongoOperations.remove(Query.query(Criteria.where("_id").is(id)), COLLECTION_NAME);
    }

    @Override
    public ServerConfig findServerByNativeId(String regionId, String tenantId, String nativeId) {
        Assert.hasText(regionId, "regionId cannot be null or empty");
        Assert.hasText(tenantId, "tenantId cannot be null or empty");
        Assert.hasText(nativeId, "nativeId cannot be null or empty");

        CriteriaDefinition searchCriteria = Criteria.where("nativeId").is(nativeId)
            .and("tenantId").is(tenantId)
            .and("regionId").is(regionId);

        return mongoOperations.findOne(Query.query(searchCriteria), ServerConfig.class, COLLECTION_NAME);
    }

    @Override
    public Collection<ServerConfig> findTenantServersNotInState(String regionId, String tenantId, Collection<String> states) {
        Assert.hasText(regionId, "regionId cannot be null or empty");

        Criteria searchCriteria = Criteria.where("regionId").is(regionId)
            .and("state").nin(states);
        if (StringUtils.isNotBlank(tenantId)){
            searchCriteria.and("tenantId").is(tenantId);
        }

        return mongoOperations.find(Query.query(searchCriteria), ServerConfig.class, COLLECTION_NAME);
    }
}
