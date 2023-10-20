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

import io.maestro3.agent.cadf.ICadfAuditEventSender;
import io.maestro3.agent.cadf.openstack.CadfUtils;
import io.maestro3.agent.cadf.openstack.OpenStackEventTypeActionMapping;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.general.MachineImage;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.OpenStackAgentRegularScheduler;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.AbstractInstanceChecker;
import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfEventType;
import io.maestro3.cadf.model.CadfMeasurement;
import io.maestro3.cadf.model.CadfOutcomes;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.v3.model.agent.openstack.SdkOsDiskInfo;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import io.maestro3.sdk.v3.model.instance.SdkInstance;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class OpenStackInstanceChecker extends AbstractInstanceChecker<OpenStackTenant, OpenStackRegionConfig, OpenStackServerConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(OpenStackAgentRegularScheduler.class);

    private final DbServicesProvider dbServicesProvider;
    private final OpenStackApiProvider apiProvider;

    @Autowired
    public OpenStackInstanceChecker(DbServicesProvider dbServicesProvider, ICadfAuditEventSender eventSender,
                                    OpenStackApiProvider apiProvider) {
        super(PrivateCloudType.OPEN_STACK, eventSender);
        this.apiProvider = apiProvider;
        this.dbServicesProvider = dbServicesProvider;
    }

    @Override
    protected List<String> getUniqueIdKeys(OpenStackServerConfig openStackServerConfig) {
        return Arrays.asList(openStackServerConfig.getNativeId(), openStackServerConfig.getNativeName());
    }

    @Override
    protected void processNewInstances(OpenStackTenant tenant, OpenStackRegionConfig region, List<Pair<OpenStackServerConfig, SdkInstance>> newInstances) {
        Map<String, OpenStackMachineImage> images = dbServicesProvider.getMachineImageDbService().findByRegionIdAndTenantId(region.getId(), tenant.getId()).stream()
            .collect(Collectors.toMap(MachineImage::getId, Function.identity()));
        List<CinderVolume> volumesFromOpenStack = getVolumesFromOpenStack(region, tenant);
        Map<String, CinderVolume> volumeMap = volumesFromOpenStack.stream()
            .collect(Collectors.toMap(CinderVolume::getId, Function.identity()));
        for (Pair<OpenStackServerConfig, SdkInstance> newInstance : newInstances) {
            OpenStackServerConfig vm = newInstance.getKey();
            generateAuditEventInternally(region, tenant, vm, new Date(), volumeMap, images, CadfActions.discover());
        }
    }

    @Override
    protected void processTerminatedInstances(OpenStackTenant tenant, OpenStackRegionConfig region, List<Pair<OpenStackServerConfig, SdkInstance>> newInstances) {
        for (Pair<OpenStackServerConfig, SdkInstance> terminatedInstance : newInstances) {
            SdkInstance sdkInstance = terminatedInstance.getRight();
            OpenStackServerConfig terminatedVm = new OpenStackServerConfig();
            terminatedVm.setNativeName(sdkInstance.getInstanceName());
            terminatedVm.setState(ServerStateEnum.TERMINATED);
            terminatedVm.setNativeId(sdkInstance.getInstanceId());
            terminatedVm.setRegionId(region.getId());
            terminatedVm.setTenantId(tenant.getId());
            terminatedVm.setNameAlias(sdkInstance.getInstanceId());
            generateAuditEventInternally(region, tenant, terminatedVm,
                sdkInstance.getCreationDateTimestamp() != 0 ? new Date(sdkInstance.getCreationDateTimestamp()) : new Date(),
                Collections.emptyMap(), Collections.emptyMap(), CadfActions.discover());
        }
    }

    @Override
    protected void processStateChangedInstances(OpenStackTenant tenant, OpenStackRegionConfig region, List<Pair<OpenStackServerConfig, SdkInstance>> newInstances) {
        Map<String, OpenStackMachineImage> images = dbServicesProvider.getMachineImageDbService().findByRegionIdAndTenantId(region.getId(), tenant.getId()).stream()
            .collect(Collectors.toMap(MachineImage::getId, Function.identity()));
        List<CinderVolume> volumesFromOpenStack = getVolumesFromOpenStack(region, tenant);
        Map<String, CinderVolume> volumeMap = volumesFromOpenStack.stream()
            .collect(Collectors.toMap(CinderVolume::getId, Function.identity()));
        for (Pair<OpenStackServerConfig, SdkInstance> changedInstance : newInstances) {
            SdkInstance prevStateSdkInstance = changedInstance.getRight();
            OpenStackServerConfig actualStateVm = changedInstance.getLeft();
            if (actualStateVm.getState().getSdkState().isTransitive()) {
                continue;
            }
            OpenStackServerConfig prevStateVm = new OpenStackServerConfig();
            prevStateVm.setNativeName(prevStateSdkInstance.getInstanceName());
            prevStateVm.setState(ServerStateEnum.forSdkServerState(prevStateSdkInstance.getState()));
            prevStateVm.setNativeId(prevStateSdkInstance.getInstanceId());
            prevStateVm.setRegionId(region.getId());
            prevStateVm.setTenantId(tenant.getId());
            prevStateVm.setNameAlias(actualStateVm.getNameAlias());
            ICadfAction action = resolveAuditAction(actualStateVm.getState().getSdkState());
            if (action == null) {
                action = getPossibleActionFromState(actualStateVm.getState().getSdkState());
                if (action == null) {
                    continue;
                }
            }
            generateAuditEventInternally(region, tenant, actualStateVm,
                prevStateSdkInstance.getCreationDateTimestamp() != 0 ? new Date(prevStateSdkInstance.getCreationDateTimestamp()) : new Date(),
                volumeMap, images, action);
        }
    }


    @Override
    protected void processConfigChangedInstances(OpenStackTenant tenant, OpenStackRegionConfig region, List<Pair<OpenStackServerConfig, SdkInstance>> newInstances) {
    }

    @Override
    protected boolean isVmStatusChanged(SdkInstance sdkInstance, OpenStackServerConfig actualVm) {
        return actualVm.getState().getSdkState() != sdkInstance.getState();
    }

    @Override
    protected Map<String, OpenStackServerConfig> getVmsFromDb(OpenStackTenant tenant, OpenStackRegionConfig region) {
        Map<String, OpenStackServerConfig> result = new HashMap<>();
        dbServicesProvider.getServerDbService().findAllAvailableTenantServers(region.getId(), tenant.getId())
            .forEach(server -> {
                result.put(server.getNativeName(), server);
                result.put(server.getNativeId(), server);
            });
        return result;
    }

    private ICadfAction resolveAuditAction(SdkInstanceState actualState) {
        return OpenStackEventTypeActionMapping.getActionByState(actualState);
    }

    private List<CinderVolume> getVolumesFromOpenStack(OpenStackRegionConfig region, OpenStackTenant tenant) {
        try {
            return apiProvider.openStack(tenant, region).blockStorage().volumes().list(tenant.getNativeId());
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void generateAuditEventInternally(OpenStackRegionConfig region,
                                              OpenStackTenant tenant,
                                              OpenStackServerConfig openStackServerConfig,
                                              Date createdDate,
                                              Map<String, CinderVolume> volumeMap,
                                              Map<String, OpenStackMachineImage> images,
                                              ICadfAction action) {

        ServerStateEnum currentServerState = openStackServerConfig.getState();
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
        if (CollectionUtils.isNotEmpty(openStackServerConfig.getAttachedVolumes())) {
            attachedDisks = openStackServerConfig.getAttachedVolumes().stream()
                .map(volumeMap::get)
                .filter(Objects::nonNull)
                .map(vol -> new SdkOsDiskInfo(vol.getId(), vol.getSize(), vol.getVolumeType(), vol.isBootable()))
                .collect(Collectors.toList());
        } else {
            attachedDisks = new ArrayList<>();
        }

        String imageId = openStackServerConfig.getImageId();
        OpenStackMachineImage image = images.get(imageId);

        List<CadfMeasurement> measurements = CadfUtils.resolveInstanceMeasurements(image, shapeConfig);
        List<CadfAttachment> attachments = CadfUtils.resolveInstanceAttachments(action, attachedDisks,
            openStackServerConfig, tenant, region, shapeConfig, image, updatedDate, createdDate, currentServerState);
        CadfAuditEvent auditEvent = generateCadfAuditEvent(
            action, updatedDate, actionId, target, measurements, attachments);

        auditEventSender.sendCadfAuditEvent(auditEvent, Arrays.asList(AuditEventGroupType.BILLING_AUDIT, AuditEventGroupType.INSTANCE_DATA, AuditEventGroupType.NATIVE_AUDIT));
    }

    private CadfAuditEvent generateCadfAuditEvent(ICadfAction action,
                                                  Date date,
                                                  String actionId,
                                                  CadfResource target,
                                                  List<CadfMeasurement> measurements,
                                                  List<CadfAttachment> attachments) {
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
            .withTags(new ArrayList<>())
            .build();
    }
}
