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

import io.maestro3.agent.model.network.impl.OpenStackProjectCustomSecurityGroup;
import io.maestro3.agent.model.server.OpenStackServerConfig;

import java.util.Collection;


public interface IOpenStackProjectCustomSecurityGroupService extends ICustomSecurityConfigService<OpenStackProjectCustomSecurityGroup> {
    OpenStackProjectCustomSecurityGroup find(String projectId, String name);

    Collection<OpenStackProjectCustomSecurityGroup> findAll(String projectId);

    Collection<OpenStackProjectCustomSecurityGroup> findForInstance(OpenStackServerConfig instance);

    Collection<OpenStackProjectCustomSecurityGroup> findForAllInstances(String projectId);

    void removeInstance(OpenStackServerConfig instance);
}
