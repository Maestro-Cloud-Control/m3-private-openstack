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

import io.maestro3.agent.dao.IOpenStackCustomSecurityConfigDao;
import io.maestro3.agent.model.network.Direction;
import io.maestro3.agent.model.network.SecurityConfigType;
import io.maestro3.agent.model.network.SecurityGroupType;
import io.maestro3.agent.model.network.impl.SecurityGroupExtension;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Service
public class OpenStackCustomSecurityConfigDao extends CustomSecurityConfigDao<SecurityGroupExtension> implements IOpenStackCustomSecurityConfigDao {

    public OpenStackCustomSecurityConfigDao() {
        super(SecurityGroupExtension.class, SecurityConfigType.OPEN_STACK);
    }

    @Override
    public List<SecurityGroupExtension> find(Set<String> zoneIds, SecurityGroupType securityGroupType, Direction direction) {
        Assert.notEmpty(zoneIds, "zoneIds can't be null or empty.");
        Criteria criteria = where("type").is(SecurityConfigType.OPEN_STACK).and("zoneId").in(zoneIds);
        if (securityGroupType != null) {
            criteria.and("securityGroupType").is(securityGroupType);
        }
        if (direction != null) {
            criteria.and("direction").is(direction);
        }
        return findAll(query(criteria));
    }
}
