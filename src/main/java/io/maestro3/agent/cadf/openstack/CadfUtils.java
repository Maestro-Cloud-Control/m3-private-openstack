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

package io.maestro3.agent.cadf.openstack;

import io.maestro3.agent.model.base.DiskState;
import io.maestro3.agent.model.base.IWithTags;
import io.maestro3.agent.model.base.ResourceTag;
import io.maestro3.agent.model.base.ShapeConfig;
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.model.compute.ImageStateMapping;
import io.maestro3.agent.model.compute.ImageVisibility;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfEventType;
import io.maestro3.cadf.model.CadfMeasurement;
import io.maestro3.cadf.model.CadfOutcomes;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.cadf.model.CadfResourceTypes;
import io.maestro3.cadf.model.CadfTag;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.internal.util.JsonUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.agent.openstack.SdkOsDiskInfo;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface CadfUtils {

    String STRING_CONTENT_TYPE = "string";
    String BOOLEAN_CONTENT_TYPE = "boolean";
    String ID_NAMESPACE = "maestro2:";

    String UNKNOWN = "UNKNOWN";

    CadfResource SYSTEM = CadfResource.builder()
        .ofType(CadfResourceTypes.system())
        .withId(ID_NAMESPACE + "SYSTEM")
        .withName("System")
        .build();

    CadfResource PRIVATE_AGENT = CadfResource.builder()
        .ofType(CadfResourceTypes.unknown())
        .withId(ID_NAMESPACE + "PRIVATE_AGENT")
        .withName("System")
        .build();


    static CadfResource createTarget(String instanceId) {
        return CadfResource.builder()
            .ofType(CadfResourceTypes.compute().machine().vm())
            .withId(ID_NAMESPACE + instanceId)
            .withName(instanceId)
            .build();
    }

    static List<CadfAttachment> resolveImageAttachments(OpenStackMachineImage machineImage,
                                                        OpenStackTenant tenantConfig,
                                                        OpenStackRegionConfig regionConfig,
                                                        Image image) {
        List<CadfAttachment> result = new ArrayList<>();

        String imageName = machineImage.getNativeName();

        String osType = machineImage.getPlatformType().name();
        ImageVisibility visibility = machineImage.getImageVisibility();
        String imageVisibility = visibility == null ? ImageVisibility.PRIVATE.name() : visibility.name();

        int minStorageSize = machineImage.getRequiredMinStorageSizeGb();
        double minRamSize = machineImage.getRequiredMinMemoryGb();

        CadfAttachment<Object> cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "type");
        cadfAttachment.setContent("OpenStackMachineImage");
        result.add(cadfAttachment);

        String description = "";
        String imageState = ImageStateMapping.UNKNOWN.getName();
        long createdDate = Long.MIN_VALUE;
        if (image != null) {
            String changedState = image.getStatus().getName();
            imageState = ImageStateMapping.fromValue(changedState).getName();
            description = image.getDescription();
            createdDate = image.getCreated().getTime();

        }

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "description");
        cadfAttachment.setContent(description);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "state");
        cadfAttachment.setContent(imageState);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("date", "createdDate");
        cadfAttachment.setContent(createdDate);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "tenantName");
        cadfAttachment.setContent(tenantConfig.getTenantAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "regionName");
        cadfAttachment.setContent(regionConfig.getRegionAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "name");
        cadfAttachment.setContent(imageName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "imageId");
        cadfAttachment.setContent(machineImage.getNativeId());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "osType");
        cadfAttachment.setContent(osType.charAt(0));
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "regionId");
        cadfAttachment.setContent(regionConfig.getId());

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "tenantId");
        cadfAttachment.setContent(tenantConfig.getNativeId());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "imageType");
        cadfAttachment.setContent(imageVisibility);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "alias");
        cadfAttachment.setContent(imageName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "minRamSize");
        cadfAttachment.setContent(minRamSize);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "minStorageSize");
        cadfAttachment.setContent(minStorageSize);
        result.add(cadfAttachment);

        return result;
    }

    static List<CadfAttachment> resolveInstanceAttachments(ICadfAction action,
                                                           List<SdkOsDiskInfo> attachedDisks,
                                                           OpenStackServerConfig serverConfig,
                                                           OpenStackTenant tenantConfig,
                                                           OpenStackRegionConfig regionConfig,
                                                           OpenStackFlavorConfig shapeConfig,
                                                           OpenStackMachineImage image,
                                                           Date updatedDate,
                                                           Date createdDate,
                                                           ServerStateEnum currentServerState) {
        List<CadfAttachment> result = new ArrayList<>();

        CadfAttachment<Object> cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "type");
        cadfAttachment.setContent("OpenStackInstance");
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("request", "request");
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("tenant", tenantConfig.getTenantAlias());
        requestParameters.put("region", regionConfig.getRegionAlias());
        requestParameters.put("cloud", SdkCloud.OPEN_STACK.name());
        requestParameters.put("instanceId", serverConfig.getNameAlias());
        cadfAttachment.setContent(requestParameters);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "instanceState");
        cadfAttachment.setContent(currentServerState.getSdkState().getStateName());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "shape");
        cadfAttachment.setContent(shapeConfig != null ? shapeConfig.getNameAlias() : UNKNOWN);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "vmName");
        cadfAttachment.setContent(serverConfig.getNativeName());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "machineImage");
        String nameAlias = image == null ? UNKNOWN : image.getNameAlias();
        cadfAttachment.setContent(nameAlias);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "tenantName");
        cadfAttachment.setContent(tenantConfig.getTenantAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "m3Instance");
        cadfAttachment.setContent(serverConfig.isOur());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "regionName");
        cadfAttachment.setContent(regionConfig.getRegionAlias());
        result.add(cadfAttachment);

        if (serverConfig.getNetworkInterfaceInfo() != null) {
            cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "privateIp");
            cadfAttachment.setContent(serverConfig.getNetworkInterfaceInfo().getPrivateIP());
            result.add(cadfAttachment);
            cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "privateDnsName");
            cadfAttachment.setContent(serverConfig.getNetworkInterfaceInfo().getPrivateDns());
            result.add(cadfAttachment);
        }

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "publicIp");
        cadfAttachment.setContent(null);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "publicDnsName");
        cadfAttachment.setContent(null);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "searchId");
        cadfAttachment.setContent(serverConfig.getNameAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "nativeId");
        cadfAttachment.setContent(serverConfig.getNativeId());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "keyName");
        cadfAttachment.setContent(serverConfig.getKeyName());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "vmOs");
        cadfAttachment.setContent(image == null ? UNKNOWN : image.getPlatformType().getFullLabel());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "vmCpuType");
        cadfAttachment.setContent(shapeConfig != null ? shapeConfig.getProcessorType() : UNKNOWN);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "availabilityZone");
        cadfAttachment.setContent(serverConfig.getAvailabilityZone());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("date", "createdDate");
        cadfAttachment.setContent(createdDate);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("date", "updatedDate");
        cadfAttachment.setContent(updatedDate);
        result.add(cadfAttachment);

        if (CollectionUtils.isNotEmpty(attachedDisks)) {
            cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "vmStorageInfo");
            cadfAttachment.setContent(JsonUtils.convertObjectToJson(attachedDisks));
            result.add(cadfAttachment);
        }

        if (CollectionUtils.isNotEmpty(serverConfig.getSecurityGroups())) {
            cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "vmSecurityGroups");
            cadfAttachment.setContent(JsonUtils.convertObjectToJson(serverConfig.getSecurityGroups()));
            result.add(cadfAttachment);
        }

        String name = serverConfig.getNameAlias();
        if (StringUtils.isNotBlank(name)) {
            result.add(CadfAttachment.<String>builder()
                .withContentType(STRING_CONTENT_TYPE)
                .withName("name")
                .withContent(name)
                .build());
        }

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "description");
        cadfAttachment.setContent(OpenStackEventTypeActionMapping.getDescriptionByCadfAction(action));
        result.add(cadfAttachment);

        return result;
    }

    static List<CadfMeasurement> resolveInstanceMeasurements(OpenStackMachineImage image, ShapeConfig config) {
        List<CadfMeasurement> result = new ArrayList<>();
        int cpu = 0;
        long mem = 0;
        if (config != null) {
            cpu = config.getCpuCount();
            mem = config.getMemorySizeMb();
        }
        CadfMeasurement measurement = CadfMeasurement.builder()
            .withMetricId(ID_NAMESPACE + "vmCpuCount")
            .withResult(cpu)
            .build();
        result.add(measurement);

        measurement = CadfMeasurement.builder()
            .withMetricId(ID_NAMESPACE + "vmMemoryMB")
            .withResult(mem)
            .build();
        result.add(measurement);
        return result;
    }

    static List<CadfMeasurement> resolveImageMeasurements(double machineImageSize) {
        if (machineImageSize != 0) {
            double result = machineImageSize / (1024 * 1024);

            CadfMeasurement<String> measurement = CadfMeasurement.<String>builder()
                .withMetricId(ID_NAMESPACE + ":imageSizeMb")
                .withResult(String.valueOf(result))
                .build();
            return Collections.singletonList(measurement);
        } else {
            return Collections.emptyList();
        }
    }

    static List<CadfTag> prepareCadfTags(IWithTags resource) {
        List<CadfTag> tags = new ArrayList<>();
        if (resource == null || CollectionUtils.isEmpty(resource.getTags())) {
            return tags;
        }
        for (ResourceTag tag : resource.getTags()) {
            CadfTag cadfTag = new CadfTag();
            cadfTag.setName(tag.getKey());
            cadfTag.setValue(tag.getValue());
            tags.add(cadfTag);
        }
        return tags;
    }


    static CadfResource createVolumeTarget(String volumeId) {
        return CadfResource.builder()
            .ofType(CadfResourceTypes.storage().volume())
            .withId(ID_NAMESPACE + volumeId)
            .withName(volumeId)
            .build();
    }

    static List<CadfMeasurement> resolveVolumeMeasurements(int diskSizeGb) {
        List<CadfMeasurement> result = new ArrayList<>();

        CadfMeasurement<String> measurement = CadfMeasurement.<String>builder()
            .withMetricId(ID_NAMESPACE + "volSizeMb")
            .withResult(String.valueOf(diskSizeGb * 1024))
            .build();
        result.add(measurement);

        return result;
    }

    static List<CadfAttachment> resolveVolumeAttachments(OpenStackTenant tenantConfig,
                                                         OpenStackRegionConfig regionConfig,
                                                         OpenStackServerConfig server, CinderVolume disk,
                                                         String volumeId,
                                                         DiskState state) {
        List<CadfAttachment> result = new ArrayList<>();

        CadfAttachment<Object> cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "tenantName");
        cadfAttachment.setContent(tenantConfig.getTenantAlias());
        result.add(cadfAttachment);

        if (StringUtils.isNotBlank(disk.getHost())) {
            cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "instanceId");
            cadfAttachment.setContent(server != null ? server.getNameAlias() : null);
            result.add(cadfAttachment);
        }
        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "regionName");
        cadfAttachment.setContent(regionConfig.getRegionAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "volumeId");
        cadfAttachment.setContent(volumeId == null ? disk.getId() : volumeId);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "volumeUuid");
        cadfAttachment.setContent(disk.getId());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "cloud");
        cadfAttachment.setContent(SdkCloud.OPEN_STACK.name());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "diskName");
        cadfAttachment.setContent(disk.getName() != null ? disk.getName() : disk.getId());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(BOOLEAN_CONTENT_TYPE, "bootableDisk");
        cadfAttachment.setContent(disk.isBootable());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "state");
        cadfAttachment.setContent(state.getStateString());
        result.add(cadfAttachment);

        return result;
    }


    static CadfAuditEvent generateVolumeErrorAuditEvent(String tenantName, String regionName, String volumeId) {

        List<CadfAttachment> result = new ArrayList<>();

        CadfAttachment<Object> cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "tenantName");
        cadfAttachment.setContent(tenantName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "regionName");
        cadfAttachment.setContent(regionName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "volumeId");
        cadfAttachment.setContent(volumeId);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "state");
        cadfAttachment.setContent("error");
        result.add(cadfAttachment);

        Date date = DateTime.now().withZone(DateTimeZone.UTC).toDate();

        CadfResource target = CadfUtils.createVolumeTarget(volumeId);
        String actionId = new ObjectId().toHexString();

        return generateCadfAuditEvent(CadfActions.error(), date, actionId, target, Collections.emptyList(), result, Collections.emptyList());
    }

    static CadfAuditEvent generateCadfAuditEvent(ICadfAction action,
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
            .withOutcome(CadfOutcomes.success())
            .withMeasurements(measurements)
            .withAttachments(attachments)
            .withTags(tags)
            .build();
    }
}
