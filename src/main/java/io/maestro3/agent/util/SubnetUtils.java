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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.util.CollectionUtils;

import java.util.Set;

public class SubnetUtils {

    private static final Logger LOG = LogManager.getLogger(SubnetUtils.class);

    public static boolean isInSubnets(String ip, Set<String> subnets) {
        if (CollectionUtils.isEmpty(subnets)) {
            return false;
        }

        return subnets.stream().anyMatch(subnet -> isInSubnet(ip, subnet));
    }

    private static boolean isInSubnet(String ip, String subnet) {
        IpAddressMatcher subNetIpAddressMatcher;
        try {
            subNetIpAddressMatcher = new IpAddressMatcher(subnet);
        } catch (Exception e) {
            LOG.error("Failed to parse subnet: {}", subnet);
            return false;
        }

        try {
            return subNetIpAddressMatcher.matches(ip);
        } catch (Exception e) {
            LOG.error("Failed to parse ip address: {}", ip);
            return false;
        }
    }

}
