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

import io.maestro3.agent.dao.BaseRegionDao;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class OpenStackRegionRepository extends BaseRegionDao<OpenStackRegionConfig> implements IOpenStackRegionRepository {

    @Autowired
    public OpenStackRegionRepository(MongoTemplate template) {
        super(template, PrivateCloudType.OPEN_STACK);
    }

    @Override
    public List<OpenStackRegionConfig> findAllOSRegionsAvailableForDescribers() {
        Criteria criteria = Criteria.where("enableScheduledDescribers").is(true);
        return template.find(Query.query(criteria), OpenStackRegionConfig.class, COLLECTION);
    }
}
