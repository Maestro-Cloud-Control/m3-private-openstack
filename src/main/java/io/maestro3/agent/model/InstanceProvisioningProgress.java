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

package io.maestro3.agent.model;

import io.maestro3.agent.model.network.impl.dns.DnsName;
import io.maestro3.agent.model.network.impl.ip.StaticIpAddress;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.model.tenant.OpenStackTenant;


public class InstanceProvisioningProgress {

    private final OpenStackTenant project;
    private final OpenStackRegionConfig zone;

    private String instanceName;
    private StaticIpAddress staticIpAddress;
    private DnsName dnsName;
    private String portId;
    // used only for predefined non static IP in Kubernetes servers
    private String predefinedIpAddress;

    public InstanceProvisioningProgress(OpenStackTenant project, OpenStackRegionConfig zone) {
        this.project = project;
        this.zone = zone;
    }

    public OpenStackRegionConfig getZone() {
        return zone;
    }

    public OpenStackTenant getProject() {
        return project;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public StaticIpAddress getStaticIpAddress() {
        return staticIpAddress;
    }

    public void setStaticIpAddress(StaticIpAddress staticIpAddress) {
        this.staticIpAddress = staticIpAddress;
    }

    public DnsName getDnsName() {
        return dnsName;
    }

    public void setDnsName(DnsName dnsName) {
        this.dnsName = dnsName;
    }

    public String getDnsNameString() {
        if (dnsName == null) {
            return null;
        }
        return dnsName.getName();
    }

    public String getIpAddressString() {
        if (staticIpAddress == null) {
            return null;
        }
        return staticIpAddress.getIpAddress();
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getPortId() {
        return portId;
    }

    public String getPredefinedIpAddress() {
        return predefinedIpAddress;
    }

    public void setPredefinedIpAddress(String predefinedIpAddress) {
        this.predefinedIpAddress = predefinedIpAddress;
    }
}
