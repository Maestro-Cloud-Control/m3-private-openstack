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
import io.maestro3.agent.openstack.api.compute.impl.ComputeService;
import io.maestro3.agent.openstack.api.identity.IKeystoneService;
import io.maestro3.agent.openstack.api.identity.impl.KeystoneService;
import io.maestro3.agent.openstack.api.images.IImagesService;
import io.maestro3.agent.openstack.api.images.impl.ImagesService;
import io.maestro3.agent.openstack.api.networking.INetworkingService;
import io.maestro3.agent.openstack.api.networking.impl.NetworkingService;
import io.maestro3.agent.openstack.api.storage.IBlockStorageService;
import io.maestro3.agent.openstack.api.storage.impl.BlockStorageService;
import io.maestro3.agent.openstack.api.telemetry.ITelemetryService;
import io.maestro3.agent.openstack.api.telemetry.impl.TelemetryService;
import io.maestro3.agent.openstack.client.IOSClient;


public class OpenStackApi implements IOpenStackApi {

    private IComputeService computeService;
    private ITelemetryService telemetryService;
    private IKeystoneService keystoneService;
    private INetworkingService networkingService;
    private IBlockStorageService blockStorageService;
    private IImagesService imagesService;

    public OpenStackApi(IOSClient client) {
        this.computeService = new ComputeService(client);
        this.telemetryService = new TelemetryService(client);
        this.keystoneService = new KeystoneService(client);
        this.networkingService = new NetworkingService(client);
        this.blockStorageService = new BlockStorageService(client);
        this.imagesService = new ImagesService(client);
    }

    public static IOpenStackApi wrap(IOSClient client) {
        return new OpenStackApi(client);
    }

    @Override
    public IComputeService compute() {
        return computeService;
    }

    @Override
    public ITelemetryService telemetry() {
        return telemetryService;
    }

    @Override
    public IKeystoneService keystone() {
        return keystoneService;
    }

    @Override
    public INetworkingService networking() {
        return networkingService;
    }

    @Override
    public IBlockStorageService blockStorage() {
        return blockStorageService;
    }

    @Override
    public IImagesService images() {
        return imagesService;
    }
}
