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

package io.maestro3.agent.api;

import com.google.common.io.BaseEncoding;
import io.maestro3.agent.api.handler.IM3ApiHandler;
import io.maestro3.agent.converter.M3ApiActionInverter;
import io.maestro3.agent.converter.M3SDKModelConverter;
import io.maestro3.agent.dao.IOpenStackRegionRepository;
import io.maestro3.agent.exception.M3PrivateAgentException;
import io.maestro3.agent.exception.ReadableAgentException;
import io.maestro3.agent.lock.Locker;
import io.maestro3.agent.model.InstanceProvisioningProgress;
import io.maestro3.agent.model.base.PlatformType;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.lock.VoidOperation;
import io.maestro3.agent.model.network.StartupNetworkingConfiguration;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackNetworkInterfaceInfo;
import io.maestro3.agent.model.server.OpenStackSecurityGroupInfo;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.ResourceNameGenerator;
import io.maestro3.agent.openstack.api.compute.bean.BlockDeviceMappingV2;
import io.maestro3.agent.openstack.api.compute.bean.SecurityInfo;
import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.provider.OpenStackApiProvider;
import io.maestro3.agent.service.IOpenStackInstanceProvisioningValidationService;
import io.maestro3.agent.service.IOpenStackNetworkingProvider;
import io.maestro3.agent.service.IServiceFactory;
import io.maestro3.agent.service.MachineImageDbService;
import io.maestro3.agent.service.ServerDbService;
import io.maestro3.agent.service.TenantDbService;
import io.maestro3.sdk.v3.core.ActionType;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.core.M3RawResult;
import io.maestro3.sdk.v3.core.M3Result;
import io.maestro3.sdk.v3.model.instance.SdkInstances;
import io.maestro3.sdk.v3.model.instance.SdkOpenStackInstance;
import io.maestro3.sdk.v3.request.instance.RunInstanceRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Component("osRunInstanceHandler")
public class OsRunInstanceHandler extends AbstractInstanceHandler implements IM3ApiHandler {

    private final MachineImageDbService machineImageDbService;
    private final ResourceNameGenerator resourceNameGenerator;
    private final IServiceFactory<IOpenStackInstanceProvisioningValidationService> validationServiceFactory;
    private final IServiceFactory<IOpenStackNetworkingProvider> networkingServiceFactory;

    @Autowired
    public OsRunInstanceHandler(IOpenStackRegionRepository regionDbService, TenantDbService tenantDbService,
                                MachineImageDbService machineImageDbService,
                                ServerDbService serverDbService, ResourceNameGenerator resourceNameGenerator,
                                OpenStackApiProvider openStackApiProvider, @Qualifier("instanceLocker") Locker locker,
                                IServiceFactory<IOpenStackInstanceProvisioningValidationService> validationServiceFactory,
                                IServiceFactory<IOpenStackNetworkingProvider> networkingServiceFactory) {
        super(regionDbService, tenantDbService, serverDbService, openStackApiProvider, locker);
        this.validationServiceFactory = validationServiceFactory;
        this.networkingServiceFactory = networkingServiceFactory;
        this.machineImageDbService = machineImageDbService;
        this.resourceNameGenerator = resourceNameGenerator;
    }

    @Override
    public M3RawResult handle(M3ApiAction action) throws M3PrivateAgentException {

        RunInstanceRequest request = M3ApiActionInverter.toRunInstanceRequest(action);

        OpenStackRegionConfig region = regionDbService.findByAliasInCloud(request.getRegion());
        OpenStackTenant tenant = tenantDbService.findOpenStackTenantByNameAndRegion(request.getTenantName(), region.getId());
        if (tenant == null) {
            throw new ReadableAgentException(String.format("Tenant is not configured for alias %s in region %s",
                request.getTenantName(), request.getRegion()));
        }
        String serverName = resourceNameGenerator.generateNewServerName(region);
        OpenStackMachineImage machineImage = machineImageDbService.findOpenStackImageByAliasForProject(request.getImageId(),
            tenant.getId(), region.getId());
        if (machineImage == null) {
            throw new ReadableAgentException("Image is not configured for alias " + request.getImageId());
        }
        OpenStackFlavorConfig flavor = null;
        for (OpenStackFlavorConfig allowedShape : region.getAllowedShapes()) {
            if (allowedShape.getNameAlias().equals(request.getShape())) {
                flavor = allowedShape;
                break;
            }
        }
        if (flavor == null) {
            throw new ReadableAgentException("Shape not configured for alias " + request.getShape());
        }

        String rawScript = StringUtils.isNotBlank(request.getInitScript()) ? request.getInitScript() : StringUtils.EMPTY;
        byte[] bytes = rawScript.getBytes(StandardCharsets.UTF_8);

        String utf8EncodedScript = BaseEncoding.base64().encode(bytes);

        ServerBootInfo.Builder bootInfoBuilder = ServerBootInfo.builder()
            .server(serverName) // internal
            .ofImage(machineImage.getNativeId())
            .withFlavor(flavor.getNativeId())
            .withDiskConfig(region.getServerDiskConfig())
            .withUserData(utf8EncodedScript);

        if (machineImage.getPlatformType() != PlatformType.WIN) {
            bootInfoBuilder.withKey(request.getKeyName());
        }

        if (region.getOsVersion() == OpenStackVersion.OCATA) {
            bootInfoBuilder.withDeviceMapping(
                BlockDeviceMappingV2.fromImageToVolume(flavor.getDiskSizeMb(), machineImage.getNativeId()));
            bootInfoBuilder.useConfigDrive();
        }
        IOpenStackInstanceProvisioningValidationService validationService = validationServiceFactory.get(region);
        try {
            validationService.validateSecurityGroups(tenant, request);
        } catch (OSClientException e) {
            throw new ReadableAgentException("Invalid security group configuration", e);
        }
        StartupNetworkingConfiguration networkConfiguration = validationService.getNetworkConfiguration(tenant, request);
        bootInfoBuilder.inNetworks(networkConfiguration.getNetworkIds());
        if (StringUtils.isNotEmpty(tenant.getSecurityGroupName())) {
            bootInfoBuilder.inSecurityGroups(Collections.singleton(tenant.getSecurityGroupName()));
        }

        Server server = bootServer(openStackApiProvider, region, tenant, bootInfoBuilder, networkConfiguration);
        locker.executeOperation(tenant.getId(), (VoidOperation<M3PrivateAgentException>) () ->
            saveServerConfig(tenant, server, bootInfoBuilder.create()));

        SdkOpenStackInstance m3SdkInstance = M3SDKModelConverter.toSdkOpenStackInstance(
            request, serverName, region, tenant, machineImage, server, flavor);
        SdkInstances result = new SdkInstances();
        result.setSdkInstances(Collections.singletonList(m3SdkInstance));

        return M3Result.success(action.getId(), result);
    }

    private void saveServerConfig(OpenStackTenant tenant,
                                  Server server, ServerBootInfo bootInfo) {
        OpenStackServerConfig serverConfig = new OpenStackServerConfig();
        serverConfig.setNameAlias(bootInfo.getName());
        serverConfig.setNativeId(server.getId());
        serverConfig.setMetadata(bootInfo.getMetadata());
        serverConfig.setNativeName(bootInfo.getName());
        serverConfig.setFlavorId(bootInfo.getFlavorRef());
        serverConfig.setImageId(bootInfo.getImageRef());
        serverConfig.setKeyName(bootInfo.getKeyName());
        serverConfig.setTenantId(tenant.getId());
        serverConfig.setRegionId(tenant.getRegionId());
        serverConfig.setState(ServerStateEnum.CREATING);
        serverConfig.setStartTime(System.currentTimeMillis());
        serverConfig.setOur(true);
        serverConfig.setSecurityGroups(
            server.getSecurityGroups().stream()
                .map(SecurityInfo::getName)
                .collect(Collectors.toList()));

        OpenStackNetworkInterfaceInfo networkInterfaceInfo = new OpenStackNetworkInterfaceInfo();
        networkInterfaceInfo.setNetworkId(tenant.getNetworkId());
        OpenStackSecurityGroupInfo securityGroupInfo = new OpenStackSecurityGroupInfo();
        securityGroupInfo.setNativeName(tenant.getSecurityGroupName());
        securityGroupInfo.setNativeId(tenant.getSecurityGroupId());
        networkInterfaceInfo.setSecurityGroupInfos(Collections.singletonList(securityGroupInfo));

        M3SDKModelConverter.populateWithPrivateIp(server, networkInterfaceInfo);

        serverConfig.setNetworkInterfaceInfo(networkInterfaceInfo);
        serverDbService.saveServerConfig(serverConfig);
    }

    private Server bootServer(OpenStackApiProvider apiProvider,
                              OpenStackRegionConfig region,
                              OpenStackTenant tenant,
                              ServerBootInfo.Builder bootInfo,
                              StartupNetworkingConfiguration networkConfiguration) throws M3PrivateAgentException {
        IOpenStackNetworkingProvider networkingProvider = networkingServiceFactory.get(region);
        InstanceProvisioningProgress progress = new InstanceProvisioningProgress(tenant, region);
        try {
            networkingProvider.initializeWhileStartup(bootInfo, progress, networkConfiguration);
            Server server = apiProvider.openStack(tenant, region).compute().servers().boot(bootInfo.create());
            networkingProvider.commit(progress);
            return server;
        } catch (OSClientException e) {
            LOG.error(e.getMessage(), e);
            networkingProvider.rollback(progress);
            throw new M3PrivateAgentException(e.getMessage());
        }
    }

    @Override
    public Set<ActionType> getSupportedActions() {
        return new HashSet<>(Collections.singletonList(ActionType.RUN_INSTANCE));
    }
}
