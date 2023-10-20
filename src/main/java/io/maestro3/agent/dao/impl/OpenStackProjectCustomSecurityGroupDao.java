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

import io.maestro3.agent.dao.IOpenStackProjectCustomSecurityGroupDao;
import io.maestro3.agent.model.network.SecurityConfigType;
import io.maestro3.agent.model.network.impl.OpenStackProjectCustomSecurityGroup;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;


@Service
public class OpenStackProjectCustomSecurityGroupDao extends CustomSecurityConfigDao<OpenStackProjectCustomSecurityGroup>
    implements IOpenStackProjectCustomSecurityGroupDao {

    public OpenStackProjectCustomSecurityGroupDao() {
        super(OpenStackProjectCustomSecurityGroup.class, SecurityConfigType.OPEN_STACK_PROJECT_CUSTOM);
    }

    @Override
    public OpenStackProjectCustomSecurityGroup find(String projectId, String name) {
        Assert.hasText(projectId, "projectId can not be null or empty");
        Assert.hasText(name, "name can not be null or empty");

        Criteria searchCriteria = projectCriteria(projectId).and("name").is(name.toLowerCase());

        return findOne(Query.query(searchCriteria));
    }

    @Override
    public Collection<OpenStackProjectCustomSecurityGroup> findAll(String projectId) {
        Assert.hasText(projectId, "projectId can not be null or empty");

        return findAll(Query.query(projectCriteria(projectId)));
    }

    @Override
    public Collection<OpenStackProjectCustomSecurityGroup> findForInstance(String projectId, String instanceId) {
        Assert.hasText(projectId, "projectId can not be null or empty");
        Assert.hasText(instanceId, "instanceId can not be null or empty");

        Criteria searchCriteria = projectCriteria(projectId).orOperator(Criteria.where("instanceIds").is(instanceId.toLowerCase()),
            Criteria.where("all").is(true));
        return findAll(Query.query(searchCriteria));
    }

    @Override
    public void remove(String projectId, String instanceId) {
        Assert.hasText(projectId, "projectId can not be null or empty");
        Assert.hasText(instanceId, "instanceId can not be null or empty");

        Update update = new Update().pull("instanceIds", instanceId.toLowerCase());
        modifyAll(Query.query(projectCriteria(projectId)), update);
    }

    @Override
    public Collection<OpenStackProjectCustomSecurityGroup> findForAllInstances(String projectId) {
        Assert.hasText(projectId, "projectId can not be null or empty");

        Criteria criteria = projectCriteria(projectId).and("all").is(true);
        return findAll(Query.query(criteria));
    }

    private Criteria projectCriteria(String projectId) {
        return baseCriteria().and("projectId").is(projectId);
    }

    private Criteria baseCriteria() {
        return Criteria.where("type").is(type);
    }
}
