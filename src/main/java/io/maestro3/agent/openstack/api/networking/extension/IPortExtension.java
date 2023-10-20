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

package io.maestro3.agent.openstack.api.networking.extension;

import io.maestro3.agent.openstack.api.compute.bean.CreatePortRequest;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.exception.OSClientException;

import java.util.List;


public interface IPortExtension {

    Port create(String networkId, String securityGroupId, String name) throws OSClientException;

    Port create(String networkId, String securityGroupId, String name, String ipAddress) throws OSClientException;

    Port create(CreatePortRequest request) throws OSClientException;

    Port get(String portId) throws OSClientException;

    void delete(String portId) throws OSClientException;

    List<Port> list() throws OSClientException;

    List<Port> listByTenantId(String tenantId) throws OSClientException;

    List<Port> listByDeviceId(String deviceId) throws OSClientException;

    void clearDns(String portId) throws OSClientException;

    void updateSecurityGroups(String portId, List<String> securityGroups) throws OSClientException;

    void updateMacAddress(String portId, String macAddress) throws OSClientException;
}
