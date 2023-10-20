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

package io.maestro3.agent.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.maestro3.agent.model.enums.ServerStateEnum;
import io.maestro3.agent.model.general.ServerConfig;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenStackServerConfig extends ServerConfig {

    @NotBlank
    private String nativeId;
    @NotBlank
    private String nativeName;
    @NotBlank
    private String flavorId;
    @NotBlank
    private String imageId;
    @NotBlank
    private String ownerUserId;
    @NotNull
    private ServerStateEnum state;

    private OpenStackNetworkInterfaceInfo networkInterfaceInfo;
    private Map<String, String> metadata;
    private List<String> securityGroups = new ArrayList<>();
    private String keyName;
    private String availabilityZone;
    private Set<String> attachedVolumes = new HashSet<>();
    private boolean isOur;
    private Long lastIpOperationDate;

    public Long getLastIpOperationDate() {
        return lastIpOperationDate;
    }

    public void setLastIpOperationDate(Long lastIpOperationDate) {
        this.lastIpOperationDate = lastIpOperationDate;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public boolean isOur() {
        return isOur;
    }

    public void setOur(boolean our) {
        isOur = our;
    }

    public Set<String> getAttachedVolumes() {
        return attachedVolumes;
    }

    public void setAttachedVolumes(Set<String> attachedVolumes) {
        this.attachedVolumes = attachedVolumes;
    }

    public String getNativeId() {
        return nativeId;
    }

    public void setNativeId(String nativeId) {
        this.nativeId = nativeId;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public OpenStackNetworkInterfaceInfo getNetworkInterfaceInfo() {
        return networkInterfaceInfo != null ? networkInterfaceInfo : new OpenStackNetworkInterfaceInfo();
    }

    public void setNetworkInterfaceInfo(OpenStackNetworkInterfaceInfo networkInterfaceInfo) {
        this.networkInterfaceInfo = networkInterfaceInfo;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public ServerStateEnum getState() {
        return state;
    }

    public void setState(ServerStateEnum state) {
        this.state = state;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }
}
