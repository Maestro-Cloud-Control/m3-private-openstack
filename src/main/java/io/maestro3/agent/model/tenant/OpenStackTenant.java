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

package io.maestro3.agent.model.tenant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;
import io.maestro3.agent.model.OpenStackUserInfo;
import io.maestro3.agent.model.base.BaseTenant;
import io.maestro3.agent.model.base.PrivateCloudType;
import io.maestro3.agent.model.network.NetworkType;
import io.maestro3.agent.model.network.SecurityGroupType;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;



@Document(collection = "Tenants")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenStackTenant extends BaseTenant {

    @NotBlank
    private String nativeId;
    @NotBlank
    private String networkId;
    @NotBlank
    private String nativeName;
    @NotBlank
    private String securityGroupId;
    @NotBlank
    private String securityGroupName;
    @NotBlank
    private String userdataTemplateId;
    @NotNull
    private OpenStackUserInfo userInfo;
    private String domainName;
    private NetworkType networkType = NetworkType.DEFAULT;
    private Set<SecurityGroupType> securityGroupTypes = Sets.newHashSet(SecurityGroupType.PUBLIC);

    public OpenStackTenant() {
        super(PrivateCloudType.OPEN_STACK);
    }

    public String getNativeId() {
        return nativeId;
    }

    public void setNativeId(String nativeId) {
        this.nativeId = nativeId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public String getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public String getSecurityGroupName() {
        return securityGroupName;
    }

    public void setSecurityGroupName(String securityGroupName) {
        this.securityGroupName = securityGroupName;
    }

    public String getUserdataTemplateId() {
        return userdataTemplateId;
    }

    public void setUserdataTemplateId(String userdataTemplateId) {
        this.userdataTemplateId = userdataTemplateId;
    }

    public OpenStackUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(OpenStackUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public OpenStackTenant setNetworkType(NetworkType networkType) {
        this.networkType = networkType;
        return this;
    }

    public Set<SecurityGroupType> getSecurityGroupTypes() {
        return securityGroupTypes;
    }

    public OpenStackTenant setSecurityGroupTypes(Set<SecurityGroupType> securityGroupTypes) {
        this.securityGroupTypes = securityGroupTypes;
        return this;
    }

    @Override
    public String toString() {
        return "OpenStackTenant{" +
            "nativeId='" + nativeId + '\'' +
            ", networkId='" + networkId + '\'' +
            ", nativeName='" + nativeName + '\'' +
            ", securityGroupId='" + securityGroupId + '\'' +
            ", securityGroupName='" + securityGroupName + '\'' +
            ", userdataTemplateId='" + userdataTemplateId + '\'' +
            ", userInfo=" + userInfo +
            '}';
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}
