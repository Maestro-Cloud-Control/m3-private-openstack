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

package io.maestro3.agent.openstack.api.compute;


import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.agent.openstack.api.compute.bean.VncConsole;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.model.compute.RebootType;
import io.maestro3.agent.model.compute.Server;

import java.util.Iterator;
import java.util.List;


public interface IServerService {

    /**
     * Gets all servers.
     *
     * @return servers or empty list if no servers found
     * @throws OSClientException Open Stack client exception
     */
    List<Server> list() throws OSClientException;

    Iterator<Server> listLimited(int limit) throws OSClientException;

    /**
     * Boots a new server with the specified boot info.
     *
     * @param bootInfo all the required information to boot server instance
     * @return newly created server
     * @throws OSClientException Open Stack client exception
     */
    Server boot(ServerBootInfo bootInfo) throws OSClientException;

    /**
     * Gets server by id.
     *
     * @param id id of a server
     * @return server or null if server was not found
     * @throws OSClientException Open Stack client exception
     */
    Server get(String id) throws OSClientException;

    /**
     * Deletes (terminates) the server.
     *
     * @param id server id
     * @throws OSClientException Open Stack client exception
     */
    void delete(String id) throws OSClientException;

    /**
     * Stops the server.
     *
     * @param serverId server id
     * @throws OSClientException Open Stack client exception
     */
    void stop(String serverId) throws OSClientException;

    /**
     * Changes the shape.
     *
     * @param serverId server id
     * @throws OSClientException Open Stack client exception
     */
    void resize(String serverId, String flavor) throws OSClientException;

    /**
     * Confirms change the shape.
     *
     * @param serverId server id
     * @throws OSClientException Open Stack client exception
     */
    void confirmResize(String serverId) throws OSClientException;

    /**
     * Reboots the server.
     *
     * @param serverId server id
     * @param type     reboot type
     * @throws OSClientException Open Stack client exception
     */
    void reboot(String serverId, RebootType type) throws OSClientException;

    boolean rebuild(String serverId, String machineImageId) throws OSClientException;


    /**
     * Suspends the server.
     *
     * @param serverId server id
     * @throws OSClientException Open Stack client exception
     */
    void suspend(String serverId) throws OSClientException;

    /**
     * Starts the server.
     *
     * @param serverId server id
     * @throws OSClientException Open Stack client exception
     */
    void start(String serverId) throws OSClientException;

    /**
     * Resumes the server from the suspended state.
     *
     * @param serverId server id
     * @throws OSClientException Open Stack client exception
     */
    void resume(String serverId) throws OSClientException;

    VncConsole getVncConsole(String serverId) throws OSClientException;

    void attachVolume(String serverId, String volumeId) throws OSClientException;

    void attachVolume(String serverId, String volumeId, String device) throws OSClientException;

    void detachVolume(String serverId, String volumeId) throws OSClientException;

    String readAdminPassword(String serverId) throws OSClientException;

    void addSecurityGroup(String serverId, String securityGroupId) throws OSClientException;

    void removeSecurityGroup(String serverId, String securityGroupId) throws OSClientException;
}
