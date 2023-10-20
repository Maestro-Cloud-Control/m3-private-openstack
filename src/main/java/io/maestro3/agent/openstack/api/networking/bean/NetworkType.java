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


public enum NetworkType {

    /**
     * Local network.
     */
    LOCAL,
    /**
     * In a flat network, everyone shares the same network segment.
     */
    FLAT,
    /**
     * In a VLAN network, tenants (projects) are separated because each is assigned to a VLAN.
     */
    VLAN,

    /**
     * GRE segmenation provides separation among tenants, and also allows overlapping subnets and IP ranges.
     * It does this by encapsulating tenant traffic in tunnels.
     */
    GRE
}
