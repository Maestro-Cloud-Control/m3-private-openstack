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

package io.maestro3.agent.openstack.api.compute.bean;

import io.maestro3.agent.model.compute.Address;
import io.maestro3.agent.model.compute.Addresses;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


class NovaAddresses implements Addresses {

    private List<Address> privateAddresses = new ArrayList<>();
    private List<Address> publicAddresses = new ArrayList<>();

    @Override
    public List<Address> getPublicAddresses() {
        return publicAddresses;
    }

    @Override
    public List<Address> getPrivateAddresses() {
        return privateAddresses;
    }

    public static NovaAddresses wrap(Map<String, List<NovaAddress>> addressesMap) {
        NovaAddresses addresses = new NovaAddresses();
        addresses.loadAddressees(addressesMap);
        return addresses;
    }

    private void loadAddressees(Map<String, List<NovaAddress>> addresseesMap) {
        Collection<List<NovaAddress>> values = addresseesMap.values();
        if (CollectionUtils.isNotEmpty(values)) {
            for (List<NovaAddress> addresses : values) {
                if (CollectionUtils.isNotEmpty(addresses)) {
                    for (NovaAddress a : addresses) {
                        switch (a.getType()) {
                            case FLOATING:
                                publicAddresses.add(a);
                                break;
                            case FIXED:
                                privateAddresses.add(a);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "NovaAddresses{" +
                "privateAddresses=" + privateAddresses +
                ", publicAddresses=" + publicAddresses +
                '}';
    }
}
