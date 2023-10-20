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

package io.maestro3.agent.model.notification;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class InstanceEvent extends Event {

    @SerializedName("instance_id")
    private String instanceId;

    @SerializedName("instance_type")
    private String flavorName; // Name of the instance type ('flavor') of this instance

    @SerializedName("instance_type_id")
    private String flavorNovaId; // Nova ID for instance type ('flavor') of this instance

    @SerializedName("display_name")
    private String instanceDisplayName; // User selected display name for instance.

    @SerializedName("launched_at")
    private Date launchDate; // when this instance was last launched by hypervisor

    @SerializedName("image_ref_url")
    private String imageUrl; // Image URL (from Glance) that this instance was created from

    @SerializedName("state")
    private String state; // Current state of instance (such as 'active' or 'deleted')

    @SerializedName("state_description")
    private String stateDescription;

    @SerializedName("fixed_ips")
    private List<Ip> fixedIps; // list of ip addresses assigned to instance

    @SerializedName("memory_mb")
    private String memoryMb; // memory allocation for this instance

    @SerializedName("disk_gb")
    private String diskGb; // disk allocation for this instance

    public static class Ip {

        @SerializedName("floating_ips")
        private List<Map<String, Object>> floatingIps;

        private Map<String, Object> meta;

        private String type; // saw an example value "fixed"

        private int version; // saw an example value 4

        private String address; // saw an example value "10.0.0.9"

        private String label; // saw an example value "public"

    }

}
