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

package io.maestro3.agent.openstack.api.compute.impl.servers;

import io.maestro3.agent.model.compute.RebootType;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.openstack.api.IServiceDelegate;
import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.IServerService;
import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.agent.openstack.api.compute.bean.VncConsole;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class ServerServiceDelegate extends BasicComputeService implements IServerService, IServiceDelegate<IServerService> {
    private static final String NOVA_API_VERSION_21 = "v2.1";

    private final AtomicReference<io.maestro3.agent.openstack.api.compute.impl.servers.v2.ServerService> v2Service = new AtomicReference<>();
    private final AtomicReference<io.maestro3.agent.openstack.api.compute.impl.servers.v21.ServerService> v3Service = new AtomicReference<>();

    public ServerServiceDelegate(IOSClient client) {
        super(client);
    }

    @Override
    public List<Server> list() throws OSClientException {
        return delegate(getVersion()).list();
    }

    @Override
    public Iterator<Server> listLimited(int limit) throws OSClientException {
        return delegate(getVersion()).listLimited(limit);
    }

    @Override
    public Server boot(ServerBootInfo bootInfo) throws OSClientException {
        return delegate(getVersion()).boot(bootInfo);
    }

    @Override
    public Server get(String id) throws OSClientException {
        return delegate(getVersion()).get(id);
    }

    @Override
    public void delete(String id) throws OSClientException {
        delegate(getVersion()).delete(id);
    }

    @Override
    public void stop(String serverId) throws OSClientException {
        delegate(getVersion()).stop(serverId);
    }

    @Override
    public void reboot(String serverId, RebootType rebootType) throws OSClientException {
        delegate(getVersion()).reboot(serverId, rebootType);
    }

    @Override
    public boolean rebuild(String serverId, String machineImageId) throws OSClientException {
        return delegate(getVersion()).rebuild(serverId, machineImageId);
    }

    @Override
    public void suspend(String serverId) throws OSClientException {
        delegate(getVersion()).suspend(serverId);
    }

    @Override
    public void resize(String serverId, String flavor) throws OSClientException {
        delegate(getVersion()).resize(serverId, flavor);
    }

    @Override
    public void confirmResize(String serverId) throws OSClientException {
        delegate(getVersion()).confirmResize(serverId);
    }

    @Override
    public void start(String serverId) throws OSClientException {
        delegate(getVersion()).start(serverId);
    }

    @Override
    public void resume(String serverId) throws OSClientException {
        delegate(getVersion()).resume(serverId);
    }

    @Override
    public VncConsole getVncConsole(String serverId) throws OSClientException {
        return delegate(getVersion()).getVncConsole(serverId);
    }

    @Override
    public void attachVolume(String serverId, String volumeId) throws OSClientException {
        delegate(getVersion()).attachVolume(serverId, volumeId);
    }

    @Override
    public void attachVolume(String serverId, String volumeId, String device) throws OSClientException {
        delegate(getVersion()).attachVolume(serverId, volumeId, device);
    }

    @Override
    public void addSecurityGroup(String serverId, String securityGroupId) throws OSClientException {
        delegate(getVersion()).addSecurityGroup(serverId, securityGroupId);

    }

    @Override
    public void removeSecurityGroup(String serverId, String securityGroupId) throws OSClientException {
        delegate(getVersion()).removeSecurityGroup(serverId, securityGroupId);

    }

    @Override
    public void detachVolume(String serverId, String volumeId) throws OSClientException {
        delegate(getVersion()).detachVolume(serverId, volumeId);
    }

    @Override
    public String readAdminPassword(String serverId) throws OSClientException {
        return delegate(getVersion()).readAdminPassword(serverId);
    }

    @Override
    public IServerService delegate(String version) {
        if (StringUtils.isNotBlank(version) && version.contains(NOVA_API_VERSION_21)) {
            io.maestro3.agent.openstack.api.compute.impl.servers.v21.ServerService localV3 = v3Service.get();
            if (localV3 == null) {
                synchronized (this) {
                    localV3 = v3Service.get();
                    if (localV3 == null) {
                        localV3 = new io.maestro3.agent.openstack.api.compute.impl.servers.v21.ServerService(client);
                        v3Service.set(localV3);
                    }
                }
            }
            return localV3;
        }
        io.maestro3.agent.openstack.api.compute.impl.servers.v2.ServerService localV2 = v2Service.get();
        if (localV2 == null) {
            synchronized (this) {
                localV2 = v2Service.get();
                if (localV2 == null) {
                    localV2 = new io.maestro3.agent.openstack.api.compute.impl.servers.v2.ServerService(client);
                    v2Service.set(localV2);
                }
            }
        }
        return localV2;
    }
}
