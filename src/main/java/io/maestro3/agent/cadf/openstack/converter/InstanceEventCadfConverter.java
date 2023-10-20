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
import io.maestro3.agent.cadf.openstack.OpenStackEventTypeActionMapping;
import io.maestro3.agent.converter.OpenStackServerStateDetector;
import io.maestro3.agent.dao.IInstanceRunRecordDao;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.model.base.InstanceRunRecord;
import io.maestro3.agent.model.base.ShapeConfig;
import io.maestro3.agent.model.compute.Address;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.notification.EventType;
import io.maestro3.agent.model.notification.Notification;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackNetworkInterfaceInfo;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.internal.util.StringUtils;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfAttachment;
import io.maestro3.cadf.model.CadfAuditEvent;
import io.maestro3.cadf.model.CadfEventType;
import io.maestro3.cadf.model.CadfMeasurement;
import io.maestro3.cadf.model.CadfOutcomes;
import io.maestro3.cadf.model.CadfResource;
import io.maestro3.cadf.model.CadfResourceTypes;
import io.maestro3.cadf.model.CadfTag;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class InstanceEventCadfConverter extends AbstractEventCadfConverter {

    private static final Logger LOG = LoggerFactory.getLogger(InstanceEventCadfConverter.class);

    private static final String ID_NAMESPACE = "maestro2:";

    private final CadfResource system = CadfResource.builder()
        .ofType(CadfResourceTypes.system())
        .withId(ID_NAMESPACE + "SYSTEM")
        .withName("System")
        .build();

    private final DbServicesProvider dbServicesProvider;
    private final OpenStackApiProvider apiProvider;
    private final IOpenStackRegionRepository regionService;
    private final IInstanceRunRecordDao instanceRunRecordDao;


    @Autowired
    public InstanceEventCadfConverter(DbServicesProvider dbServicesProvider,
                                      IOpenStackRegionRepository regionService,
                                      OpenStackApiProvider apiProvider,
                                      IInstanceRunRecordDao instanceRunRecordDao) {
        this.dbServicesProvider = dbServicesProvider;
        this.instanceRunRecordDao = instanceRunRecordDao;
        this.regionService = regionService;
        this.apiProvider = apiProvider;
    }

    @Override
    public CadfAuditEvent doConvert(ICadfAction action, Notification notification) {
        String tenantId = (String) notification.getPayload().get("tenant_id");
        OpenStackTenant tenantConfig = dbServicesProvider.getTenantDbService().findOpenStackTenantByNativeId(tenantId);
        OpenStackRegionConfig regionConfig = regionService.findByIdInCloud(tenantConfig.getRegionId());

        String instanceId = (String) notification.getPayload().get("instance_id");

        OpenStackServerConfig server = dbServicesProvider.getServerDbService()
            .findServerByNativeId(tenantConfig.getRegionId(), tenantConfig.getId(), instanceId);

        if (server != null) {
            updateDbServerConfigState(regionConfig, tenantConfig, server);
        }

        Date date = DateUtils.parseDate(notification.getTimestamp(), NOTIFICATION_TIMESTAMP_DATE_FORMAT);

        String actionId = new ObjectId().toHexString();
        CadfResource target = createTarget(instanceId);

        String flavorId = (String) notification.getPayload().get("instance_flavor_id");
        ShapeConfig shapeConfig = null;
        for (OpenStackFlavorConfig allowedShape : regionConfig.getAllowedShapes()) {
            if (allowedShape.getNativeId().equals(flavorId)) {
                shapeConfig = allowedShape;
                break;
            }
        }

        String imageId = server == null ? null : server.getImageId();
        OpenStackMachineImage image = null;
        if (StringUtils.isNotBlank(imageId)) {
            image = dbServicesProvider.getMachineImageDbService().findByNativeId(imageId);
        }
        List<CadfMeasurement> measurements = resolveMeasurements(notification, shapeConfig);
        List<CadfAttachment> attachments = resolveAttachments(notification, server, tenantConfig, regionConfig,
            shapeConfig, image, action);
        List<CadfTag> cadfTags = server == null ? Collections.emptyList() : CadfUtils.prepareCadfTags(server);
        return CadfAuditEvent.builder()
            .withId(ID_NAMESPACE + actionId)
            .withAction(action)
            .withEventTime(DateUtils.formatDate(date, DateUtils.CADF_FORMAT_TIME))
            .withEventType(CadfEventType.ACTIVITY)
            .withInitiator(system)
            .withObserver(system)
            .withTarget(target)
            .withOutcome(CadfOutcomes.success()) // all billing events are of this type, need to add logic to convert other events
            .withMeasurements(measurements)
            .withAttachments(attachments)
            .withTags(cadfTags)
            .build();
    }

    private void updateDbServerConfigState(OpenStackRegionConfig regionConfig, OpenStackTenant tenantConfig,
                                           OpenStackServerConfig dbServer) {
        try {
            Server osServer = getServerFromOpenStack(regionConfig, tenantConfig, dbServer.getNativeId());
            String privateIpAddress = null;
            ServerStateEnum serverState;

            if (osServer != null) {
                serverState = OpenStackServerStateDetector.toServerState(
                    osServer.getStatus(), osServer.getPowerState(), osServer.getTaskState());
                if (dbServer.getState() != serverState) {
                    List<Address> addressList = osServer.getAddresses().getPrivateAddresses();
                    if (CollectionUtils.isNotEmpty(addressList)) {
                        privateIpAddress = addressList.get(0).getIp();
                        setPrivateIp(dbServer, privateIpAddress);
                    }
                }
            } else {
                serverState = ServerStateEnum.TERMINATED;
            }
            Boolean runSuccess = null;
            if (dbServer.isOur() && !dbServer.isInstanceRunSuccess() && serverState == ServerStateEnum.RUNNING) {
                runSuccess = true;
                instanceRunRecordDao.save(new InstanceRunRecord(regionConfig.getId(), tenantConfig.getId(),
                    dbServer.getImageId(), System.currentTimeMillis() - dbServer.getStartTime()));
            }
            dbServicesProvider.getServerDbService().updateServerConfig(
                dbServer.getId(),
                serverState,
                privateIpAddress,
                runSuccess);
        } catch (M3PrivateAgentException e) {
            LOG.error(e.getMessage());
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

    private Server getServerFromOpenStack(OpenStackRegionConfig region, OpenStackTenant tenant, String serverId) throws M3PrivateAgentException {
        try {
            return apiProvider.openStack(tenant, region).compute().servers().get(serverId);
        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);

            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    private List<CadfAttachment> resolveAttachments(Notification notification, OpenStackServerConfig server, OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig, ShapeConfig config, OpenStackMachineImage image, ICadfAction cadfAction) {
        List<CadfAttachment> result = new ArrayList<>();

        String privateIp;
        if (server != null) {
            privateIp = server.getNetworkInterfaceInfo().getPrivateIP();
            if (privateIp == null) {
                privateIp = extractPrivateIp(notification);
            }
        } else {
            privateIp = extractPrivateIp(notification);
        }

        CadfAttachment cadfAttachment = new CadfAttachment<>("string", "type");
        cadfAttachment.setContent("OpenStackInstance");
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("request", "request");
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("tenant", tenantConfig.getTenantAlias());
        requestParameters.put("region", regionConfig.getRegionAlias());
        requestParameters.put("cloud", SdkCloud.OPEN_STACK.name());
        String instanceName = server == null ? extractInstanceName(notification) : server.getNameAlias();
        requestParameters.put("instanceId", instanceName);
        cadfAttachment.setContent(requestParameters);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "instanceState");
        cadfAttachment.setContent(OpenStackEventTypeActionMapping.getStateByEventType(notification.getEventType()).getStateName());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "shape");
        cadfAttachment.setContent(config.getNameAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "machineImage");
        String nameAlias = image == null ? "unknown" : image.getNameAlias();
        cadfAttachment.setContent(nameAlias);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "tenantName");
        cadfAttachment.setContent(tenantConfig.getTenantAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "regionName");
        cadfAttachment.setContent(regionConfig.getRegionAlias());
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "privateIp");
        cadfAttachment.setContent(privateIp);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "publicIp");
        cadfAttachment.setContent(null);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "privateDnsName");
        String privateDns = server == null ? null : server.getNetworkInterfaceInfo().getPrivateDns();
        cadfAttachment.setContent(privateDns);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "publicDnsName");
        cadfAttachment.setContent(null);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "searchId");
        cadfAttachment.setContent(instanceName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "keyName");
        String keyName = server == null ? null : server.getKeyName();
        cadfAttachment.setContent(keyName);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "availabilityZone");
        cadfAttachment.setContent(null);
        result.add(cadfAttachment);

        Date createdDate = Date.from(LocalDateTime.from(OffsetDateTime
            .parse(((String) notification.getPayload().get("created_at"))
                .replace(" ", "T")))
            .toInstant(ZoneOffset.UTC));

        cadfAttachment = new CadfAttachment<>("date", "createdDate");
        cadfAttachment.setContent(createdDate);
        result.add(cadfAttachment);

        Date updatedDate = Date.from(LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            .parse(notification.getTimestamp()))
            .toInstant(ZoneOffset.UTC));

        cadfAttachment = new CadfAttachment<>("date", "updatedDate");
        cadfAttachment.setContent(updatedDate);
        result.add(cadfAttachment);

        cadfAttachment = new CadfAttachment<>("string", "description");
        cadfAttachment.setContent(OpenStackEventTypeActionMapping.getDescriptionByCadfAction(cadfAction));
        result.add(cadfAttachment);

        String name = (String) notification.getPayload().get("display_name");
        if (StringUtils.isNotBlank(name)) {
            result.add(CadfAttachment.<String>builder()
                .withContentType("string")
                .withName("name")
                .withContent(name)
                .build());
        }

        return result;
    }

    private String extractPrivateIp(Notification notification) {
        String privateIp = null;
        List<Map<String, Object>> apiInfoMap = (List<Map<String, Object>>) notification.getPayload().get("fixed_ips");
        if (apiInfoMap != null) {
            for (Map<String, Object> objectMap : apiInfoMap) {
                String address = (String) objectMap.get("address");
                if (address != null) {
                    privateIp = address;
                }
            }
        }
        return privateIp;
    }

    private String extractInstanceName(Notification notification) {
        Map<String, Object> payload = notification.getPayload();
        return (String) payload.get("display_name");
    }

    private List<CadfMeasurement> resolveMeasurements(Notification notification, ShapeConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ShapeConfig can not be null");
        }
        List<CadfMeasurement> result = new ArrayList<>();

        CadfMeasurement measurement = CadfMeasurement.builder()
            .withMetricId(ID_NAMESPACE + "vmCpuCount")
            .withResult(config.getCpuCount())
            .build();
        result.add(measurement);

        measurement = CadfMeasurement.builder()
            .withMetricId(ID_NAMESPACE + "vmMemoryMB")
            .withResult(config.getMemorySizeMb())
            .build();
        result.add(measurement);

        String osType = (String) notification.getOriginalNotification().get("os_type");
        if ("windows".equals(osType)) {
            measurement = CadfMeasurement.builder()
                .withMetricId(ID_NAMESPACE + "vmMemoryMB")
                .withResult("1")
                .build();
            result.add(measurement);
        }
        return result;
    }

    private CadfResource createTarget(String instanceId) {
        return CadfResource.builder()
            .ofType(CadfResourceTypes.compute().machine().vm())
            .withId(ID_NAMESPACE + instanceId)
            .withName(instanceId)
            .build();
    }

    @Override
    public List<EventType> getSupportedEventTypes() {
        return Arrays.asList(
            EventType.INSTANCE_CREATE_END, EventType.INSTANCE_POWERON_END, EventType.INSTANCE_POWEROFF_END,
            EventType.INSTANCE_SUSPEND_END, EventType.INSTANCE_SHUTDOWN_END, EventType.INSTANCE_REBOOT_END);
    }

    @Override
    public List<AuditEventGroupType> getOutputEventGroups() {
        return Arrays.asList(AuditEventGroupType.BILLING_AUDIT, AuditEventGroupType.INSTANCE_DATA, AuditEventGroupType.NATIVE_AUDIT);
    }
}
