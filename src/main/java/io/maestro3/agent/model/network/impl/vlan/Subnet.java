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

package io.maestro3.agent.model.network.impl.vlan;

import io.maestro3.agent.model.network.impl.ip.AddressPool;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;


public class Subnet {
    @Valid
    private AddressPool addressPool;
    @NotBlank
    private String cidr;
    private String gateway;
    private String usedBy;

    public AddressPool getAddressPool() {
        return addressPool;
    }

    public void setAddressPool(AddressPool addressPool) {
        this.addressPool = addressPool;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public String getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(String usedBy) {
        this.usedBy = usedBy;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
}
