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

package io.maestro3.agent.service.impl;

import io.maestro3.agent.dao.IOpenStackProjectCustomSecurityGroupDao;
import io.maestro3.agent.model.network.impl.OpenStackProjectCustomSecurityGroup;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.service.IOpenStackProjectCustomSecurityGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;


@Service
public class OpenStackProjectCustomSecurityGroupService extends CustomSecurityConfigService<OpenStackProjectCustomSecurityGroup>
    implements IOpenStackProjectCustomSecurityGroupService {

    private final IOpenStackProjectCustomSecurityGroupDao projectCustomSecurityGroupDao;

    @Autowired
    protected OpenStackProjectCustomSecurityGroupService(IOpenStackProjectCustomSecurityGroupDao configDao) {
        super(configDao);
        this.projectCustomSecurityGroupDao = configDao;
    }

    @Override
    public OpenStackProjectCustomSecurityGroup find(String projectId, String name) {
        Assert.hasText(projectId, "projectId can not be null or empty");
        Assert.hasText(name, "name can not be null or empty");

        return projectCustomSecurityGroupDao.find(projectId, name);
    }

    @Override
    public Collection<OpenStackProjectCustomSecurityGroup> findAll(String projectId) {
        Assert.hasText(projectId, "projectId can not be null or empty");

        return projectCustomSecurityGroupDao.findAll(projectId);
    }

    @Override
    public Collection<OpenStackProjectCustomSecurityGroup> findForInstance(OpenStackServerConfig instance) {
        Assert.notNull(instance, "instance can not be null");

        return projectCustomSecurityGroupDao.findForInstance(instance.getTenantId(), instance.getNativeId());
    }

    @Override
    public Collection<OpenStackProjectCustomSecurityGroup> findForAllInstances(String projectId) {
        Assert.hasText(projectId, "projectId can not be null or empty");

        return projectCustomSecurityGroupDao.findForAllInstances(projectId);
    }

    @Override
    public void removeInstance(OpenStackServerConfig instance) {
        Assert.notNull(instance, "instance can not be null");

        projectCustomSecurityGroupDao.remove(instance.getTenantId(), instance.getNativeId());
    }
}
