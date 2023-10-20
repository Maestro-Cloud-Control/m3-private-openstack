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

package io.maestro3.agent.model.network.impl;

import com.google.common.base.Objects;
import io.maestro3.agent.util.OsIpUtils;


public class ProjectSource {
    /**
     * Rule description should looks like '$PROJECT. $ZONE. $RESOURCE_ID $RESOURCE_TYPE_DESCRIPTION'
     */
    private static final String DESCRIPTION_TEMPLATE = "%s. %s. %s %s";

    private String projectCode;
    private String zoneName;
    private String resourceId;
    private String address;
    private ResourceType resourceType;

    private ProjectSource(String projectCode, String zoneName, String resourceId, String address, ResourceType resourceType) {
        this.projectCode = projectCode;
        this.zoneName = zoneName;
        this.resourceId = resourceId;
        this.address = OsIpUtils.convertSingleIpAddr(address);
        this.resourceType = resourceType;
    }

    public static ProjectSource instanceResource(String projectCode, String zoneName, String resourceId, String address) {
        return new ProjectSource(projectCode, zoneName, resourceId, address, ResourceType.INSTANCE_IP);
    }

    public static ProjectSource staticIPResource(String projectCode, String zoneName, String resourceId, String address) {
        return new ProjectSource(projectCode, zoneName, resourceId, address, ResourceType.STATIC_IP);
    }

    public static ProjectSource vlanResource(String projectCode, String zoneName, String resourceId, String address) {
        return new ProjectSource(projectCode, zoneName, resourceId, address, ResourceType.VLAN);
    }

    public static ProjectSource sdnExternalGatewayResource(String projectCode, String zoneName, String resourceId, String address) {
        return new ProjectSource(projectCode, zoneName, resourceId, address, ResourceType.SDN_EXTERNAL_GATEWAY);
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return String.format(DESCRIPTION_TEMPLATE, projectCode, zoneName, resourceId, resourceType.getDescription());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectSource that = (ProjectSource) o;
        return Objects.equal(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(address);
    }

    private enum ResourceType {
        INSTANCE_IP("instance IP"),
        STATIC_IP("static IP address"),
        VLAN("vlan CIDR"),
        SDN_EXTERNAL_GATEWAY("SDN external gateway");

        private String description;

        ResourceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
