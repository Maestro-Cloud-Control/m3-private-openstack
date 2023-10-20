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

package io.maestro3.agent.model.compute;

import io.maestro3.agent.model.OpenStackResource;
import io.maestro3.agent.openstack.api.compute.bean.Fault;
import io.maestro3.agent.openstack.api.compute.bean.Personality;
import io.maestro3.agent.openstack.api.compute.bean.SecurityInfo;
import io.maestro3.agent.openstack.api.storage.bean.CinderVolume;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface Server extends OpenStackResource {

    /**
     * Gets server id.
     *
     * @return id of a server
     */
    String getId();

    /**
     * Gets server status.
     *
     * @return status
     */
    ServerStatus getStatus();

    /**
     * Gets power state.
     *
     * @return state
     */
    PowerState getPowerState();

    /**
     * Gets server IP addresses
     *
     * @return addresses
     */
    Addresses getAddresses();

    Set<String> getNetworkNames();

    /**
     * Gets server name
     *
     * @return server name
     */
    String getName();

    /**
     * Gets task currently performed with server.
     *
     * @return task state
     */
    TaskState getTaskState();

    /**
     * Gets id of a flavor.
     *
     * @return instance flavor.
     */
    String getFlavorId();

    /**
     * Gets id of an image.
     *
     * @return image.
     */
    String getImageId();

    /**
     * Gets date of creation.
     *
     * @return creation date
     */
    Date getCreated();

    /**
     * Get instance SSH key name
     *
     * @return name of the SSH key
     */
    String getKeyName();


    /**
     * Get instance metadata
     *
     * @return metadata
     */
    Map<String, String> getMetadata();

    /**
     * Gets a list of personality.
     *
     * @return list of personality.
     * @see Personality
     */
    List<Personality> getPersonality();

    String getTenantId();

    Fault getFault();

    String getHost();

    List<CinderVolume> getVolumes();

    String getAvailabilityZone();

    List<SecurityInfo> getSecurityGroups();

}
