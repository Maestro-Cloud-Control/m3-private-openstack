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

import io.maestro3.agent.model.network.CustomSecurityConfig;
import io.maestro3.agent.model.network.Direction;
import io.maestro3.agent.model.network.SecurityConfigType;
import io.maestro3.agent.model.network.SecurityGroupType;

import java.util.Objects;


public class SecurityGroupExtension extends CustomSecurityConfig {

    private String ipRange;
    private String zoneId;
    private SecurityGroupType securityGroupType;
    private Direction direction;
    private String protocol;
    private String description;
    private Integer fromPort;
    private Integer toPort;

    public SecurityGroupExtension() {
        super(SecurityConfigType.OPEN_STACK);
    }

    public String getIpRange() {
        return ipRange;
    }

    public void setIpRange(String ipRange) {
        this.ipRange = ipRange;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public SecurityGroupType getSecurityGroupType() {
        return securityGroupType;
    }

    public void setSecurityGroupType(SecurityGroupType securityGroupType) {
        this.securityGroupType = securityGroupType;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDescription() {
        return description;
    }

    public SecurityGroupExtension setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getFromPort() {
        return fromPort;
    }

    public void setFromPort(Integer fromPort) {
        this.fromPort = fromPort;
    }

    public Integer getToPort() {
        return toPort;
    }

    public void setToPort(Integer toPort) {
        this.toPort = toPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityGroupExtension that = (SecurityGroupExtension) o;
        if (!Objects.equals(getProtocol(), that.getProtocol())) return false;
        if (!Objects.equals(fromPort, that.fromPort)) return false;
        if (!Objects.equals(toPort, that.toPort)) return false;
        if (!Objects.equals(ipRange, that.ipRange)) return false;
        if (!Objects.equals(zoneId, that.zoneId)) return false;
        if (securityGroupType != that.securityGroupType) return false;
        if (direction != that.direction) return false;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = getProtocol() != null ? getProtocol().hashCode() : 0;
        result = 31 * result + (ipRange != null ? ipRange.hashCode() : 0);
        result = 31 * result + (zoneId != null ? zoneId.hashCode() : 0);
        result = 31 * result + (securityGroupType != null ? securityGroupType.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (fromPort != null ? fromPort.hashCode() : 0);
        result = 31 * result + (toPort != null ? toPort.hashCode() : 0);
        return result;
    }
}
