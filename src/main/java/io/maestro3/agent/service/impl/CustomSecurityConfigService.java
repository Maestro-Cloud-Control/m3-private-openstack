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

import io.maestro3.agent.dao.ICustomSecurityConfigDao;
import io.maestro3.agent.model.network.CustomSecurityConfig;
import io.maestro3.agent.service.ICustomSecurityConfigService;
import org.springframework.util.Assert;


public abstract class CustomSecurityConfigService<T extends CustomSecurityConfig> implements ICustomSecurityConfigService<T> {

    private final ICustomSecurityConfigDao<T> configDao;

    protected CustomSecurityConfigService(ICustomSecurityConfigDao<T> configDao) {
        this.configDao = configDao;
    }

    @Override
    public void save(T securityConfig) {
        Assert.notNull(securityConfig, "securityConfig cannot be null");
        Assert.isNull(securityConfig.getId(), "securityConfig.id must be null.");
        configDao.save(securityConfig);
    }

    @Override
    public void update(T securityConfig) {
        Assert.notNull(securityConfig, "securityConfig cannot be null");
        Assert.hasText(securityConfig.getId(), "securityConfig.id can not be null or empty.");
        configDao.save(securityConfig);
    }

    @Override
    public void delete(String id) {
        Assert.hasText(id, "id can not be null or empty.");
        configDao.delete(id);
    }
}
