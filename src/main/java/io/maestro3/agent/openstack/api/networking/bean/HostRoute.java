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


public class HostRoute {

    private String destination;
    private String nexthop;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getNexthop() {
        return nexthop;
    }

    public void setNexthop(String nexthop) {
        this.nexthop = nexthop;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HostRoute{");
        sb.append("destination='").append(destination).append('\'');
        sb.append(", nexthop='").append(nexthop).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
