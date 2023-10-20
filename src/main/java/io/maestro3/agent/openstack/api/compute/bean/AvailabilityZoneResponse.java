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

import java.util.Map;


public class AvailabilityZoneResponse {
    private String zoneName;
    private ZoneState zoneState;
    private Map<String, Map<String, HostServiceInfo>> hosts;

    public Map<String, Map<String, HostServiceInfo>> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, Map<String, HostServiceInfo>> hosts) {
        this.hosts = hosts;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public ZoneState getZoneState() {
        return zoneState;
    }

    public void setZoneState(ZoneState zoneState) {
        this.zoneState = zoneState;
    }

    public static final class ZoneState {
        private boolean available;

        public boolean isAvailable() {
            return available;
        }
    }
}
