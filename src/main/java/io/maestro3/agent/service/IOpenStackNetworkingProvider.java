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

import io.maestro3.agent.model.InstanceProvisioningProgress;
import io.maestro3.agent.model.network.StartupNetworkingConfiguration;
import io.maestro3.agent.model.network.impl.ip.EnsureIpAddressInfo;
import io.maestro3.agent.model.network.impl.ip.MoveIpAddressInfo;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.sdk.v3.request.instance.RunInstanceRequest;

import java.util.List;
import java.util.Map;

public interface IOpenStackNetworkingProvider {

    String getDefaultNetworkId(OpenStackTenant project);

    String getDefaultSecurityGroupId(OpenStackTenant project);

    boolean isDefaultSecurityGroupRequired();

    void processIpAddressesOnInstanceTerminate(OpenStackTenant project, OpenStackServerConfig instance);


    void processStaticIpOnInstanceMoveToProject(OpenStackTenant project, OpenStackServerConfig instance);

    void assertInstanceNetworkAvailableInTargetProject(OpenStackTenant fromProject, OpenStackTenant toProject, OpenStackServerConfig instance);

    MoveIpAddressInfo moveIpAddressToAnotherProject(OpenStackTenant fromProject, OpenStackTenant toProject, OpenStackServerConfig instance);

    EnsureIpAddressInfo ensureIpAddressForInstanceCameFromAnotherProject(OpenStackTenant project, OpenStackServerConfig instance, Map<String, String> metadata);

    // Instance provisioning
    void commit(InstanceProvisioningProgress progress);

    void rollback(InstanceProvisioningProgress progress);

    // Processing instance in updater
    String ensureStaticIpAssociated(OpenStackTenant project, OpenStackServerConfig existing, OpenStackServerConfig incoming);

    /**
     * Deletes IP address on previous project and creates on new one.
     * IP addresses are equal. If failed to create the same IP address on the new project, creates the one, that is available in DHCP pool.
     *
     * @return IP address unique ID.
     */
    StartupNetworkingConfiguration getNetworkConfiguration(OpenStackTenant project,
                                                           List<String> networks, String ipAddress);

    void initializeWhileStartup(ServerBootInfo.Builder bootBuilder, InstanceProvisioningProgress progress,
                                StartupNetworkingConfiguration networkingConfiguration);

    void deleteInstancePorts(OpenStackRegionConfig regionConfig, OpenStackTenant project, OpenStackServerConfig instance);

    IVirtOpenStackNetworkService networkingService();
}
