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

import io.maestro3.agent.dao.KeyPairDao;
import io.maestro3.agent.model.general.KeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;


@Repository
public class KeyPairDaoImpl implements KeyPairDao {

    private final static String COLLECTION_NAME = "KeyPairs";

    private MongoOperations mongoOperations;

    public KeyPairDaoImpl(@Autowired MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public KeyPair findByNameForProject(String keyName, String tenantId) {
        Assert.hasLength(keyName, "keyName cannot be null or empty");
        Assert.hasLength(tenantId, "tenantId cannot be null or empty");

        Criteria searchCriteria = Criteria.where("tenantId").is(tenantId)
                .and("nameAlias").is(keyName);

        return mongoOperations.findOne(Query.query(searchCriteria), KeyPair.class, COLLECTION_NAME);
    }
}
