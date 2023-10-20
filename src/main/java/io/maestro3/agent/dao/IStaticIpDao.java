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

package io.maestro3.agent.dao;

import io.maestro3.agent.model.network.impl.DomainType;
import io.maestro3.agent.model.network.impl.ip.IPState;
import io.maestro3.agent.model.network.impl.ip.OpenStackPort;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.tenant.OpenStackTenant;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface IStaticIpDao {

    void save(StaticIpAddress staticIpAddress);

    void update(StaticIpAddress staticIpAddress);

    void delete(String id);

    List<StaticIpAddress> findStaticIpAddresses(String zoneId, String projectId, List<String> ipAddresses, DomainType domainType, List<String> instanceIds);

    StaticIpAddress findStaticIpAddress(String zoneId, String projectId, String ipAddress);

    StaticIpAddress findById(String staticIpAddressId);

    List<StaticIpAddress> findNotAssociatedStaticIps(String zoneId, String projectId, DomainType domainType);

    List<StaticIpAddress> findNotAssociatedAndNotReservedStaticIps(String zoneId, String projectId, DomainType domainType);

    List<StaticIpAddress> findStaticIpAddressByInstanceId(String zoneId, String projectId, String instanceId);

    StaticIpAddress findReservedByInstance(String zoneId, String projectId, String instanceId);

    OpenStackPort findPortById(String zoneId, String projectId, String portId);

    List<StaticIpAddress> findStaticIpAddressesWithCurrentTask(String zoneId, String projectId, Set<String> taskTypes);

    List<StaticIpAddress> findAssociatedStaticIps(String zoneId, String projectId);

    List<StaticIpAddress> findAssociatedStaticIPs(Collection<OpenStackTenant> projects);

    List<String> findProjectIdsWithStaticIpsByState(String zoneId, IPState... states);
}
