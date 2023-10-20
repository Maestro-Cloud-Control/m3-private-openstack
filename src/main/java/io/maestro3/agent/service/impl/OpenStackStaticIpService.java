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

import io.maestro3.agent.model.network.impl.DomainType;
import io.maestro3.agent.model.network.impl.ip.OpenStackPort;
import io.maestro3.agent.model.network.impl.ip.OpenStackStaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.service.IOpenStackStaticIpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Service
@Qualifier("openStackStaticIpService")
public class OpenStackStaticIpService extends StaticIpService implements IOpenStackStaticIpService {

    @Override
    public OpenStackStaticIpAddress findReservedByInstance(String zoneId, String projectId, String instanceId) {
        Assert.hasText(instanceId, "instanceId cannot be null or empty.");
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        return (OpenStackStaticIpAddress) staticIpDao.findReservedByInstance(zoneId, projectId, instanceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<OpenStackStaticIpAddress> findNotAssociatedAndNotReservedStaticIps(String zoneId, String projectId,
                                                                                         DomainType domainType) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");

        List<StaticIpAddress> staticIps = staticIpDao.findNotAssociatedAndNotReservedStaticIps(zoneId, projectId, domainType);
        return convert(staticIps);
    }

    private Collection<OpenStackStaticIpAddress> convert(List<StaticIpAddress> staticIps) {
        return Optional.ofNullable(staticIps)
                .stream()
                .filter(OpenStackStaticIpAddress.class::isInstance)
                .map(OpenStackStaticIpAddress.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public OpenStackPort findPortById(String zoneId, String projectId, String portId) {
        Assert.hasText(projectId, "project cannot be null or empty.");
        Assert.hasText(zoneId, "zoneId cannot be null or empty.");
        Assert.hasText(portId, "portId cannot be null or empty.");

        return staticIpDao.findPortById(zoneId, projectId, portId);
    }
}
