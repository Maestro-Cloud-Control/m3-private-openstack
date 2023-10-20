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

import com.google.common.collect.Lists;
import io.maestro3.agent.dao.BaseDao;
import io.maestro3.agent.dao.IStaticIpDao;
import io.maestro3.agent.model.network.impl.DomainType;
import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.OpenStackPort;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Repository
public class StaticIpDao extends BaseDao<StaticIpAddress> implements IStaticIpDao {

    public static final String COLLECTION_NAME = "StaticIPs";

    public StaticIpDao() {
        super(COLLECTION_NAME, StaticIpAddress.class);
    }

    public void ensureIndexes() {
        this.ensureIndex(new Index("zoneId", Sort.Direction.ASC).on("tenantId", Sort.Direction.ASC).on("ipAddress", Sort.Direction.ASC).unique());
        this.ensureIndex(new Index("zoneId", Sort.Direction.ASC).on("tenantId", Sort.Direction.ASC).on("instanceId", Sort.Direction.ASC).on("ipAddress", Sort.Direction.ASC));
    }

    public List<StaticIpAddress> findStaticIpAddresses(String zoneId, String tenantId, List<String> ipAddresses, DomainType domainType, List<String> instanceIds) {
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        Criteria criteria = new Criteria("zoneId").is(zoneId).and("tenantId").is(tenantId);

        if (domainType != null) {
            criteria.and("domainType").is(domainType);
        }

        List<Criteria> searchCharacteristics = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(ipAddresses)) {
            searchCharacteristics.add(Criteria.where("ipAddress").in(ipAddresses));
        }
        if (CollectionUtils.isNotEmpty(instanceIds)) {
            searchCharacteristics.add(Criteria.where("instanceId").in(StringUtils.toLowerCaseList(instanceIds)));
        }

        if (CollectionUtils.isNotEmpty(searchCharacteristics)) {
            criteria.orOperator(searchCharacteristics.toArray(new Criteria[0]));
        }

        return super.findAll(query(criteria));
    }

    @Override
    public StaticIpAddress findStaticIpAddress(String zoneId, String tenantId, String ipAddress) {
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");
        Assert.hasText(ipAddress, "ipAddress cannot be null or empty.");

        return super.findOne(query(where("zoneId").is(zoneId).and("tenantId").is(tenantId).and("ipAddress").is(ipAddress)));
    }

    @Override
    public List<StaticIpAddress> findNotAssociatedStaticIps(String zoneId, String tenantId, DomainType domainType) {
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        Criteria criteria = findNotAssociatedStaticIpsCriteria(zoneId, tenantId, domainType);

        return super.findAll(query(criteria));
    }

    @Override
    public List<StaticIpAddress> findNotAssociatedAndNotReservedStaticIps(String zoneId, String tenantId, DomainType domainType) {
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        Criteria criteria = findNotAssociatedStaticIpsCriteria(zoneId, tenantId, domainType);
        criteria.and("reservedBy").is(null);
        return super.findAll(query(criteria));
    }

    private Criteria findNotAssociatedStaticIpsCriteria(String zoneId, String tenantId, DomainType domainType) {
        Criteria criteria = new Criteria("zoneId").is(zoneId).and("tenantId").is(tenantId).and("instanceId").is(null);

        if (domainType != null) {
            criteria.and("domainType").is(domainType);
        }
        return criteria;
    }

    @Override
    public List<StaticIpAddress> findStaticIpAddressByInstanceId(String zoneId, String tenantId, String instanceId) {
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");
        Assert.hasText(instanceId, "ipAddress cannot be null or empty.");

        return super.findAll(query(where("zoneId").is(zoneId).and("tenantId").is(tenantId).and("instanceId").is(instanceId.toLowerCase())));
    }

    @Override
    public StaticIpAddress findReservedByInstance(String zoneId, String tenantId, String instanceId) {
        Assert.hasText(instanceId, "instanceId cannot be null or empty.");
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        return super.findOne(query(where("zoneId").is(zoneId).and("tenantId").is(tenantId).and("reservedBy").is(instanceId.toLowerCase())));
    }

    @Override
    public OpenStackPort findPortById(String zoneId, String tenantId, String portId) {
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(portId, "portId cannot be null or empty.");

        Criteria criteria = where("zoneId").is(zoneId)
            .and("tenantId").is(tenantId)
            .and("portId").is(portId);
        return (OpenStackPort) super.findOne(query(criteria));
    }

    @Override
    public List<StaticIpAddress> findStaticIpAddressesWithCurrentTask(String zoneId, String tenantId, Set<String> taskTypes) {
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");
        Assert.hasText(tenantId, "tenantId cannot be null or empty.");
        Assert.notEmpty(taskTypes, "taskTypes cannot be null or empty.");

        Criteria criteria = where("zoneId").is(zoneId)
            .and("tenantId").is(tenantId)
            .and("currentTask.name").in(taskTypes);
        return super.findAll(Query.query(criteria));
    }

    @Override
    public List<StaticIpAddress> findAssociatedStaticIps(String zoneId, String tenantId) {
        Assert.hasText(tenantId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        Criteria criteria = where("zoneId").is(zoneId).and("tenantId").is(tenantId).and("instanceId").exists(true);
        return super.findAll(Query.query(criteria));
    }

    @Override
    public List<StaticIpAddress> findAssociatedStaticIPs(Collection<OpenStackTenant> projects) {
        Assert.notEmpty(projects, "projects can not be null or empty");

        Set<String> tenantIds = projects.stream()
            .map(OpenStackTenant::getId)
            .collect(Collectors.toSet());
        Criteria searchCriteria = where("tenantId").in(tenantIds).and("instanceId").exists(true);
        return super.findAll(query(searchCriteria));
    }

    @Override
    public List<String> findProjectIdsWithStaticIpsByState(String zoneId,
                                                          IPState... states) {
        Assert.hasText(zoneId, "zoneId can not be null or empty");
        Assert.notNull(states, "states can not be null or empty");

        List<String> statesStrings = Arrays.stream(states).map(IPState::name).collect(Collectors.toList());
        Criteria searchCriteria = where("zoneId").is(zoneId).and("ipState").in(statesStrings);
        return super.distinct(query(searchCriteria), "tenantId");
    }
}
