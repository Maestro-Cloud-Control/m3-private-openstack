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

import io.maestro3.agent.dao.IStaticIpDao;
import io.maestro3.agent.model.network.impl.DomainType;
import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.IStaticIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;


@Service
public class StaticIpService implements IStaticIpService {

    @Autowired
    protected IStaticIpDao staticIpDao;

    @Override
    public void save(StaticIpAddress staticIpAddress) {
        Assert.notNull(staticIpAddress, "staticIpAddress cannot be null.");
        this.staticIpDao.save(staticIpAddress);
    }

    @Override
    public void update(StaticIpAddress staticIpAddress) {
        Assert.notNull(staticIpAddress, "staticIpAddress cannot be null.");
        this.staticIpDao.update(staticIpAddress);
    }

    @Override
    public void delete(StaticIpAddress staticIpAddress) {
        Assert.notNull(staticIpAddress, "staticIpAddress cannot be null.");
        Assert.hasText(staticIpAddress.getId(), "staticIpAddress.id cannot be null or empty.");
        this.staticIpDao.delete(staticIpAddress.getId());
    }

    @Override
    public StaticIpAddress findById(String id) {
        Assert.notNull(id, "id cannot be null or empty.");

        return this.staticIpDao.findById(id);
    }

    @Override
    public List<StaticIpAddress> findStaticIpAddresses(String zoneId, String projectId, List<String> ipAddresses) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        return this.staticIpDao.findStaticIpAddresses(zoneId, projectId, ipAddresses, null, null);
    }

    @Override
    public List<StaticIpAddress> findNotAssociatedStaticIps(String zoneId, String projectId, DomainType domainType) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        return this.staticIpDao.findNotAssociatedStaticIps(zoneId, projectId, domainType);
    }

    @Override
    public List<StaticIpAddress> findAssociatedStaticIps(String zoneId, String projectId) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        return this.staticIpDao.findAssociatedStaticIps(zoneId, projectId);
    }

    @Override
    public List<StaticIpAddress> findStaticIpAddresses(String zoneId, String projectId, List<String> ipAddresses, DomainType domainType, List<String> instanceIds) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        return this.staticIpDao.findStaticIpAddresses(zoneId, projectId, ipAddresses, domainType, instanceIds);
    }

    @Override
    public StaticIpAddress findStaticIpAddress(String zoneId, String projectId, String ipAddress) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");
        Assert.hasText(ipAddress, "publicIp cannot be null or empty.");

        return this.staticIpDao.findStaticIpAddress(zoneId, projectId, ipAddress);
    }

    @Override
    public List<StaticIpAddress> findStaticIpAddressByInstanceId(String zoneId, String projectId, String instanceId) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");
        Assert.hasText(instanceId, "instanceId cannot be null or empty.");

        return this.staticIpDao.findStaticIpAddressByInstanceId(zoneId, projectId, instanceId);
    }

    @Override
    public List<StaticIpAddress> findAssociatedStaticIPs(Collection<OpenStackTenant> projects) {
        Assert.notEmpty(projects, "projects can not be null or empty");
        return staticIpDao.findAssociatedStaticIPs(projects);
    }

    @Override
    public List<String> findProjectIdsWithStaticIpsByState(String zoneId, IPState... states) {
        Assert.hasText(zoneId, "zoneId can not be null or empty");
        Assert.notNull(states, "states can not be null or empty");
        return staticIpDao.findProjectIdsWithStaticIpsByState(zoneId, states);
    }
}
