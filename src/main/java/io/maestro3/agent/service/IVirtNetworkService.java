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

import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.network.impl.ip.parameters.AllocateStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.AssociateStaticIpAddressParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DescribeStaticIpAddressesParameters;
import io.maestro3.agent.model.network.impl.ip.parameters.DisassociateStaticIpAddressParameters;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;

import java.util.List;

public interface IVirtNetworkService {

    List<StaticIpAddress> describeStaticIps(OpenStackTenant project, DescribeStaticIpAddressesParameters parameters);

    StaticIpAddress allocateStaticIp(OpenStackTenant project, AllocateStaticIpAddressParameters parameters);

    StaticIpAddress associateStaticIp(OpenStackTenant project, AssociateStaticIpAddressParameters parameters);

    StaticIpAddress disassociateStaticIp(OpenStackTenant project, DisassociateStaticIpAddressParameters parameters);

    boolean releaseStaticIp(OpenStackTenant project, String staticIp);
}
