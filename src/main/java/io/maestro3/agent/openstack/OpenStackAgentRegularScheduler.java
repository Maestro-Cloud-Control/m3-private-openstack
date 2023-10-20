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

package io.maestro3.agent.openstack;

import io.maestro3.agent.cadf.CadfAuditEventSender;
import io.maestro3.agent.cadf.openstack.CadfUtils;
import io.maestro3.agent.cadf.openstack.OpenStackEventTypeActionMapping;
import io.maestro3.agent.converter.OpenStackServerStateDetector;
import io.maestro3.agent.dao.IInstanceRunRecordDao;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.base.DiskState;
import io.maestro3.agent.model.base.InstanceRunRecord;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.base.TenantState;
import io.maestro3.agent.model.compute.Address;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.lock.VoidOperation;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackNetworkInterfaceInfo;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.compute.bean.SecurityInfo;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.scheduler.AbstractScheduler;
import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.agent.service.VolumeDbService;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfEventType;
import io.maestro3.cadf.model.CadfMeasurement;
import io.maestro3.cadf.model.CadfOutcomes;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.cadf.model.CadfTag;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.v3.model.agent.openstack.SdkOsDiskInfo;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class OpenStackAgentRegularScheduler extends AbstractScheduler {

    private final OpenStackApiProvider apiProvider;
    private final DbServicesProvider dbServicesProvider;
    private final CadfAuditEventSender auditEventSender;
    private final Locker locker;
    private final IInstanceRunRecordDao instanceRunRecordDao;
    private IOpenStackRegionRepository regionService;

    @Value("${flag.enable.instances.schedule.describer}")
    private boolean enableInstancesScheduledDescribers;

    @Autowired
    public OpenStackAgentRegularScheduler(OpenStackApiProvider apiProvider,
                                          IOpenStackRegionRepository regionService,
                                          DbServicesProvider dbServicesProvider,
                                          IInstanceRunRecordDao instanceRunRecordDao,
                                          CadfAuditEventSender auditEventSender,
                                          @Qualifier("instanceLocker") Locker locker) {
        super(PrivateCloudType.OPEN_STACK, true);
        this.apiProvider = apiProvider;
        this.dbServicesProvider = dbServicesProvider;
        this.instanceRunRecordDao = instanceRunRecordDao;
        this.regionService = regionService;
        this.auditEventSender = auditEventSender;
        this.locker = locker;
    }

    @Override
    public String getScheduleTitle() {
        return "OpenStack instance updating";
    }

    @Scheduled(cron = "${cron.update.openstack.instances: 0 0/1 * * * ?}")
    public void executeSchedule() {
        super.executeSchedule();
    }

    public void execute() {
        if (!enableInstancesScheduledDescribers) return;

        start("Execute instances update...");
        Collection<OpenStackRegionConfig> regionConfigs = regionService
            .findAllOSRegionsAvailableForDescribers();

        regionConfigs.forEach(this::updateRegionInstances);
        end("Instances update executed.");
    }

    private void updateRegionInstances(OpenStackRegionConfig region) {
        Collection<OpenStackTenant> tenantConfigs = dbServicesProvider.getTenantDbService()
            .findAllByRegion(region.getId());

        tenantConfigs.stream()
            .filter(tenant -> {
                if (tenant.isSkipHealthCheck() || tenant.getTenantState().equals(TenantState.AVAILABLE)) {
                    return true;
                }
                LOG.debug("Tenant {} skipped because it state is not AVAILABLE", tenant.getTenantAlias());
                return false;
            })
            .forEach(openStackTenantConfig -> updateTenantInRegionInstances(region, openStackTenantConfig));
    }

    private void updateTenantInRegionInstances(OpenStackRegionConfig region, OpenStackTenant tenant) {
        boolean describeAllInstances = tenant.isDescribeAllInstances();
        Collection<OpenStackServerConfig> dbServers = dbServicesProvider.getServerDbService()
            .findAllAvailableTenantServers(region.getId(), tenant.getId());

        List<Server> serverList = getServersFromOpenStack(region, tenant);
        List<CinderVolume> volumesFromOpenStack = getVolumesFromOpenStack(region, tenant);
        synchronizeVolumes(serverList, volumesFromOpenStack, region, tenant);
        Map<String, CinderVolume> volumeMap = volumesFromOpenStack.stream()
            .collect(Collectors.toMap(CinderVolume::getId, Function.identity()));
        Map<String, Server> serverMap = serverList.stream()
            .collect(Collectors.toMap(Server::getId, Function.identity()));

        for (OpenStackServerConfig openStackServerConfig : dbServers) {
            ServerStateEnum previousServerState = openStackServerConfig.getState();
            Server server = serverMap.remove(openStackServerConfig.getNativeId());
            ServerStateEnum currentServerState;

            if (server != null) {
                currentServerState = OpenStackServerStateDetector.toServerState(
                    server.getStatus(), server.getPowerState(), server.getTaskState());
            } else {
                currentServerState = ServerStateEnum.TERMINATED;
            }
            Boolean runSuccess = null;
            if (openStackServerConfig.isOur() && !openStackServerConfig.isInstanceRunSuccess()
                && currentServerState == ServerStateEnum.RUNNING && server != null) {
                runSuccess = true;
                instanceRunRecordDao.save(new InstanceRunRecord(region.getId(), tenant.getId(),
                    server.getImageId(), System.currentTimeMillis() - openStackServerConfig.getStartTime()));
            }
            Boolean finalRunSuccess = runSuccess;
            executeOperation(tenant,
                () -> updateServerDbState(openStackServerConfig, server, currentServerState, finalRunSuccess));
            boolean instanceStateChanged = openStackServerConfig.getState() != currentServerState;
            boolean instanceVolumesChanged = server != null && isVolumeChanged(openStackServerConfig, server);

            if (instanceStateChanged || instanceVolumesChanged) {
                generateAuditEventInternally(region, tenant, openStackServerConfig, currentServerState,
                    previousServerState, server, instanceStateChanged, false, volumeMap);
            }
            if (instanceVolumesChanged) {
                processVolumeAudit(region, tenant, openStackServerConfig, server, volumeMap);
            }

        }
        if (describeAllInstances) {
            processInstancesFromOs(region, tenant, serverMap, volumeMap);
        }
    }

    private void processVolumeAudit(OpenStackRegionConfig region, OpenStackTenant tenant,
                                    OpenStackServerConfig openStackServerConfig, Server server,
                                    Map<String, CinderVolume> volumeMap) {
        Set<String> dbVolumes = openStackServerConfig.getAttachedVolumes();
        Set<String> nativeVolumes = server.getVolumes().stream()
            .map(CinderVolume::getId)
            .collect(Collectors.toSet());
        Set<String> detachedVolumes = dbVolumes.stream()
            .filter(id -> !nativeVolumes.contains(id))
            .collect(Collectors.toSet());
        Set<String> attachedVolumes = nativeVolumes.stream()
            .filter(id -> !dbVolumes.contains(id))
            .collect(Collectors.toSet());
        for (String detachedVolume : detachedVolumes) {
            generateVolumeEvent(region, tenant, null, openStackServerConfig, volumeMap.get(detachedVolume), CadfActions.detach(), DiskState.AVAILABLE);
        }
        for (String attachedVolume : attachedVolumes) {
            generateVolumeEvent(region, tenant, null, openStackServerConfig, volumeMap.get(attachedVolume), CadfActions.attach(), DiskState.IN_USE);
        }
    }

    private void synchronizeVolumes(List<Server> serverList, List<CinderVolume> volumesFromCP,
                                    OpenStackRegionConfig region, OpenStackTenant tenant) {
        Map<String, CinderVolume> volumeIdsFromCloudProvider = volumesFromCP.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(CinderVolume::getId, Function.identity()));

        VolumeDbService volumeDbService = dbServicesProvider.getVolumeDbService();
        List<CinderVolume> volumesFromDataBase = volumeDbService.findByTenantAndRegion(tenant.getTenantAlias(), region.getRegionAlias());
        Map<String, CinderVolume> volumesIdsFromDataBase = volumesFromDataBase.stream()
            .collect(Collectors.toMap(CinderVolume::getId, Function.identity()));
        List<CinderVolume> volumesToDelete = volumesFromDataBase.stream()
            .filter(v -> !volumeIdsFromCloudProvider.containsKey(v.getId()))
            .collect(Collectors.toList());

        Map<String, String> hostsByAttachedVolumeId = new HashMap<>();
        for (Server server : serverList) {
            List<CinderVolume> volumes = server.getVolumes();
            for (CinderVolume volume : volumes) {
                hostsByAttachedVolumeId.put(volume.getId(), server.getId());
            }
        }
        volumesFromCP.forEach(v -> {
            v.setHost(hostsByAttachedVolumeId.get(v.getId()));
            v.setRegion(region.getRegionAlias());
            v.setTenant(tenant.getTenantAlias());
        });
        List<CinderVolume> newVolumes = volumesFromCP.stream()
            .filter(v -> !volumesIdsFromDataBase.containsKey(v.getId()) && v.getHost() == null)
            .collect(Collectors.toList());
        volumeDbService.updateVolumes(volumesToDelete, volumesFromCP);
        for (CinderVolume newVolume : newVolumes) {
            generateVolumeEvent(region, tenant, null, null, newVolume, CadfActions.create(), DiskState.AVAILABLE);
        }
        for (CinderVolume deletedVolume : volumesToDelete) {
            generateVolumeEvent(region, tenant, deletedVolume, null, null, CadfActions.delete(), DiskState.AVAILABLE);
        }
    }

    private void processInstancesFromOs(OpenStackRegionConfig region, OpenStackTenant tenant,
                                        Map<String, Server> serverMap, Map<String, CinderVolume> volumeMap) {
        for (Server server : serverMap.values()) {
            ServerStateEnum serverState = OpenStackServerStateDetector.toServerState(
                server.getStatus(), server.getPowerState(), server.getTaskState());
            if (serverState == ServerStateEnum.CREATING ||
                serverState == ServerStateEnum.STARTING) {
                continue;
            }
            OpenStackServerConfig serverConfig = new OpenStackServerConfig();
            serverConfig.setNameAlias(server.getId());
            serverConfig.setNativeId(server.getId());
            serverConfig.setMetadata(server.getMetadata());
            serverConfig.setNativeName(server.getName());
            serverConfig.setFlavorId(server.getFlavorId());
            serverConfig.setImageId(server.getImageId());
            serverConfig.setKeyName(server.getKeyName());
            serverConfig.setTenantId(tenant.getId());
            serverConfig.setRegionId(tenant.getRegionId());
            serverConfig.setState(serverState);
            serverConfig.setSecurityGroups(
                server.getSecurityGroups().stream()
                    .map(SecurityInfo::getName)
                    .collect(Collectors.toList()));
            serverConfig.setStartTime(server.getCreated().getTime());
            serverConfig.setOur(false);
            Set<String> volumes = server.getVolumes().stream()
                .map(CinderVolume::getId)
                .collect(Collectors.toSet());
            serverConfig.setAttachedVolumes(volumes);
            serverConfig.setTags(Collections.emptyList());
            executeOperation(tenant,
                () -> dbServicesProvider.getServerDbService().saveServerConfig(serverConfig));
            generateAuditEventInternally(region, tenant, serverConfig, serverState, null, server,
                false, true, volumeMap);
        }
    }

    private boolean isVolumeChanged(OpenStackServerConfig openStackServerConfig, Server server) {
        Set<String> dbVolumes = openStackServerConfig.getAttachedVolumes();
        Set<String> nativeVolumes = server.getVolumes().stream()
            .map(CinderVolume::getId)
            .collect(Collectors.toSet());
        return dbVolumes.size() != nativeVolumes.size() ||
            !(dbVolumes.containsAll(nativeVolumes) && nativeVolumes.containsAll(dbVolumes));
    }

    private List<Server> getServersFromOpenStack(OpenStackRegionConfig region, OpenStackTenant tenant) {
        try {
            return apiProvider.openStack(tenant, region).compute().servers().list();
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            // skip further exception throwing
            return Collections.emptyList();
        }
    }

    private List<CinderVolume> getVolumesFromOpenStack(OpenStackRegionConfig region, OpenStackTenant tenant) {
        try {
            return apiProvider.openStack(tenant, region).blockStorage().volumes().list(tenant.getNativeId());
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            // skip further exception throwing
            return Collections.emptyList();
        }
    }

    private void generateAuditEventInternally(OpenStackRegionConfig region,
                                              OpenStackTenant tenant,
                                              OpenStackServerConfig openStackServerConfig,
                                              ServerStateEnum currentServerState,
                                              ServerStateEnum previousServerState,
                                              Server server,
                                              boolean instanceStateChanged,
                                              boolean newInstance,
                                              Map<String, CinderVolume> volumeMap) {
        ICadfAction action = extractCadfAction(currentServerState, previousServerState, instanceStateChanged, newInstance);
        if (Objects.isNull(action)) {
            return;
        }

        Date createdDate = server == null ? DateTime.now().withZone(DateTimeZone.UTC).toDate() : server.getCreated();
        Date updatedDate = DateTime.now().withZone(DateTimeZone.UTC).toDate();

        String actionId = new ObjectId().toHexString();
        CadfResource target = CadfUtils.createTarget(openStackServerConfig.isOur()
            ? openStackServerConfig.getNativeName()
            : openStackServerConfig.getNativeId());

        String flavorId = openStackServerConfig.getFlavorId();
        OpenStackFlavorConfig shapeConfig = null;
        for (OpenStackFlavorConfig allowedShape : region.getAllowedShapes()) {
            if (allowedShape.getNativeId().equals(flavorId)) {
                shapeConfig = allowedShape;
                break;
            }
        }

        List<SdkOsDiskInfo> attachedDisks;
        if (server != null) {
            attachedDisks = server.getVolumes().stream()
                .map(vol -> volumeMap.get(vol.getId()))
                .map(vol -> new SdkOsDiskInfo(vol.getId(), vol.getSize(), vol.getVolumeType(), vol.isBootable()))
                .collect(Collectors.toList());
        } else {
            attachedDisks = new ArrayList<>();
        }

        String imageId = openStackServerConfig.getImageId();
        OpenStackMachineImage image = dbServicesProvider.getMachineImageDbService().findByNativeId(imageId);

        List<CadfMeasurement> measurements = CadfUtils.resolveInstanceMeasurements(image, shapeConfig);
        List<CadfAttachment> attachments = CadfUtils.resolveInstanceAttachments(action, attachedDisks,
            openStackServerConfig, tenant, region, shapeConfig, image, updatedDate, createdDate, currentServerState);
        List<CadfTag> tags = CadfUtils.prepareCadfTags(openStackServerConfig);

        CadfAuditEvent auditEvent = generateCadfAuditEvent(
            action, updatedDate, actionId, target, measurements, attachments, tags);

        auditEventSender.sendCadfAuditEvent(auditEvent, Arrays.asList(AuditEventGroupType.BILLING_AUDIT, AuditEventGroupType.INSTANCE_DATA, AuditEventGroupType.NATIVE_AUDIT));
    }

    private ICadfAction extractCadfAction(ServerStateEnum currentServerState, ServerStateEnum previousServerState, boolean instanceStateChanged, boolean newInstance) {
        if (newInstance) {
            return CadfActions.discover();
        } else {
            if (instanceStateChanged) {
                if (currentServerState == ServerStateEnum.ERROR ||
                    currentServerState == ServerStateEnum.TERMINATED) {
                    return CadfActions.delete();
                } else {
                    return OpenStackEventTypeActionMapping.getActionByPreviousState(previousServerState);
                }
            } else {
                return CadfActions.configure();
            }
        }
    }

    private void executeOperation(OpenStackTenant tenant, VoidOperation<M3PrivateAgentException> operation) {
        try {
            locker.executeOperation(tenant.getId(), operation);
        } catch (M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void updateServerDbState(OpenStackServerConfig openStackServerConfig, Server server, ServerStateEnum state, Boolean runSuccess) {
        if (server != null) {
            String privateIpAddress = null;
            List<Address> addressList = server.getAddresses().getPrivateAddresses();
            if (CollectionUtils.isNotEmpty(addressList)) {
                privateIpAddress = addressList.get(0).getIp();
                setPrivateIp(openStackServerConfig, privateIpAddress);
            }
            Set<String> volumes = server.getVolumes().stream()
                .map(CinderVolume::getId)
                .collect(Collectors.toSet());
            dbServicesProvider.getServerDbService().updateServerConfig(
                openStackServerConfig.getId(),
                state,
                privateIpAddress,
                volumes,
                runSuccess);
        } else {
            dbServicesProvider.getServerDbService().updateServerConfig(
                openStackServerConfig.getId(),
                state,
                null,
                runSuccess);
        }
    }

    private void setPrivateIp(OpenStackServerConfig openStackServerConfig, String privateIp) {
        OpenStackNetworkInterfaceInfo networkInterfaceInfo = openStackServerConfig.getNetworkInterfaceInfo();
        if (networkInterfaceInfo == null) {
            networkInterfaceInfo = new OpenStackNetworkInterfaceInfo();
            openStackServerConfig.setNetworkInterfaceInfo(networkInterfaceInfo);
        }
        networkInterfaceInfo.setPrivateIP(privateIp);
    }

    private CadfAuditEvent generateCadfAuditEvent(ICadfAction action,
                                                  Date date,
                                                  String actionId,
                                                  CadfResource target,
                                                  List<CadfMeasurement> measurements,
                                                  List<CadfAttachment> attachments,
                                                  List<CadfTag> tags) {
        return CadfAuditEvent.builder()
            .withId(CadfUtils.ID_NAMESPACE + actionId)
            .withAction(action)
            .withEventTime(DateUtils.formatDate(date, DateUtils.CADF_FORMAT_TIME))
            .withEventType(CadfEventType.ACTIVITY)
            .withInitiator(CadfUtils.PRIVATE_AGENT)
            .withObserver(CadfUtils.SYSTEM)
            .withTarget(target)
            .withOutcome(CadfOutcomes.success()) // all billing events are of this type, need to add logic to convert other events
            .withMeasurements(measurements)
            .withAttachments(attachments)
            .withTags(tags)
            .build();
    }

    private CadfAuditEvent generateVolumeEvent(OpenStackRegionConfig region, OpenStackTenant tenant, CinderVolume registeredDisk,
                                               OpenStackServerConfig server, CinderVolume diskFromProvider, ICadfAction action, DiskState state) {

        CinderVolume disk = diskFromProvider != null ? diskFromProvider : registeredDisk;
        String maestroVolId = null;
        if (disk.getMetadata() != null) {
            maestroVolId = disk.getMetadata().get("maestroId");
        }
        List<CadfAttachment> attachments = CadfUtils.resolveVolumeAttachments(tenant, region, server, disk, maestroVolId, state);
        List<CadfMeasurement> measurements = CadfUtils.resolveVolumeMeasurements(disk.getSize());

        Date date = DateTime.now().withZone(DateTimeZone.UTC).toDate();

        CadfResource target = CadfUtils.createVolumeTarget(diskFromProvider != null ? diskFromProvider.getId() : registeredDisk.getId());
        String actionId = new ObjectId().toHexString();

        CadfAuditEvent cadfAuditEvent = generateCadfAuditEvent(action, date, actionId, target, measurements, attachments, new ArrayList<>());
        if (cadfAuditEvent != null) {
            auditEventSender.sendCadfAuditEvent(cadfAuditEvent, Collections.singletonList(AuditEventGroupType.VOLUME_DATA));
        }
        return cadfAuditEvent;
    }
}
