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

import io.maestro3.agent.model.common.KeyPair;
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
import io.maestro3.agent.openstack.api.networking.request.CreateFloatingIp;
import io.maestro3.agent.openstack.api.storage.bean.CinderBackendStoragePoolInfo;
import io.maestro3.agent.openstack.api.storage.bean.CinderHost;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolumeAttachment;
import io.maestro3.agent.openstack.api.storage.bean.CreateCinderVolumeParameters;
import io.maestro3.agent.openstack.api.storage.bean.VolumeSnapshot;
import io.maestro3.agent.openstack.api.storage.request.CinderServiceType;
import io.maestro3.agent.openstack.api.storage.request.ListHostsRequest;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.filter.impl.NetworkApiFilter;
import io.maestro3.agent.openstack.filter.impl.SubnetApiFilter;
import io.maestro3.agent.service.IOpenStackClient;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class OpenStackClient implements IOpenStackClient {

    private static final int DEFAULT_LIMIT = 10;

    private IOpenStackApi openStackApi;

    /**
     * Wraps Open Stack API.
     *
     * @param api Open Stack API
     * @return new Open Stack Client
     */
    public static OpenStackClient wrap(IOpenStackApi api) {
        OpenStackClient wrapper = new OpenStackClient();
        wrapper.openStackApi = api;
        return wrapper;
    }

    @Override
    public Server bootServer(ServerBootInfo bootInfo) throws OSClientException {
        return openStackApi.compute().servers().boot(bootInfo);
    }

    @Override
    public List<Server> getServerList() throws OSClientException {
        return openStackApi.compute().servers().list();
    }

    @Override
    public Iterator<Server> serversIterator() throws OSClientException {
        // lists servers chunk by chunk
        return openStackApi.compute().servers().listLimited(DEFAULT_LIMIT);
    }

    @Override
    public Server getServer(String serverId) throws OSClientException {
        return openStackApi.compute().servers().get(serverId);
    }

    @Override
    public Server getServer(String projectId, String serverId) throws OSClientException {
        Assert.hasText(projectId, "projectId cannot be null or empty.");
        Assert.hasText(serverId, "serverId cannot be null or empty.");

        // OpenStack API does not allow to filter instances by tenant_id, when specifying server_id
        // So this is a little trick to do so
        Server server = openStackApi.compute().servers().get(serverId);
        if (server == null) {
            return null;
        }
        if (projectId.equals(server.getTenantId())) {
            return server;
        }
        return null;
    }

    @Override
    public void deleteServer(String serverId) throws OSClientException {
        openStackApi.compute().servers().delete(serverId);
    }

    @Override
    public void stopServer(String serverId) throws OSClientException {
        openStackApi.compute().servers().stop(serverId);
    }

    @Override
    public void startServer(String serverId) throws OSClientException {
        openStackApi.compute().servers().start(serverId);
    }

    @Override
    public void resizeServer(String serverId, String flavor) throws OSClientException {
        openStackApi.compute().servers().resize(serverId, flavor);
    }

    @Override
    public void confirmResizeServer(String serverId) throws OSClientException {
        openStackApi.compute().servers().confirmResize(serverId);
    }

    @Override
    public void rebootServer(String serverId, RebootType type) throws OSClientException {
        openStackApi.compute().servers().reboot(serverId, type);
    }

    @Override
    public boolean rebuildServer(String serverId, String openStackImageId) throws OSClientException {
        return openStackApi.compute().servers().rebuild(serverId, openStackImageId);
    }

    @Override
    public void suspendServer(String serverId) throws OSClientException {
        openStackApi.compute().servers().suspend(serverId);
    }

    @Override
    public void resumeServer(String serverId) throws OSClientException {
        openStackApi.compute().servers().resume(serverId);
    }

    @Override
    public void upsertServerMetadata(String serverId, Map<String, String> metadata) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");

        openStackApi.compute().metadata().upsert(serverId, metadata);
    }

    @Override
    public Flavor getFlavor(String flavorId) throws OSClientException {
        return openStackApi.compute().flavors().get(flavorId);
    }

    @Override
    public Map<String, String> getFlavorSpec(String flavorId) throws OSClientException {
        return openStackApi.compute().flavors().listExtraSpecs(flavorId);
    }

    @Override
    public List<Pair<Meter, String>> listMeters(String resourceId) throws OSClientException {
        return openStackApi.telemetry().listMeters(resourceId);
    }

    @Override
    public void importKey(String name, String publicKey) throws OSClientException {
        openStackApi.compute().keyPairs().importKeyPair(name, publicKey);
    }

    @Override
    public boolean isKeyPairExist(String name) throws OSClientException {
        Assert.hasText(name, "key name can not be empty or null");

        KeyPair keyPair = openStackApi.compute().keyPairs().inspect(name);
        return keyPair != null;
    }

    @Override
    public List<NovaKeyPair> listKeyPairs() throws OSClientException {
        return openStackApi.compute().keyPairs().list();
    }

    @Override
    public void deleteKeyPair(String name) throws OSClientException {
        openStackApi.compute().keyPairs().deleteKeyPair(name);
    }

    @Override
    public List<Network> listNetworks() throws OSClientException {
        return openStackApi.networking().networks().list();
    }

    @Override
    public List<Network> listNetworks(NetworkApiFilter filter) throws OSClientException {
        return openStackApi.networking().networks().list(filter);
    }

    @Override
    public List<Network> listNetworksByName(String name) throws OSClientException {
        return openStackApi.networking().networks().listByName(name);
    }

    @Override
    public Network getNetwork(String networkId) throws OSClientException {
        return openStackApi.networking().networks().get(networkId);
    }

    @Override
    public List<NovaSubnet> listSubnets(List<String> subnetIds) throws OSClientException {
        SubnetApiFilter filter = new SubnetApiFilter().withIds(subnetIds);
        return openStackApi.networking().subnets().list(filter);
    }

    @Override
    public List<NovaSubnet> listSubnets() throws OSClientException {
        return listSubnets(Collections.<String>emptyList());
    }

    @Override
    public List<SecurityGroup> listSecurityGroups() throws OSClientException {
        return openStackApi.networking().securityGroups().list();
    }

    @Override
    public FloatingIp allocateFloatingIp(String networkId, String fixedIp) throws OSClientException {
        CreateFloatingIp createFloatingIp = CreateFloatingIp.builder().withAddress(fixedIp).inNetwork(networkId).get();
        return openStackApi.networking().floatingIps().create(createFloatingIp);
    }

    @Override
    public FloatingIp allocateFloatingIp(String networkId, String portId, String fixedIp) throws OSClientException {
        CreateFloatingIp createFloatingIp = CreateFloatingIp.builder()
            .withAddress(fixedIp)
            .inNetwork(networkId)
            .portId(portId)
            .get();

        return openStackApi.networking().floatingIps().create(createFloatingIp);
    }

    @Override
    public void releaseFloatingIp(String floatingIpId) throws OSClientException {
        openStackApi.networking().floatingIps().delete(floatingIpId);
    }

    @Override
    public List<FloatingIp> listFloatingIps(String tenantId) throws OSClientException {
        return openStackApi.networking().floatingIps().listByTenantId(tenantId);
    }

    @Override
    public FloatingIp getFloatingIp(String floatingIpId) throws OSClientException {
        Assert.hasText(floatingIpId, "floatingIpId cannot be null or empty.");
        return openStackApi.networking().floatingIps().detail(floatingIpId);
    }

    @Override
    public FloatingIp associateFloatingIp(String floatingIpId, String portId) throws OSClientException {
        return openStackApi.networking().floatingIps().associate(floatingIpId, portId);
    }

    @Override
    public FloatingIp disassociateFloatingIp(String floatingIpId) throws OSClientException {
        return openStackApi.networking().floatingIps().disassociate(floatingIpId);
    }

    @Override
    public Port createPort(CreatePortRequest request) throws OSClientException {
        Assert.notNull(request, "request cannot be null or empty.");

        return openStackApi.networking().ports().create(request);
    }

    @Override
    public List<Port> listPortsByDeviceId(String deviceId) throws OSClientException {
        return openStackApi.networking().ports().listByDeviceId(deviceId);
    }

    @Override
    public Port getPort(String portId) throws OSClientException {
        Assert.hasText(portId, "portId cannot be null or empty.");

        return openStackApi.networking().ports().get(portId);
    }

    @Override
    public void attachPortToServer(String serverId, String portId) throws OSClientException {
        Assert.hasText(portId, "portId cannot be null or empty.");
        Assert.hasText(serverId, "serverId cannot be null or empty.");

        openStackApi.compute().portInterfaces().attach(serverId, portId);
    }

    @Override
    public void detachPortFromServer(String serverId, String portId) throws OSClientException {
        Assert.hasText(portId, "portId cannot be null or empty.");
        Assert.hasText(serverId, "serverId cannot be null or empty.");

        openStackApi.compute().portInterfaces().detach(serverId, portId);
    }

    @Override
    public void deletePort(String portId) throws OSClientException {
        Assert.hasText(portId, "portId cannot be null or empty.");

        openStackApi.networking().ports().delete(portId);
    }

    @Override
    public List<Port> getPorts(String tenantId) throws OSClientException {
        Assert.hasText(tenantId, "tenantId cannot be null or empty.");

        return openStackApi.networking().ports().listByTenantId(tenantId);
    }

    @Override
    public void resetPort(String portId) throws OSClientException {
        Assert.hasText(portId, "portId cannot be null or empty.");

        openStackApi.networking().ports().clearDns(portId);
    }

    @Override
    public void updatePortMacAddress(String portId, String macAddress) throws OSClientException {
        Assert.hasText(portId, "portId cannot be null or empty.");
        Assert.hasText(macAddress, "macAddress cannot be null or empty.");

        openStackApi.networking().ports().updateMacAddress(portId, macAddress);
    }

    @Override
    public void updatePortSecurityGroups(String portId, List<String> securityGroups) throws OSClientException {
        Assert.hasText(portId, "portId cannot be null or empty.");
        Assert.notEmpty(securityGroups, "macAddress cannot be null or empty.");

        openStackApi.networking().ports().updateSecurityGroups(portId, securityGroups);
    }

    @Override
    public List<CinderVolume> listVolumes() throws OSClientException {
        return openStackApi.blockStorage().volumes().list();
    }

    @Override
    public List<CinderVolume> listVolumes(String serverId) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");

        List<CinderVolume> volumes = openStackApi.blockStorage().volumes().list();
        if (CollectionUtils.isEmpty(volumes)) {
            return volumes;
        }
        List<CinderVolume> instanceVolumes = new ArrayList<>();
        for (CinderVolume volume : volumes) {
            List<CinderVolumeAttachment> attachments = volume.getAttachments();
            for (CinderVolumeAttachment attachment : attachments) {
                if (serverId.equals(attachment.getServerId())) {
                    instanceVolumes.add(volume);
                    break;
                }
            }
        }
        return instanceVolumes;
    }

    @Override
    public Iterator<CinderVolume> volumesIterator() throws OSClientException {
        return openStackApi.blockStorage().volumes().listLimited(DEFAULT_LIMIT);
    }

    @Override
    public CinderVolume createVolume(CreateCinderVolumeParameters parameters) throws OSClientException {
        Assert.notNull(parameters, "parameters cannot be null.");

        return openStackApi.blockStorage().volumes().create(parameters);
    }

    @Override
    public CinderVolume getVolume(String id) throws OSClientException {
        Assert.hasText(id, "id cannot be null or empty.");

        return openStackApi.blockStorage().volumes().inspect(id);
    }

    @Override
    public void deleteVolume(String volumeId) throws OSClientException {
        Assert.hasText("volumeId cannot be null or empty.");

        openStackApi.blockStorage().volumes().delete(volumeId);
    }

    @Override
    public void attachVolume(String serverId, String volumeId) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        Assert.hasText("volumeId cannot be null or empty.");

        openStackApi.compute().servers().attachVolume(serverId, volumeId);
    }

    @Override
    public void attachVolume(String serverId, String volumeId, String device) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        Assert.hasText("volumeId cannot be null or empty.");

        openStackApi.compute().servers().attachVolume(serverId, volumeId, device);
    }

    @Override
    public void detachVolume(String serverId, String volumeId) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        Assert.hasText(volumeId, "volumeId cannot be null or empty.");

        openStackApi.compute().servers().detachVolume(serverId, volumeId);
    }

    @Override
    public void extendVolume(String volumeId, int sizeGB) throws OSClientException {
        Assert.hasText(volumeId, "volumeId cannot be null or empty.");

        openStackApi.blockStorage().volumes().extendVolume(volumeId, sizeGB);
    }

    @Override
    public void updateVolumeMetadata(String volumeId, Map<String, String> metadata) throws OSClientException {
        Assert.hasText(volumeId, "volumeId cannot be null or empty.");

        openStackApi.blockStorage().volumes().updateMetadata(volumeId, metadata);
    }

    @Override
    public Map<String, String> listServerMetadata(String serverId) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");

        return openStackApi.compute().metadata().list(serverId);
    }

    @Override
    public VolumeSnapshot createVolumeSnapshot(String volumeId, Map<String, String> metadata) throws OSClientException {
        Assert.hasText(volumeId, "volumeId cannot be null or empty.");

        return openStackApi.blockStorage().snapshots().create(volumeId, metadata);
    }

    @Override
    public VolumeSnapshot getVolumeSnapshot(String snapshotId) throws OSClientException {
        Assert.hasText(snapshotId, "snapshotId cannot be null or empty.");

        return openStackApi.blockStorage().snapshots().details(snapshotId);
    }

    @Override
    public void deleteVolumeSnapshot(String snapshotId) throws OSClientException {
        Assert.hasText(snapshotId, "snapshotId cannot be null or empty.");

        openStackApi.blockStorage().snapshots().delete(snapshotId);
    }

    @Override
    public VncConsole getVncConsole(String serverId) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");

        return openStackApi.compute().servers().getVncConsole(serverId);
    }

    @Override
    public Image getProjectImage(String tenantId, String imageId) throws OSClientException {
        Assert.hasText(tenantId, "tenantId cannot be null or empty.");
        Assert.hasText(imageId, "imageId cannot be null or empty.");

        return openStackApi.images().image().getProject(tenantId, imageId);
    }

    @Override
    public List<Image> listProjectImages(String tenantId) throws OSClientException {
        Assert.hasText(tenantId, "tenantId cannot be null or empty.");

        return openStackApi.images().image().listProject(tenantId);
    }

    @Override
    public List<Image> listPublicImages() throws OSClientException {
        return openStackApi.images().image().listPublic();
    }

    @Override
    public String createImage(String serverId, String imageName) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        Assert.hasText(imageName, "imageName cannot be null or empty.");

        return openStackApi.compute().images().create(serverId, imageName);
    }

    @Override
    public void deleteImage(String imageId) throws OSClientException {
        Assert.hasText(imageId, "imageId cannot be null or empty.");

        openStackApi.images().image().delete(imageId);
    }

    @Override
    public List<Hypervisor> listHosts() throws OSClientException {
        return openStackApi.compute().hypervisors().list();
    }

    @Override
    public List<AvailabilityZone> listAvailabilityZones() throws OSClientException {
        return openStackApi.compute().availabilityZones().list();
    }

    @Override
    public CinderBackendStoragePoolInfo getStoragePoolInfo(String hostName) throws OSClientException {
        Assert.hasText(hostName, "hostName cannot be null or empty.");

        List<CinderBackendStoragePoolInfo> storagePoolInfoList = openStackApi.blockStorage().schedulerStats().listBackendStoragePools();
        if (CollectionUtils.isEmpty(storagePoolInfoList)) {
            return null;
        }
        for (CinderBackendStoragePoolInfo storagePoolInfo : storagePoolInfoList) {
            if (extractHostName(hostName).equalsIgnoreCase(extractHostName(storagePoolInfo.getHostName()))) {
                return storagePoolInfo;
            }
        }
        return null;
    }

    private String extractHostName(String hostName) {
        if (StringUtils.isBlank(hostName)) {
            return hostName;
        }
        if (hostName.contains("@")) {
            return hostName.split("@")[0];
        }
        return hostName;
    }

    @Override
    public List<CinderHost> listCinderHosts() throws OSClientException {
        return openStackApi.blockStorage().hosts().list(new ListHostsRequest(Collections.singletonList(CinderServiceType.CINDER_VOLUME)));
    }

    @Override
    public IOpenStackApi toApi() {
        return openStackApi;
    }
}
