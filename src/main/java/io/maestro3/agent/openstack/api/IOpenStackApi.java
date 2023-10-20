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

package io.maestro3.agent.openstack.api;

import io.maestro3.agent.openstack.api.compute.IComputeService;
import io.maestro3.agent.openstack.api.identity.IKeystoneService;
import io.maestro3.agent.openstack.api.images.IImagesService;
import io.maestro3.agent.openstack.api.networking.INetworkingService;
import io.maestro3.agent.openstack.api.storage.IBlockStorageService;
import io.maestro3.agent.openstack.api.telemetry.ITelemetryService;


public interface IOpenStackApi {

    /**
     * @return Open Stack Compute (Nova) service.
     */
    IComputeService compute();

    /**
     * @return Open Stack Telemetry (Ceilometer) service.
     */
    ITelemetryService telemetry();

    /**
     * @return Open Stack Keystone (Identity) service.
     */
    IKeystoneService keystone();

    /**
     * @return Open Stack Networking (Neutron) service.
     */
    INetworkingService networking();

    /**
     * @return Open Stack Block Storage (Cinder) service.
     */
    IBlockStorageService blockStorage();

    /**
     * @return Open Stack Images (Glance) service.
     */
    IImagesService images();
}
