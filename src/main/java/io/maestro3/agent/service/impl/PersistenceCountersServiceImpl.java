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

import io.maestro3.agent.dao.PersistenceCountersDao;
import io.maestro3.agent.model.enums.CounterType;
import io.maestro3.agent.model.general.PersistenceCounter;
import io.maestro3.agent.service.PersistenceCountersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PersistenceCountersServiceImpl implements PersistenceCountersService {

    private PersistenceCountersDao countersDao;

    public PersistenceCountersServiceImpl(@Autowired PersistenceCountersDao countersDao) {
        this.countersDao = countersDao;
    }

    @Override
    public void addInstanceInZoneCounter(String regionId) {
        PersistenceCounter persistenceCounter = new PersistenceCounter();
        persistenceCounter.setCounterType(CounterType.INSTANCE_IN_ZONE);
        persistenceCounter.setCounterBoundResourceId(regionId);
        persistenceCounter.setValue(0);

        countersDao.save(persistenceCounter);

    }

    @Override
    public PersistenceCounter findInstanceInZoneCounter(String regionId) {
        return countersDao.findOfTypeByBoundResourceId(CounterType.INSTANCE_IN_ZONE, regionId);
    }
}
