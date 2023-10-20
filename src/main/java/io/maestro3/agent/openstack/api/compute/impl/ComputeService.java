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

package io.maestro3.agent.openstack.api.compute.impl;

import io.maestro3.agent.openstack.api.compute.IComputeImageService;
import io.maestro3.agent.openstack.api.compute.IComputeService;
import io.maestro3.agent.openstack.api.compute.IFlavorService;
import io.maestro3.agent.openstack.api.compute.IPortInterfaceExtension;
import io.maestro3.agent.openstack.api.compute.IQuotasExtension;
import io.maestro3.agent.openstack.api.compute.IServerService;
import io.maestro3.agent.openstack.api.compute.extension.IAvailabilityZonesExtension;
import io.maestro3.agent.openstack.api.compute.extension.IHostAggregatesExtension;
import io.maestro3.agent.openstack.api.compute.extension.IHypervisorsExtension;
import io.maestro3.agent.openstack.api.compute.extension.IKeyPairExtension;
import io.maestro3.agent.openstack.api.compute.extension.IMetadataExtension;
import io.maestro3.agent.openstack.api.compute.extension.impl.AvailabilityZonesExtension;
import io.maestro3.agent.openstack.api.compute.extension.impl.HostAggregatesExtension;
import io.maestro3.agent.openstack.api.compute.extension.impl.HypervisorsExtension;
import io.maestro3.agent.openstack.api.compute.extension.impl.KeyPairExtension;
import io.maestro3.agent.openstack.api.compute.extension.impl.MetadataExtension;
import io.maestro3.agent.openstack.api.compute.impl.servers.ServerServiceDelegate;
import io.maestro3.agent.openstack.client.IOSClient;


public class ComputeService implements IComputeService {

    private IFlavorService flavorService;
    private IComputeImageService computeImageService;
    private IServerService serverService;
    private IQuotasExtension quotasExtension;
    private IKeyPairExtension keyPairExtension;
    private IMetadataExtension metadataExtension;
    private IPortInterfaceExtension portInterfaceExtension;
    private IHostAggregatesExtension hostAggregatesExtension;
    private IHypervisorsExtension hypervisorsExtension;
    private IAvailabilityZonesExtension availabilityZonesExtension;

    public ComputeService(IOSClient client) {
        flavorService = new FlavorService(client);
        computeImageService = new ComputeImageService(client);
        serverService = new ServerServiceDelegate(client);
        quotasExtension = new QuotasExtension(client);
        keyPairExtension = new KeyPairExtension(client);
        metadataExtension = new MetadataExtension(client);
        portInterfaceExtension = new PortInterfaceExtension(client);
        hostAggregatesExtension = new HostAggregatesExtension(client);
        hypervisorsExtension = new HypervisorsExtension(client);
        availabilityZonesExtension = new AvailabilityZonesExtension(client);
    }

    @Override
    public IFlavorService flavors() {
        return flavorService;
    }

    @Override
    public IComputeImageService images() {
        return computeImageService;
    }

    @Override
    public IServerService servers() {
        return serverService;
    }

    @Override
    public IKeyPairExtension keyPairs() {
        return keyPairExtension;
    }

    @Override
    public IMetadataExtension metadata() {
        return metadataExtension;
    }

    @Override
    public IPortInterfaceExtension portInterfaces() {
        return portInterfaceExtension;
    }

    @Override
    public IQuotasExtension quotas() {
        return quotasExtension;
    }

    @Override
    public IHostAggregatesExtension hostAggregates() {
        return hostAggregatesExtension;
    }

    @Override
    public IHypervisorsExtension hypervisors() {
        return hypervisorsExtension;
    }

    @Override
    public IAvailabilityZonesExtension availabilityZones() {
        return availabilityZonesExtension;
    }
}
