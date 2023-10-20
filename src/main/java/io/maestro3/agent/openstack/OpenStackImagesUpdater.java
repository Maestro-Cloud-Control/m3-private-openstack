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
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.model.AdminProjectMeta;
import io.maestro3.agent.model.PlatformShapeMapping;
import io.maestro3.agent.model.base.PlatformType;
import io.maestro3.agent.model.base.TenantState;
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.model.compute.ImageStateMapping;
import io.maestro3.agent.model.compute.ImageType;
import io.maestro3.agent.model.compute.ImageVisibility;
import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.DbServicesProvider;
import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.internal.util.DateUtils;
import io.maestro3.sdk.v3.model.audit.AuditEventGroupType;
import io.maestro3.cadf.ICadfAction;
import io.maestro3.cadf.model.CadfActions;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
public class OpenStackImagesUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(OpenStackImagesUpdater.class);

    private final OpenStackApiProvider apiProvider;
    private final DbServicesProvider dbServicesProvider;
    private final CadfAuditEventSender auditEventSender;
    private final IOpenStackRegionRepository regionService;

    @Autowired
    public OpenStackImagesUpdater(OpenStackApiProvider apiProvider,
                                  IOpenStackRegionRepository regionService,
                                  DbServicesProvider dbServicesProvider,
                                  CadfAuditEventSender auditEventSender) {
        this.apiProvider = apiProvider;
        this.regionService = regionService;
        this.auditEventSender = auditEventSender;
        this.dbServicesProvider = dbServicesProvider;
    }

    public void updateImages(boolean forceUpdate) {
        Collection<OpenStackRegionConfig> regionConfigs = regionService
            .findAllOSRegionsAvailableForDescribers();
        LOG.info("Region configs : {}", regionConfigs);

        LOG.info("Gooing to execute images update");
        regionConfigs.forEach(regionConfig -> updateRegionImages(regionConfig, forceUpdate));
    }

    private void updateRegionImages(OpenStackRegionConfig regionConfig, boolean forceUpdate) {
        Collection<OpenStackTenant> tenantConfigs = dbServicesProvider.getTenantDbService()
            .findAllByRegion(regionConfig.getId());
        LOG.info("Tenant configs : {}", tenantConfigs);

        tenantConfigs.stream()
            .filter(tenant -> {
                if (tenant.isSkipHealthCheck() || tenant.getTenantState().equals(TenantState.AVAILABLE)) {
                    return true;
                }
                LOG.debug("Tenant {} skipped because it state is not AVAILABLE", tenant.getId());
                return false;
            })
            .forEach(tenantConfig -> updateTenantInRegionImages(tenantConfig, regionConfig, forceUpdate));
    }

    private void updateTenantInRegionImages(OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig, boolean forceUpdate) {
        List<OpenStackMachineImage> machineImages = dbServicesProvider.getMachineImageDbService()
            .findByRegionId(regionConfig.getId());
        LOG.info("Machine images : {}", machineImages);

        AdminProjectMeta adminProjectMeta = regionConfig.getAdminProjectMeta();
        Set<PlatformShapeMapping> publicImagesMapping = adminProjectMeta.getAvailablePublicImagesPlatformMapping();

        Set<String> availablePublicImagesNativeNames = publicImagesMapping.stream()
            .map(PlatformShapeMapping::getName)
            .collect(Collectors.toSet());

        synchronizePublicImagesAndGenerateAuditEvent(machineImages, adminProjectMeta, tenantConfig, regionConfig, forceUpdate);

        List<OpenStackMachineImage> privateImagesFromDb = machineImages.stream()
            .filter(machineImage -> !availablePublicImagesNativeNames.contains(machineImage.getNativeName()))
            .collect(Collectors.toList());
        LOG.info("Private images : {}", privateImagesFromDb);

        List<Image> privateImagesFromOs = getImagesFromOpenStack(tenantConfig, regionConfig).stream()
            .filter(image -> regionConfig.getOsVersion() == OpenStackVersion.OCATA || ImageType.SNAPSHOT == image.getType())
            .collect(Collectors.toList());
        LOG.info("Private images from OS : {}", privateImagesFromOs);

        synchronizePrivateImagesAndGenerateAuditEvent(privateImagesFromOs, privateImagesFromDb,
            tenantConfig, regionConfig);
    }

    private void synchronizePrivateImagesAndGenerateAuditEvent(List<Image> privateImagesFromOs, List<OpenStackMachineImage> privateImagesFromDb,
                                                               OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) {
        Map<String, Image> imageMap = privateImagesFromOs.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Image::getId, Function.identity()));

        if (CollectionUtils.isNotEmpty(privateImagesFromDb)) {
            privateImagesFromDb.forEach(machineImage -> {

                String nativeId = machineImage.getNativeId();

                Image image = imageMap.get(nativeId);
                ICadfAction cadfAction;

                if (image != null) {
                    String imageStatus = ImageStateMapping.fromValue(image.getStatus().getName()).getName();

                    String machineImageStatus = machineImage.getImageStatus();
                    if (!imageStatus.equals(machineImageStatus)) {
                        machineImage.setImageStatus(imageStatus);

                        dbServicesProvider.getMachineImageDbService().save(machineImage);
                        cadfAction = CadfActions.create();

                        generateAuditEventInternally(regionConfig, tenantConfig, machineImage, image, cadfAction);
                    }
                } else {
                    dbServicesProvider.getMachineImageDbService().removeByNativeId(nativeId);
                    cadfAction = CadfActions.delete();

                    generateAuditEventInternally(regionConfig, tenantConfig, machineImage, null, cadfAction);

                    LOG.info(String.format("Image with native id : %s was removed from db", nativeId));
                }
            });
        }
    }

    private void synchronizePublicImagesAndGenerateAuditEvent(List<OpenStackMachineImage> machineImages,
                                                              AdminProjectMeta adminProjectMeta,
                                                              OpenStackTenant tenantConfig,
                                                              OpenStackRegionConfig regionConfig,
                                                              boolean forceUpdate) {
        List<Image> publicImagesFromOs = listPublicImagesFromOs(tenantConfig, regionConfig);

        Set<PlatformShapeMapping> publicImagesMapping = adminProjectMeta.getAvailablePublicImagesPlatformMapping();

        Set<String> availablePublicImagesNativeNames = publicImagesMapping.stream()
            .map(PlatformShapeMapping::getName)
            .collect(Collectors.toSet());

        List<OpenStackMachineImage> publicImagesFromDb = machineImages.stream()
            .filter(machineImage -> availablePublicImagesNativeNames.contains(machineImage.getNativeName()))
            .collect(Collectors.toList());
        LOG.info("Public images from db : {}", publicImagesFromDb);

        Map<String, Image> publicImagesMap = new HashMap<>();
        for (Image image : publicImagesFromOs) {
            if (image != null && availablePublicImagesNativeNames.contains(image.getName())){
                publicImagesMap.put(image.getName(), image);
            }
        }
        LOG.info("Public images map : {}", publicImagesMap);

        if (CollectionUtils.isNotEmpty(publicImagesFromDb)) {
            publicImagesFromDb.forEach(machineImage -> {
                String imageName = machineImage.getNativeName();
                Image image = publicImagesMap.get(imageName);

                if (image != null) {
                    //check the native id's, if they are not equals - synchronize them and update machine image data in mongoDB collection
                    String nativeId = machineImage.getNativeId();
                    if (!image.getId().equals(nativeId)) {
                        String imageStatus = image.getStatus().getName();

                        machineImage.setNativeId(image.getId());
                        machineImage.setImageStatus(ImageStateMapping.fromValue(imageStatus).getName());
                        LOG.info("Going to update image : {}", machineImage);
                        dbServicesProvider.getMachineImageDbService().save(machineImage);
                    } else {
                        LOG.info("Native id's are synchronized!");
                    }
                } else {
                    /// if there is no such public image on OS  - delete it from mongoDB collection and send audit event
                    LOG.info("The requested image not found on OS, going to remove it from mongoDB collection");
                    dbServicesProvider.getMachineImageDbService().removeByNativeId(machineImage.getNativeId());

                    generateAuditEventInternally(regionConfig, tenantConfig, machineImage, null, CadfActions.delete());
                }
            });
        }

        Collection<Image> publicImagesList = publicImagesMap.values();

        if (CollectionUtils.isNotEmpty(publicImagesList)) {
            //Check the presence of required public images in mongoDB collection
            publicImagesList.forEach(publicImage -> {
                OpenStackMachineImage machineImage = publicImagesFromDb.stream()
                    .filter(Objects::nonNull)
                    .filter(image -> image.getNativeName().equalsIgnoreCase(publicImage.getName()))
                    .findFirst()
                    .orElse(null);

                PlatformShapeMapping platformShapeMapping = null;
                for (PlatformShapeMapping mapping : publicImagesMapping) {
                    if (mapping.getName().equals(publicImage.getName())) {
                        platformShapeMapping = mapping;
                        break;
                    }
                }

                String platformTypeName = platformShapeMapping.getPlatformType();
                PlatformType platformType = PlatformType.fromShortLabel(String.valueOf(platformTypeName.charAt(0)));

                //The required public image present on OS but absent on mongDB collection
                String imageStatus = ImageStateMapping.fromValue(publicImage.getStatus().getName()).getName();

                int requiredMinStorageSize = platformShapeMapping.getMinStorageSizeGb();
                double requiredMinMemorySize = platformShapeMapping.getMinMemoryGb();

                OpenStackMachineImage updatedMachineImage = new OpenStackMachineImage();
                updatedMachineImage.setNativeId(publicImage.getId());
                updatedMachineImage.setNativeName(publicImage.getName());
                updatedMachineImage.setNameAlias(publicImage.getName());
                updatedMachineImage.setRegionId(regionConfig.getId());
                updatedMachineImage.setImageStatus(imageStatus);
                updatedMachineImage.setPlatformType(platformType);
                updatedMachineImage.setImageVisibility(ImageVisibility.PUBLIC);
                updatedMachineImage.setRequiredMinMemoryGb(requiredMinMemorySize);
                updatedMachineImage.setRequiredMinStorageSizeGb(requiredMinStorageSize);

                if (machineImage == null) {
                    dbServicesProvider.getMachineImageDbService().save(updatedMachineImage);
                    generateAuditEventInternally(regionConfig, tenantConfig, updatedMachineImage, publicImage, CadfActions.create());
                } else if (forceUpdate) {
                    generateAuditEventInternally(regionConfig, tenantConfig, updatedMachineImage, publicImage, CadfActions.create());
                }
            });
        }
    }

    private List<Image> listPublicImagesFromOs(OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) {
        try {
            return apiProvider.openStack(tenantConfig, regionConfig).images().image().listPublic();

        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<Image> getImagesFromOpenStack(OpenStackTenant tenantConfig, OpenStackRegionConfig regionConfig) {
        try {
            return apiProvider.openStack(tenantConfig, regionConfig).images().image().listProject(tenantConfig.getNativeId());

        } catch (OSClientException | M3PrivateAgentException e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void generateAuditEventInternally(OpenStackRegionConfig regionConfig,
                                              OpenStackTenant tenantConfig,
                                              OpenStackMachineImage machineImage,
                                              Image image,
                                              ICadfAction cadfAction) {

        Date date = image == null ? new Date(System.currentTimeMillis()) : image.getCreated();
        double imageSize = image == null ? 0 : image.getSize();

        String actionId = new ObjectId().toHexString();
        CadfResource target = createTarget(machineImage.getNativeId());

        List<CadfMeasurement> measurements = CadfUtils.resolveImageMeasurements(imageSize);
        List<CadfAttachment> attachments = CadfUtils.resolveImageAttachments(machineImage, tenantConfig, regionConfig, image);

        CadfAuditEvent cadfAuditEvent = generateCadfAuditEvent(cadfAction, date, actionId, target, measurements, attachments);

        auditEventSender.sendCadfAuditEvent(cadfAuditEvent, Collections.singletonList(AuditEventGroupType.IMAGE_DATA));
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
            .withOutcome(CadfOutcomes.success())
            .withAttachments(attachments)
            .withMeasurements(measurements)
            .withTags(new ArrayList<>())
            .withTarget(target)
            .build();
    }

    private CadfResource createTarget(String imageId) {
        return CadfResource.builder()
            .ofType(CadfResourceTypes.data().image())
            .withId(CadfUtils.ID_NAMESPACE + imageId)
            .withName(imageId)
            .build();
    }

}
