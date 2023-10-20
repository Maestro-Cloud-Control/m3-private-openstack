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

package io.maestro3.agent.openstack.api.networking.bean;

import org.springframework.util.Assert;


public enum IpVersion {
    V4(4),
    V6(6);

    private int version;

    IpVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public static IpVersion encode(int version) {
        IpVersion result = null;
        for (IpVersion ver : values()) {
            if (ver.getVersion() == version) {
                result = ver;
                break;
            }
        }
        Assert.notNull(result, "Unknown IP version " + version);
        return result;
    }
}
