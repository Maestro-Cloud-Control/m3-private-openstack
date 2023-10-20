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

import io.maestro3.agent.dao.PersistenceCountersDao;
import io.maestro3.agent.model.enums.CounterType;
import io.maestro3.agent.model.general.PersistenceCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;


@Repository
public class PersistenceCountersDaoImpl implements PersistenceCountersDao {

    private final static String COLLECTION_NAME = "PersistenceCounters";

    private MongoOperations mongoOperations;

    public PersistenceCountersDaoImpl(@Autowired MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void save(PersistenceCounter counter) {
        Assert.notNull(counter, "counter cannot be null");

        mongoOperations.save(counter, COLLECTION_NAME);
    }

    @Override
    public PersistenceCounter findOfTypeByBoundResourceId(CounterType counterType, String resourceId) {
        Assert.notNull(counterType, "counterType cannot be null");
        Assert.hasLength(resourceId, "resourceId cannot be null or empty");

        CriteriaDefinition searchCriteria = Criteria.where("counterType").is(counterType)
                .and("counterBoundResourceId").is(resourceId);

        return mongoOperations.findAndModify(Query.query(searchCriteria), new Update().inc("value", 1),
                PersistenceCounter.class, COLLECTION_NAME);
    }
}
