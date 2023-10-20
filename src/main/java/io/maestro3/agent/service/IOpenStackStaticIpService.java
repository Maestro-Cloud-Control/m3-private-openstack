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

package io.maestro3.agent.service;

import io.maestro3.agent.model.network.impl.DomainType;
import io.maestro3.agent.model.network.impl.ip.OpenStackPort;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;

import java.util.Collection;


public interface IOpenStackStaticIpService extends IStaticIpService {

    OpenStackStaticIpAddress findReservedByInstance(String zoneId, String projectId, String instanceId);

    Collection<OpenStackStaticIpAddress> findNotAssociatedAndNotReservedStaticIps(String zoneId, String projectId,
                                                                                  DomainType domainType);


    OpenStackPort findPortById(String zoneId, String projectId, String portId);
}
