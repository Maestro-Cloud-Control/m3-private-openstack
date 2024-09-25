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

package io.maestro3.agent.model.region;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.maestro3.agent.model.AdminProjectMeta;
import io.maestro3.agent.model.OpenStackUserInfo;
import io.maestro3.agent.model.base.BaseAmqpRegion;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.compute.DiskConfig;
import io.maestro3.agent.model.enums.OpenStackVersion;
import io.maestro3.agent.model.flavor.OpenStackFlavorConfig;
import io.maestro3.agent.model.network.NetworkingPolicy;
import io.maestro3.agent.model.network.SecurityModeConfiguration;
import io.maestro3.agent.model.network.impl.AutoNetworkingPolicy;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenStackRegionConfig extends BaseAmqpRegion<OpenStackFlavorConfig> {

    @Range
    private int regionNumber;
    private String serverNamePrefix;
    private boolean enableScheduledDescribers;
    @NotBlank
    private String keystoneAuthUrl;
    private String nativeRegionName;
    @NotNull
    private OpenStackServiceTenantInfo serviceTenantInfo;
    @NotNull
    private OpenStackUserInfo adminUserCredentials;
    private AdminProjectMeta adminProjectMeta;
    private DiskConfig serverDiskConfig;
    private OpenStackVersion osVersion = OpenStackVersion.OTHER;
    private NetworkingPolicy networkingPolicy;
    private Map<String, SecurityModeConfiguration> securityModeConfigurations = new HashMap<>();
    private int allowedIpOperationsMinutes = 3;

    public OpenStackRegionConfig() {
        super(PrivateCloudType.OPEN_STACK);
    }


    public int getAllowedIpOperationsMinutes() {
        return allowedIpOperationsMinutes;
    }

    public void setAllowedIpOperationsMinutes(int allowedIpOperationsMinutes) {
        this.allowedIpOperationsMinutes = allowedIpOperationsMinutes;
    }

    public int getRegionNumber() {
        return regionNumber;
    }

    public NetworkingPolicy getNetworkingPolicy() {
        return networkingPolicy == null ? new AutoNetworkingPolicy() : networkingPolicy;
    }

    public OpenStackRegionConfig setNetworkingPolicy(NetworkingPolicy networkingPolicy) {
        this.networkingPolicy = networkingPolicy;
        return this;
    }

    public Map<String, SecurityModeConfiguration> getSecurityModeConfigurations() {
        return securityModeConfigurations;
    }

    public Optional<SecurityModeConfiguration> getDefaultSecurityModeConfiguration() {
        return securityModeConfigurations.values().stream()
                .filter(SecurityModeConfiguration::isDefaultMode)
                .findFirst();
    }

    public Optional<SecurityModeConfiguration> getSecurityModeConfiguration(String modeName) {
        return securityModeConfigurations.values().stream()
                .filter(mode -> Objects.equals(modeName, mode.getName()))
                .findFirst();
    }

    public void setSecurityModeConfigurations(Map<String, SecurityModeConfiguration> securityModeConfigurations) {
        this.securityModeConfigurations = securityModeConfigurations;
    }

    public OpenStackVersion getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(OpenStackVersion osVersion) {
        this.osVersion = osVersion;
    }

    public void setRegionNumber(int regionNumber) {
        this.regionNumber = regionNumber;
    }

    public String getServerNamePrefix() {
        return serverNamePrefix;
    }

    public void setServerNamePrefix(String serverNamePrefix) {
        this.serverNamePrefix = serverNamePrefix;
    }

    public boolean isEnableScheduledDescribers() {
        return enableScheduledDescribers;
    }

    public void setEnableScheduledDescribers(boolean enableScheduledDescribers) {
        this.enableScheduledDescribers = enableScheduledDescribers;
    }

    public String getKeystoneAuthUrl() {
        return keystoneAuthUrl;
    }

    public void setKeystoneAuthUrl(String keystoneAuthUrl) {
        this.keystoneAuthUrl = keystoneAuthUrl;
    }

    public String getNativeRegionName() {
        return nativeRegionName;
    }

    public void setNativeRegionName(String nativeRegionName) {
        this.nativeRegionName = nativeRegionName;
    }

    public OpenStackServiceTenantInfo getServiceTenantInfo() {
        return serviceTenantInfo;
    }

    public void setServiceTenantInfo(OpenStackServiceTenantInfo serviceTenantInfo) {
        this.serviceTenantInfo = serviceTenantInfo;
    }

    public OpenStackUserInfo getAdminUserCredentials() {
        return adminUserCredentials;
    }

    public void setAdminUserCredentials(OpenStackUserInfo adminUserCredentials) {
        this.adminUserCredentials = adminUserCredentials;
    }

    public AdminProjectMeta getAdminProjectMeta() {
        return adminProjectMeta;
    }

    public void setAdminProjectMeta(AdminProjectMeta adminProjectMeta) {
        this.adminProjectMeta = adminProjectMeta;
    }

    public DiskConfig getServerDiskConfig() {
        return serverDiskConfig;
    }

    public void setServerDiskConfig(DiskConfig serverDiskConfig) {
        this.serverDiskConfig = serverDiskConfig;
    }
}
