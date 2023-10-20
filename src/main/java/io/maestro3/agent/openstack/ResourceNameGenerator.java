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

package io.maestro3.agent.openstack;

import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.model.general.PersistenceCounter;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.service.PersistenceCountersService;
import io.maestro3.cadf.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public final class ResourceNameGenerator {

    private final PersistenceCountersService persistenceCountersService;

    @Autowired
    public ResourceNameGenerator(PersistenceCountersService persistenceCountersService) {
        this.persistenceCountersService = persistenceCountersService;
    }

    public String generateNewServerName(OpenStackRegionConfig regionConfig) throws M3PrivateAgentException {
       return generateNewServerName(regionConfig, this.persistenceCountersService);
    }

    public static String generateNewServerName(OpenStackRegionConfig regionConfig,
                                               PersistenceCountersService persistenceCountersService) throws M3PrivateAgentException {
        PersistenceCounter counter = persistenceCountersService.findInstanceInZoneCounter(regionConfig.getId());
        Assert.notNull(counter, "Region configuration is broken. Servers launch not available.");

        int value = (int) counter.getValue();
        return generateName(regionConfig.getServerNamePrefix(), regionConfig.getRegionNumber(), value);
    }

    private static String generateName(String prefix, int first, int second) {
        return getDnsNameCompletePrefix(prefix, first) + String.format("%04X", second);
    }

    private static String getDnsNameCompletePrefix(String prefix, int indexNumber) {
        // i.e. ECS000 + 6 + 4 = ECS00064
        String zoneIndex = Integer.toHexString(indexNumber);
        return prefix + zoneIndex + 0;
    }
}
