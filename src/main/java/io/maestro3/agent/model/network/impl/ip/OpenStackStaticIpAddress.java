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

package io.maestro3.agent.model.network.impl.ip;

import javax.validation.constraints.NotNull;


public abstract class OpenStackStaticIpAddress extends StaticIpAddress {

    @NotNull
    private IPState ipState;
    private String reservedBy;
    private String portId;
    private boolean fixed;
    private String fixedIp;

    public IPState getIpState() {
        return ipState;
    }

    public void setIpState(IPState ipState) {
        this.ipState = ipState;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public void setReservedBy(String reservedBy) {
        this.reservedBy = reservedBy;
    }


    public String getPortId() {
        return portId;
    }


    public void setPortId(String portId) {
        this.portId = portId;
    }


    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String getFixedIp() {
        return fixedIp;
    }

    public void setFixedIp(String fixedIp) {
        this.fixedIp = fixedIp;
    }

    public abstract String getUniqueNotEmptyId();
}
