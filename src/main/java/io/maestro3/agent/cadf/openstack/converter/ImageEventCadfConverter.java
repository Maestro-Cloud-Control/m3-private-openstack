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

package io.maestro3.agent.cadf.openstack.converter;

import io.maestro3.agent.cadf.openstack.CadfUtils;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.model.base.PlatformType;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.notification.EventType;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import io.maestro3.sdk.v3.model.image.SdkImageState;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfEventType;
import io.maestro3.cadf.model.CadfMeasurement;
import io.maestro3.cadf.model.CadfOutcomes;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.cadf.model.CadfResourceTypes;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
public class ImageEventCadfConverter extends AbstractEventCadfConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ImageEventCadfConverter.class);

    private static final String ID_NAMESPACE = "maestro2:";
    private static final String STRING_CONTENT_TYPE = CadfUtils.STRING_CONTENT_TYPE;

    private final DbServicesProvider dbServicesProvider;
    private final IOpenStackRegionRepository regionService;

    @Autowired
    private ImageEventCadfConverter(DbServicesProvider dbServicesProvider,
                                    IOpenStackRegionRepository regionService) {
        this.dbServicesProvider = dbServicesProvider;
        this.regionService = regionService;
    }

    @Override
    protected CadfAuditEvent doConvert(ICadfAction action, Notification notification) {
        Map<String, Object> payload = notification.getPayload();

        double imageSize = 0;
        if (payload.get("size") != null) {
            imageSize = (double) payload.get("size");
        }
        String tenantId = (String) payload.get("owner");

        OpenStackTenant tenantConfig = dbServicesProvider.getTenantDbService().findOpenStackTenantByNativeId(tenantId);
        if (tenantConfig != null) {
            OpenStackRegionConfig regionConfig = regionService.findByIdInCloud(tenantConfig.getRegionId());

            Map<String, Object> propertiesMap = (Map<String, Object>) payload.get("properties");
            String osType = (String) propertiesMap.get("os_type");

            String actionId = new ObjectId().toHexString();

            Date date = DateUtils.parseDate(notification.getTimestamp(), NOTIFICATION_TIMESTAMP_DATE_FORMAT);

            String imageId = (String) payload.get("id");

            CadfResource target = createTarget(imageId);

            OpenStackMachineImage machineImage = dbServicesProvider.getMachineImageDbService()
                    .findByNativeId(imageId);

            if (machineImage != null) {
                String shortLabel = String.valueOf(osType.charAt(0));
                machineImage.setPlatformType(PlatformType.fromShortLabel(shortLabel));

                dbServicesProvider.getMachineImageDbService().save(machineImage);
            }

            List<CadfMeasurement> measurements = CadfUtils.resolveImageMeasurements(imageSize);
            List<CadfAttachment> attachments = resolveAttachments(payload, tenantConfig, regionConfig);

            return CadfAuditEvent.builder()
                    .withId(ID_NAMESPACE + actionId)
                    .withAction(action)
                    .withEventTime(DateUtils.formatDate(date, DateUtils.CADF_FORMAT_TIME))
                    .withEventType(CadfEventType.ACTIVITY)
                    .withInitiator(system)
                    .withObserver(system)
                    .withOutcome(CadfOutcomes.success())
                    .withAttachments(attachments)
                    .withMeasurements(measurements)
                    .withTags(new ArrayList<>())
                    .withTarget(target)
                    .build();
        }
        LOG.info("Tenant config with this id is not found");
        return null;
    }

    @Override
    public List<AuditEventGroupType> getOutputEventGroups() {
        return Arrays.asList(AuditEventGroupType.BILLING_AUDIT, AuditEventGroupType.IMAGE_DATA);
    }

    @Override
    public List<EventType> getSupportedEventTypes() {
        return Arrays.asList(EventType.IMAGE_UPLOAD, EventType.IMAGE_UPDATE,
                EventType.IMAGE_PREPARE, EventType.IMAGE_CREATE,
                EventType.IMAGE_DELETE, EventType.IMAGE_ACTIVATE);
    }

    private CadfResource createTarget(String imageId) {
        return CadfResource.builder()
                .ofType(CadfResourceTypes.data().image())
                .withId(ID_NAMESPACE + imageId)
                .withName(imageId)
                .build();
    }

    private List<CadfAttachment> resolveAttachments(Map<String, Object> payloadMap,
                                                    OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) {
        List<CadfAttachment> result = new ArrayList<>();

        String imageName = (String) payloadMap.get("name");
        String imageState = SdkImageState.IN_PROGRESS.getName();

        Map<String, Object> properties = (Map<String, Object>) payloadMap.get("properties");

        String osType = (String) properties.get("os_type");
        String changedState = (String) properties.get("image_state");

        if (StringUtils.isNotBlank(changedState)) {
            imageState = changedState;
        }


        CadfAttachment<Object> cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "type");
        cadfAttachment.setContent("OpenStackMachineImage");
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
        cadfAttachment.setContent(payloadMap.get("id"));
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "osType");
        cadfAttachment.setContent(osType.charAt(0));
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "description");
        cadfAttachment.setContent(payloadMap.get("description"));
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "regionId");
        cadfAttachment.setContent(regionConfig.getId());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "tenantId");
        cadfAttachment.setContent(tenantConfig.getNativeId());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "imageType");
        cadfAttachment.setContent("PRIVATE");
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "state");
        cadfAttachment.setContent(imageState);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "alias");
        cadfAttachment.setContent(imageName);
        result.add(cadfAttachment);

        Date createdDate = Date.from(LocalDateTime.from(OffsetDateTime
                .parse(((String) payloadMap.get("created_at"))
                        .replace(" ", "T")))
                .toInstant(ZoneOffset.UTC));

        cadfAttachment = new CadfAttachment<>("date", "createdDate");
        cadfAttachment.setContent(createdDate);
        result.add(cadfAttachment);

        return result;
    }

    private final CadfResource system = CadfResource.builder()
            .ofType(CadfResourceTypes.system())
            .withId(ID_NAMESPACE + "SYSTEM")
            .withName("System")
            .build();

}

