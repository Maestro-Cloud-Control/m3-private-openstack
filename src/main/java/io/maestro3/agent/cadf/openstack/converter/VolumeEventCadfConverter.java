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
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.model.notification.EventType;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolumeAttachment;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.agent.service.VolumeDbService;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
public class VolumeEventCadfConverter extends AbstractEventCadfConverter {

    private static final Logger LOG = LoggerFactory.getLogger(VolumeEventCadfConverter.class);

    private static final String ID_NAMESPACE = "maestro2:";
    private static final String STRING_CONTENT_TYPE = CadfUtils.STRING_CONTENT_TYPE;

    private final DbServicesProvider dbServicesProvider;
    private final OpenStackApiProvider apiProvider;
    private final IOpenStackRegionRepository regionService;

    @Autowired
    public VolumeEventCadfConverter(DbServicesProvider dbServicesProvider,
                                    IOpenStackRegionRepository regionService,
                                    OpenStackApiProvider apiProvider) {
        this.regionService = regionService;
        this.dbServicesProvider = dbServicesProvider;
        this.apiProvider = apiProvider;
    }

    @Override
    protected CadfAuditEvent doConvert(ICadfAction action, Notification notification) {
        Map<String, Object> payload = notification.getPayload();

        String tenantId = (String) payload.get("tenant_id");

        OpenStackTenant tenantConfig = dbServicesProvider.getTenantDbService().findOpenStackTenantByNativeId(tenantId);
        OpenStackRegionConfig regionConfig = regionService.findByIdInCloud(tenantConfig.getRegionId());

        String volumeId = (String) payload.get("volume_id");

        CinderVolume cinderVolume;
        try {
            cinderVolume = extractCinderVolume(volumeId, tenantConfig, regionConfig);
            LOG.info("Extracted cinder volume : {}", cinderVolume);

            List<CadfAttachment> attachments = resolveAttachments(cinderVolume, tenantConfig, regionConfig);
            List<CadfMeasurement> measurements = resolveMeasurements(cinderVolume);

            Date date = DateUtils.parseDate(notification.getTimestamp(), NOTIFICATION_TIMESTAMP_DATE_FORMAT);

            CadfResource target = createTarget(volumeId);
            String actionId = new ObjectId().toHexString();

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

        } catch (M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private List<CadfAttachment> resolveAttachments(CinderVolume cinderVolume, OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) throws M3PrivateAgentException {

        String volumeId = cinderVolume.getId();
        String status = cinderVolume.getStatus();
        String serverNativeName = extractServerNativeName(cinderVolume, tenantConfig, regionConfig);

        List<CadfAttachment> result = new ArrayList<>();

        CadfAttachment<Object> cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "instanceId");
        cadfAttachment.setContent(serverNativeName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "tenantName");
        cadfAttachment.setContent(tenantConfig.getTenantAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "regionName");
        cadfAttachment.setContent(regionConfig.getRegionAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "volumeId");
        cadfAttachment.setContent(volumeId);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>(STRING_CONTENT_TYPE, "state");
        cadfAttachment.setContent(status);
        result.add(cadfAttachment);

        return result;
    }

    private String extractServerNativeName(CinderVolume cinderVolume, OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) throws M3PrivateAgentException {
        String serverId;
        CinderVolumeAttachment cinderVolumeAttachment = cinderVolume.getAttachments()
                .stream()
                .filter(attachment -> attachment.getServerId() != null)
                .findFirst()
                .orElse(null);

        if (cinderVolumeAttachment != null) {
            serverId = cinderVolumeAttachment.getServerId();

            return extractServerNativeName(serverId, tenantConfig, regionConfig);
        }
        throw new M3PrivateAgentException("Volume has no attachments");
    }

    private String extractServerNativeName(String serverId, OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) {
        String tenantId = tenantConfig.getId();
        String regionId = regionConfig.getId();
        OpenStackServerConfig serverConfig = dbServicesProvider.getServerDbService().findServerByNativeId(regionId, tenantId, serverId);

        return serverConfig.getNativeName();
    }

    private CinderVolume extractCinderVolume(String volumeId, OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) throws M3PrivateAgentException {
        VolumeDbService volumeDbService = dbServicesProvider.getVolumeDbService();

        CinderVolume cinderVolume = volumeDbService.findById(volumeId);
        if (cinderVolume == null) {
            cinderVolume = getVolumeFromOs(tenantConfig, regionConfig, volumeId);
            volumeDbService.save(cinderVolume);
        }
        return cinderVolume;
    }

    private CinderVolume getVolumeFromOs(OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig, String volumeId) throws M3PrivateAgentException {
        try {
            return apiProvider.openStack(tenantConfig, regionConfig).blockStorage().volumes().get(volumeId);
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);

            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    private List<CadfMeasurement> resolveMeasurements(CinderVolume cinderVolume) {
        String sizeMb = String.valueOf(cinderVolume.getSize() * 1024);
        List<CadfMeasurement> result = new ArrayList<>();

        CadfMeasurement<String> measurement = CadfMeasurement.<String>builder()
                .withMetricId(ID_NAMESPACE + "volSizeMb")
                .withResult(sizeMb)
                .build();
        result.add(measurement);

        return result;
    }

    private CadfResource createTarget(String volumeId) {
        return CadfResource.builder()
                .ofType(CadfResourceTypes.storage().volume())
                .withId(ID_NAMESPACE + volumeId)
                .withName(volumeId)
                .build();
    }

    @Override
    public List<EventType> getSupportedEventTypes() {
        return Collections.singletonList(EventType.VOLUME_ATTACH_END);
    }

    @Override
    public List<AuditEventGroupType> getOutputEventGroups() {
        return Arrays.asList(AuditEventGroupType.BILLING_AUDIT, AuditEventGroupType.VOLUME_DATA);
    }

    private final CadfResource system = CadfResource.builder()
            .ofType(CadfResourceTypes.system())
            .withId(ID_NAMESPACE + "SYSTEM")
            .withName("System")
            .build();

}
