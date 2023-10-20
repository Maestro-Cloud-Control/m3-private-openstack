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

import io.maestro3.agent.model.compute.Flavor;
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.model.compute.RebootType;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.telemetry.Meter;
import io.maestro3.agent.openstack.api.IOpenStackApi;
import io.maestro3.agent.openstack.api.compute.bean.AvailabilityZone;
import io.maestro3.agent.openstack.api.compute.bean.CreatePortRequest;
import io.maestro3.agent.openstack.api.compute.bean.Hypervisor;
import io.maestro3.agent.openstack.api.compute.bean.NovaKeyPair;
import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.agent.openstack.api.compute.bean.VncConsole;
import io.maestro3.agent.openstack.api.networking.bean.FloatingIp;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.NovaSubnet;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;
import io.maestro3.agent.openstack.api.storage.bean.CinderBackendStoragePoolInfo;
import io.maestro3.agent.openstack.api.storage.bean.CinderHost;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.api.storage.bean.CreateCinderVolumeParameters;
import io.maestro3.agent.openstack.api.storage.bean.VolumeSnapshot;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.filter.impl.NetworkApiFilter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public interface IOpenStackClient {

    Server bootServer(ServerBootInfo bootInfo) throws OSClientException;

    List<Server> getServerList() throws OSClientException;

    Iterator<Server> serversIterator() throws OSClientException;

    Server getServer(String serverId) throws OSClientException;

    Server getServer(String projectId, String serverId) throws OSClientException;

    void deleteServer(String serverId) throws OSClientException;

    void stopServer(String serverId) throws OSClientException;

    void startServer(String serverId) throws OSClientException;

    void resizeServer(String serverId, String flavor) throws OSClientException;

    void confirmResizeServer(String serverId) throws OSClientException;

    void rebootServer(String serverId, RebootType type) throws OSClientException;

    boolean rebuildServer(String serverId, String openStackImageId) throws OSClientException;

    void suspendServer(String serverId) throws OSClientException;

    void resumeServer(String serverId) throws OSClientException;

    void upsertServerMetadata(String serverId, Map<String, String> metadata) throws OSClientException;

    Flavor getFlavor(String flavorId) throws OSClientException;

    Map<String, String> getFlavorSpec(String flavorId) throws OSClientException;

    List<Pair<Meter, String>> listMeters(String resourceId) throws OSClientException;

    void importKey(String name, String publicKey) throws OSClientException;

    boolean isKeyPairExist(String name) throws OSClientException;

    List<NovaKeyPair> listKeyPairs() throws OSClientException;

    void deleteKeyPair(String name) throws OSClientException;

    List<Network> listNetworks() throws OSClientException;

    List<Network> listNetworks(NetworkApiFilter filter) throws OSClientException;

    List<Network> listNetworksByName(String name) throws OSClientException;

    Network getNetwork(String networkId) throws OSClientException;

    List<NovaSubnet> listSubnets(List<String> subnetIds) throws OSClientException;

    FloatingIp associateFloatingIp(String floatingIpId, String portId) throws OSClientException;

    FloatingIp disassociateFloatingIp(String floatingIpId) throws OSClientException;

    Port createPort(CreatePortRequest request) throws OSClientException;

    List<Port> listPortsByDeviceId(String deviceId) throws OSClientException;

    Port getPort(String portId) throws OSClientException;

    void attachPortToServer(String server, String portId) throws OSClientException;

    void detachPortFromServer(String server, String portId) throws OSClientException;

    void deletePort(String portId) throws OSClientException;

    List<Port> getPorts(String tenantId) throws OSClientException;

    void resetPort(String portId) throws OSClientException;

    void updatePortMacAddress(String portId, String macAddress) throws OSClientException;

    void updatePortSecurityGroups(String portId, List<String> securityGroups) throws OSClientException;

    @SuppressWarnings("unused")
    List<NovaSubnet> listSubnets() throws OSClientException;

    List<SecurityGroup> listSecurityGroups() throws OSClientException;

    FloatingIp allocateFloatingIp(String networkId, String fixedIp) throws OSClientException;

    FloatingIp allocateFloatingIp(String networkId, String portId, String floatingIp) throws OSClientException;

    void releaseFloatingIp(String floatingIpId) throws OSClientException;

    List<FloatingIp> listFloatingIps(String externalId) throws OSClientException;

    FloatingIp getFloatingIp(String floatingIpId) throws OSClientException;

    List<CinderVolume> listVolumes() throws OSClientException;

    List<CinderVolume> listVolumes(String serverId) throws OSClientException;

    Iterator<CinderVolume> volumesIterator() throws OSClientException;

    CinderVolume createVolume(CreateCinderVolumeParameters parameters) throws OSClientException;

    CinderVolume getVolume(String id) throws OSClientException;

    void deleteVolume(String volumeId) throws OSClientException;

    void attachVolume(String serverId, String volumeId) throws OSClientException;

    void attachVolume(String serverId, String volumeId, String device) throws OSClientException;

    void detachVolume(String serverId, String volumeId) throws OSClientException;

    void extendVolume(String volumeId, int sizeGB) throws OSClientException;

    void updateVolumeMetadata(String volumeId, Map<String, String> metadata) throws OSClientException;

    Map<String, String> listServerMetadata(String serverId) throws OSClientException;

    VolumeSnapshot createVolumeSnapshot(String volumeId, Map<String, String> metadata) throws OSClientException;

    VolumeSnapshot getVolumeSnapshot(String snapshotId) throws OSClientException;

    void deleteVolumeSnapshot(String snapshotId) throws OSClientException;

    VncConsole getVncConsole(String serverId) throws OSClientException;

    Image getProjectImage(String tenantId, String imageId) throws OSClientException;

    List<Image> listProjectImages(String tenantId) throws OSClientException;

    List<Image> listPublicImages() throws OSClientException;

    String createImage(String serverId, String imageName) throws OSClientException;

    void deleteImage(String imageId) throws OSClientException;

    List<Hypervisor> listHosts() throws OSClientException;

    List<AvailabilityZone> listAvailabilityZones() throws OSClientException;

    CinderBackendStoragePoolInfo getStoragePoolInfo(String hostName) throws OSClientException;

    List<CinderHost> listCinderHosts() throws OSClientException;

    IOpenStackApi toApi();
}
