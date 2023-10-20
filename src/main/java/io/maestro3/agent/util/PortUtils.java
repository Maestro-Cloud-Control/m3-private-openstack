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

import io.maestro3.agent.openstack.api.networking.bean.FixedIp;
import io.maestro3.agent.openstack.api.networking.bean.Port;
import io.maestro3.sdk.internal.util.CollectionUtils;

import java.util.List;
import java.util.UUID;


public final class PortUtils {

    public static final String MANUALLY_CREATED_PORT_PREFIX = "manually-created-";
    public static final String EO_PORT_PREFIX = "eo-port-";

    public static String generateManuallyCreatedPortName() {
        return MANUALLY_CREATED_PORT_PREFIX + UUID.randomUUID();
    }

    public static String generateStaticIpPortName() {
        return EO_PORT_PREFIX + UUID.randomUUID();
    }

    public static String getIpAddress(Port port) {
        if (port == null) {
            return null;
        }
        List<FixedIp> fixedIps = port.getFixedIps();
        if (CollectionUtils.isEmpty(fixedIps)) {
            return null;
        }
        return fixedIps.get(0).getIpAddress();
    }
}
