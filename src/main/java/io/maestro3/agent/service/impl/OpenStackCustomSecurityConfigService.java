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

import io.maestro3.agent.dao.IOpenStackCustomSecurityConfigDao;
import io.maestro3.agent.model.network.Direction;
import io.maestro3.agent.model.network.SecurityGroupType;
import io.maestro3.agent.model.network.impl.SecurityGroupExtension;
import io.maestro3.agent.service.IOpenStackCustomSecurityConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;


@Service
public class OpenStackCustomSecurityConfigService extends CustomSecurityConfigService<SecurityGroupExtension> implements IOpenStackCustomSecurityConfigService {

    private final IOpenStackCustomSecurityConfigDao openStackCustomSecurityConfigDao;

    @Autowired
    private OpenStackCustomSecurityConfigService(IOpenStackCustomSecurityConfigDao openStackCustomSecurityConfigDao) {
        super(openStackCustomSecurityConfigDao);
        this.openStackCustomSecurityConfigDao = openStackCustomSecurityConfigDao;
    }

    @Override
    public List<SecurityGroupExtension> findByZoneIds(Set<String> zoneIds, SecurityGroupType securityGroupType, Direction direction) {
        Assert.notEmpty(zoneIds, "zoneIds can not be null or empty");
        return openStackCustomSecurityConfigDao.find(zoneIds, securityGroupType, direction);
    }
}
