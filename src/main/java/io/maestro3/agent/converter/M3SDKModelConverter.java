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

package io.maestro3.agent.converter;

import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.image.OpenStackMachineImage;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.server.OpenStackNetworkInterfaceInfo;
import io.maestro3.agent.model.server.OpenStackServerConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;
import io.maestro3.agent.openstack.api.compute.bean.SecurityInfo;
import io.maestro3.agent.util.M3DateUtils;
import io.maestro3.sdk.v3.model.instance.SdkInstance;
import io.maestro3.sdk.v3.model.instance.SdkInstanceState;
import io.maestro3.sdk.v3.model.instance.SdkInstances;
import io.maestro3.sdk.v3.model.instance.SdkOpenStackInstance;
import io.maestro3.sdk.v3.request.instance.RunInstanceRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public final class M3SDKModelConverter {

    private M3SDKModelConverter() {
        throw new UnsupportedOperationException();
    }

    public static SdkOpenStackInstance toSdkOpenStackInstance(RunInstanceRequest request,
                                                              String instanceName,
                                                              OpenStackRegionConfig region,
                                                              OpenStackTenant tenant,
                                                              OpenStackMachineImage machineImage,
                                                              Server server,
                                                              OpenStackFlavorConfig flavor) {
        SdkOpenStackInstance sdkOpenStackInstance = new SdkOpenStackInstance();
        sdkOpenStackInstance.setArchitecture(machineImage.getPlatformType().name());
        sdkOpenStackInstance.setOwner(request.getOwner());
        sdkOpenStackInstance.setImageId(machineImage.getNameAlias());
        sdkOpenStackInstance.setInstanceName(instanceName);
        sdkOpenStackInstance.setInstanceId(instanceName);
        sdkOpenStackInstance.setInstanceType(request.getShape());
        sdkOpenStackInstance.setTenant(tenant.getTenantAlias());
        sdkOpenStackInstance.setRegion(region.getRegionAlias());
        sdkOpenStackInstance.setState(SdkInstanceState.STARTING);
        sdkOpenStackInstance.setKeyName(request.getKeyName());
        sdkOpenStackInstance.setDiskSize(flavor.getDiskSizeMb());
        sdkOpenStackInstance.setMemoryMb((int) flavor.getMemorySizeMb());

        populateWithPrivateIp(server, sdkOpenStackInstance);

        Date created = DateTime.now().withZone(DateTimeZone.UTC).toDate();
        long dateTimestamp = created.getTime();
        String formattedDate = String.format(M3DateUtils.DATE_FORMAT, M3DateUtils.getFormattedMonth(created),
            M3DateUtils.getFormattedDay(created), M3DateUtils.getFormattedYear(created));
        sdkOpenStackInstance.setCreationDate(formattedDate);
        sdkOpenStackInstance.setCreationDateTimestamp(dateTimestamp);
        sdkOpenStackInstance.setSecurityGroupsNames(server.getSecurityGroups().stream()
            .map(SecurityInfo::getName)
            .collect(Collectors.toList()));
        return sdkOpenStackInstance;
    }

    public static SdkOpenStackInstance toSdkOpenStackInstance(OpenStackRegionConfig region,
                                                              OpenStackTenant tenant,
                                                              OpenStackServerConfig serverConfig,
                                                              SdkInstanceState instanceState) {
        SdkOpenStackInstance sdkOpenStackInstance = new SdkOpenStackInstance();
        sdkOpenStackInstance.setInstanceName(serverConfig.getNameAlias());
        sdkOpenStackInstance.setInstanceId(serverConfig.getNameAlias());
        sdkOpenStackInstance.setTenant(tenant.getTenantAlias());
        sdkOpenStackInstance.setRegion(region.getRegionAlias());
        sdkOpenStackInstance.setState(instanceState);

        return sdkOpenStackInstance;
    }

    public static SdkOpenStackInstance toSdkOpenStackInstance(OpenStackRegionConfig region,
                                                              OpenStackTenant tenant,
                                                              Server server) {
        SdkOpenStackInstance sdkOpenStackInstance = new SdkOpenStackInstance();
        sdkOpenStackInstance.setInstanceName(server.getName());
        sdkOpenStackInstance.setInstanceId(server.getName());
        sdkOpenStackInstance.setTenant(tenant.getTenantAlias());
        sdkOpenStackInstance.setRegion(region.getRegionAlias());
        SdkInstanceState state = OpenStackServerStateDetector.toM3SdkInstanceState(
            server.getStatus(), server.getPowerState(), server.getTaskState());

        populateWithPrivateIp(server, sdkOpenStackInstance);

        sdkOpenStackInstance.setState(state);

        return sdkOpenStackInstance;
    }

    private static void populateWithPrivateIp(Server server, SdkOpenStackInstance sdkOpenStackInstance) {
        Optional.ofNullable(server.getAddresses())
            .ifPresent(addresses ->
                Optional.ofNullable(addresses.getPrivateAddresses()).filter(CollectionUtils::isNotEmpty)
                    .ifPresent(addressList -> sdkOpenStackInstance.setPrivateIpAddress(addressList.get(0).getIp())));
    }

    public static void populateWithPrivateIp(Server server, OpenStackNetworkInterfaceInfo networkInterfaceInfo) {
        Optional.ofNullable(server.getAddresses())
            .ifPresent(addresses ->
                Optional.ofNullable(addresses.getPrivateAddresses()).filter(CollectionUtils::isNotEmpty)
                    .ifPresent(addressList -> networkInterfaceInfo.setPrivateIP(addressList.get(0).getIp())));
    }

    public static SdkInstances toSdkOpenStackInstancesList(OpenStackRegionConfig region,
                                                        OpenStackTenant tenant,
                                                        List<Server> serverList) {
        List<SdkInstance> sdkOpenStackInstances = serverList.stream()
            .map(server -> toSdkOpenStackInstance(region, tenant, server))
            .collect(Collectors.toList());
        SdkInstances instances = new SdkInstances();
        instances.setSdkInstances(sdkOpenStackInstances);

        return instances;
    }
}
