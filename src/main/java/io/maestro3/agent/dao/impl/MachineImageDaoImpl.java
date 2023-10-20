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

import io.maestro3.agent.dao.MachineImageDao;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.general.MachineImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;


@Repository
public class MachineImageDaoImpl implements MachineImageDao {

    private final static String COLLECTION_NAME = "MachineImages";

    private MongoOperations mongoOperations;

    public MachineImageDaoImpl(@Autowired MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public MachineImage findByAliasForProject(String imageNameAlias, String tenantId, String regionId) {
        Assert.hasLength(imageNameAlias, "imageNameAlias cannot be null or empty");
        Assert.hasLength(tenantId, "tenantId cannot be null or empty");
        Assert.hasLength(regionId, "regionId cannot be null or empty");

        Criteria searchCriteria = Criteria.where("nameAlias").is(imageNameAlias)
                .and("regionId").is(regionId)
                .orOperator(
                        Criteria.where("tenantId").exists(false),
                        Criteria.where("tenantId").is(tenantId));

        return mongoOperations.findOne(Query.query(searchCriteria), MachineImage.class, COLLECTION_NAME);
    }

    @Override
    public <T extends MachineImage> void insert(List<T> imagesConfig) {
        Assert.notEmpty(imagesConfig, "imagesConfig cannot be null or empty");
        for (T imageConfig : imagesConfig) {
            assertParametersValid(imageConfig);
        }

        mongoOperations.insert(imagesConfig, COLLECTION_NAME);
    }

    @Override
    public <T extends MachineImage> void save(T imageConfig) {
        Assert.notNull(imageConfig, "imagesConfig cannot be null or empty");
        assertParametersValid(imageConfig);

        mongoOperations.save(imageConfig, COLLECTION_NAME);
    }

    @Override
    public MachineImage findByNativeId(String nativeId) {
        CriteriaDefinition searchCriteria = Criteria.where("nativeId").is(nativeId);

        return mongoOperations.findOne(Query.query(searchCriteria), MachineImage.class, COLLECTION_NAME);
    }

    @Override
    public String findAlias(String tenant, String region, String id) {
        MachineImage byNativeId = findByNativeId(id);
        return byNativeId != null ? byNativeId.getNameAlias() : null;
    }

    @Override
    public PrivateCloudType getCloud() {
        return PrivateCloudType.OPEN_STACK;
    }

    @Override
    public List<MachineImage> findByRegionId(String regionId) {
        Assert.hasText(regionId, "Region id cannot be null or empty");

        CriteriaDefinition searchCriteria = Criteria.where("regionId").is(regionId);

        return mongoOperations.find(Query.query(searchCriteria), MachineImage.class, COLLECTION_NAME);
    }

    @Override
    public List<MachineImage> findByRegionIdAndTenantId(String regionId, String tenantId) {
        Assert.hasText(regionId, "Region id cannot be null or empty");

        CriteriaDefinition searchCriteria = Criteria.where("regionId").is(regionId)
            .and("tenantId").is(tenantId);

        return mongoOperations.find(Query.query(searchCriteria), MachineImage.class, COLLECTION_NAME);
    }

    @Override
    public void removeByNativeId(String nativeId) {
        Assert.hasText(nativeId, "nativeId cannot be null or empty");

        mongoOperations.remove(Query.query(Criteria.where("nativeId").is(nativeId)), COLLECTION_NAME);
    }

    private void assertParametersValid(MachineImage image) {
        Assert.notNull(image, "Image cannot be null");
        Assert.hasText(image.getNameAlias(), "Image.NameAlias cannot be null or empty");
        Assert.hasText(image.getRegionId(), "Image.RegionId cannot be null or empty");
        Assert.notNull(image.getPlatformType(), "Image.PlatformType cannot be null");
    }
}
