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

import io.maestro3.agent.dao.VolumeDao;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.cadf.util.Assert;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;


@Repository
public class VolumeDaoImpl implements VolumeDao {

    private static final String COLLECTION_NAME = "Volumes";

    private MongoOperations mongoOperations;

    @Autowired
    public VolumeDaoImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void save(CinderVolume volume) {
        Assert.notNull(volume, "Volume cannot be null");
        Query query = Query.query(Criteria.where(CinderVolume.ID_FIELD).is(volume.getId()));
        mongoOperations.findAndReplace(query, volume, FindAndReplaceOptions.options().upsert(), CinderVolume.class, COLLECTION_NAME);
    }

    @Override
    public void updateVolumes(List<CinderVolume> volumesToRemove, List<CinderVolume> volumesToUpdate) {
        Assert.notNull(volumesToUpdate, "List of volumes to update can not be null");
        Assert.notNull(volumesToRemove, "List of volumes to remove can not be null");

        MongoCollection<Document> collection = mongoOperations.getCollection(COLLECTION_NAME);
        List<WriteModel<Document>> updates = volumesToUpdate.stream()
            .map(v -> new ReplaceOneModel<>(Criteria.where(CinderVolume.ID_FIELD).is(v.getId()).getCriteriaObject(),
                Document.parse(JsonUtils.convertObjectToJson(v)), new ReplaceOptions().upsert(true)))
            .collect(Collectors.toList());
        volumesToRemove.stream()
            .map(v -> new DeleteOneModel<Document>(Document.parse(JsonUtils.convertObjectToJson(v))))
            .forEach(updates::add);
        if (CollectionUtils.isNotEmpty(updates)) {
            collection.bulkWrite(updates);
        }
    }

    @Override
    public CinderVolume findById(String volumeId) {
        Assert.hasText(volumeId, "Volume id cannot be null or empty");

        Criteria criteria = Criteria.where("_id").is(volumeId);
        return mongoOperations.findOne(Query.query(criteria), CinderVolume.class, COLLECTION_NAME);
    }

    @Override
    public List<CinderVolume> findByTenantAndRegion(String tenantName, String regionName) {
        Assert.hasText(tenantName, "Tenant name cannot be null or empty");
        Assert.hasText(regionName, "Region name cannot be null or empty");

        Criteria criteria = Criteria.where("tenant").is(tenantName).and("region").is(regionName);
        return mongoOperations.find(Query.query(criteria), CinderVolume.class, COLLECTION_NAME);
    }

    @Override
    public List<CinderVolume> findByIds(List<String> volumeIds) {
        Assert.isTrue(CollectionUtils.isNotEmpty(volumeIds), "List of volume ids to be removed can not be null or empty");

        Criteria criteria = Criteria.where(CinderVolume.ID_FIELD).in(volumeIds);
        return mongoOperations.find(Query.query(criteria), CinderVolume.class, COLLECTION_NAME);
    }
}
