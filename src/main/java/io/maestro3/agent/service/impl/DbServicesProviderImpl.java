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

import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.agent.service.KeyPairDbService;
import io.maestro3.agent.service.MachineImageDbService;
import io.maestro3.agent.service.PersistenceCountersService;
import io.maestro3.agent.service.ScriptTemplateDbService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.agent.service.VolumeDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DbServicesProviderImpl implements DbServicesProvider {

    private KeyPairDbService keyPairDbService;
    private MachineImageDbService machineImageDbService;
    private ScriptTemplateDbService scriptTemplateDbService;
    private ServerDbService serverDbService;
    private TenantDbService tenantDbService;
    private PersistenceCountersService countersService;
    private VolumeDbService volumeDbService;

    @Autowired
    public DbServicesProviderImpl(KeyPairDbService keyPairDbService,
                                  MachineImageDbService machineImageDbService,
                                  ScriptTemplateDbService scriptTemplateDbService,
                                  ServerDbService serverDbService,
                                  TenantDbService tenantDbService,
                                  PersistenceCountersService countersService,
                                  VolumeDbService volumeDbService) {
        this.keyPairDbService = keyPairDbService;
        this.machineImageDbService = machineImageDbService;
        this.scriptTemplateDbService = scriptTemplateDbService;
        this.serverDbService = serverDbService;
        this.tenantDbService = tenantDbService;
        this.countersService = countersService;
        this.volumeDbService = volumeDbService;
    }

    @Override
    public KeyPairDbService getKeyPairDbService() {
        return keyPairDbService;
    }

    @Override
    public MachineImageDbService getMachineImageDbService() {
        return machineImageDbService;
    }

    @Override
    public ScriptTemplateDbService getScriptTemplateDbService() {
        return scriptTemplateDbService;
    }

    @Override
    public ServerDbService getServerDbService() {
        return serverDbService;
    }

    @Override
    public TenantDbService getTenantDbService() {
        return tenantDbService;
    }

    @Override
    public PersistenceCountersService getPersistenceCountersService() {
        return countersService;
    }

    @Override
    public VolumeDbService getVolumeDbService() {
        return volumeDbService;
    }
}
