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

package io.maestro3.agent.util;

import com.google.common.collect.Multimap;
import io.maestro3.agent.model.base.VLAN;
import io.maestro3.agent.model.network.impl.vlan.OpenStackVLAN;
import io.maestro3.sdk.internal.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpenStackNetworkUtils {

    private static final IConversionFunction<VLAN, String> CONVERSION_FUNCTION_NETWORK_ID =
        input -> input instanceof OpenStackVLAN ? ((OpenStackVLAN) input).getOpenStackNetworkId() : null;

    private static final String ROUTER_SUFFIX = "-router";

    public static List<String> filterNonDmzVLANSubnets(List<VLAN> dmzVLANs, Collection<String> ourSubnetNetworkIds) {
        if (CollectionUtils.isEmpty(ourSubnetNetworkIds)) return Collections.emptyList();

        Multimap<String, VLAN> vlanMap = ConversionUtils.convertToMultimap(dmzVLANs, CONVERSION_FUNCTION_NETWORK_ID);
        return ourSubnetNetworkIds.stream()
            .filter(networkId -> isNonDmzVLANSubnet(vlanMap, networkId))
            .collect(Collectors.toList());
    }

    public static List<String> filterNonDmzVLANAvailableInProject(
        List<VLAN> VLANs, Collection<String> ourSubnetNetworkIds, String projectId) {
        if (CollectionUtils.isEmpty(ourSubnetNetworkIds)) return Collections.emptyList();
        List<String> nonDmzVLANNetworkIds = filterNonDmzVLANSubnets(VLANs, ourSubnetNetworkIds);
        if (CollectionUtils.isEmpty(nonDmzVLANNetworkIds)) return Collections.emptyList();

        return filterVLANSubnetsAvailableInProject(VLANs, nonDmzVLANNetworkIds, projectId);
    }

    public static List<String> filterVLANSubnetsAvailableInProject(
        List<VLAN> VLANs, Collection<String> ourSubnetNetworkIds, String projectId) {
        if (CollectionUtils.isEmpty(ourSubnetNetworkIds)) return Collections.emptyList();
        Multimap<String, VLAN> vlanMap = ConversionUtils.convertToMultimap(VLANs, CONVERSION_FUNCTION_NETWORK_ID);

        return ourSubnetNetworkIds.stream()
            .filter(networkId -> isVLANAvailableForProject(projectId, vlanMap, networkId))
            .collect(Collectors.toList());
    }

    private static boolean isNonDmzVLANSubnet(Multimap<String, VLAN> vlanMap, String networkId) {
        if (networkId == null) return false;
        Collection<VLAN> vlans = vlanMap.get(networkId);
        return CollectionUtils.isNotEmpty(vlans) && vlans.stream().noneMatch(VLAN::isDmz);
    }

    private static boolean isVLANAvailableForProject(String projectId, Multimap<String, VLAN> vlanMap, String networkId) {
        Collection<VLAN> vlans = vlanMap.get(networkId);
        if (CollectionUtils.isEmpty(vlans)) {
            return false;
        }

        return vlans.stream()
            .anyMatch(vlan -> vlan.getTenantId() == null || StringUtils.equalsIgnoreCase(vlan.getTenantId(), projectId));
    }

    public static String generateRouterName(String networkName) {
        Assert.hasText(networkName, "networkName can not be null or empty");
        return networkName + ROUTER_SUFFIX;
    }

    public static String generateIsolationSecurityGroupName(String envPrefix) {
        String securityGroupName = "isolation";
        if (StringUtils.isBlank(envPrefix)) {
            return securityGroupName;
        }

        return securityGroupName
            .concat("-")
            .concat(envPrefix);
    }
}
