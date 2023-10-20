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

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.compute.ActionResponse;
import io.maestro3.agent.model.compute.RebootType;
import io.maestro3.agent.model.compute.Server;
import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.IServerService;
import io.maestro3.agent.openstack.api.compute.bean.NovaServer;
import io.maestro3.agent.openstack.api.compute.bean.ServerBootInfo;
import io.maestro3.agent.openstack.api.compute.bean.VncConsole;
import io.maestro3.agent.openstack.api.compute.bean.action.ServerAction;
import io.maestro3.agent.openstack.api.compute.bean.action.ServerActions;
import io.maestro3.agent.openstack.api.support.LimitedIterator;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.client.IOSClientOption;
import io.maestro3.agent.openstack.client.OSClientOption;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.exception.OverLimitException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.response.IOSResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public abstract class BaseServerService extends BasicComputeService implements IServerService {

    private static final IOSClientOption WITH_NULL_SERIALIZER = OSClientOption.builder().withNullSerializer().build();

    public BaseServerService(IOSClient client) {
        super(client);
    }

    @Override
    public List<Server> list() throws OSClientException {
        BasicOSRequest<Servers> list = builder(Servers.class, endpoint())
            .path("/servers/detail")
            .create();
        Servers wrapper = client.execute(list).getEntity();
        List<NovaServer> servers = (wrapper == null) ? null : wrapper.serversList;
        return (servers != null) ? new ArrayList<>(servers) : new ArrayList<>();
    }

    @Override
    public Iterator<Server> listLimited(int limit) {
        return new LimitedServersIterator(limit);
    }

    @Override
    public Server boot(ServerBootInfo server) throws OSClientException {
        Assert.notNull(server, "server cannot be null.");

        Object bootServerRequest = getBootServerRequest(server);
        BasicOSRequest<ServerWrapper> boot = builder(ServerWrapper.class, endpoint())
            .path("/servers")
            .post(new ServerBootInfoWrapper(bootServerRequest))
            .create();

        ServerWrapper response;
        try {
            response = client.execute(boot).getEntity();
        } catch (OSClientException e) {
            return processBootFailure(e);
        }
        return response == null ? null : response.server;
    }

    @Override
    public Server get(String id) throws OSClientException {
        Assert.hasText(id, "id cannot be null or empty.");
        BasicOSRequest<ServerWrapper> getServer = builder(ServerWrapper.class, endpoint())
            .path("/servers/%s", id)
            .create();

        ServerWrapper wrapper = client.execute(getServer).getEntity();
        return wrapper == null ? null : wrapper.server;
    }

    @Override
    public void delete(String id) throws OSClientException {
        Assert.hasText(id);
        BasicOSRequest<Void> delete = builder(Void.class, endpoint())
            .delete()
            .path("/servers/%s", id)
            .create();
        client.execute(delete);
    }

    @Override
    public void attachVolume(String serverId, String volumeId) throws OSClientException {
        attachVolume(serverId, volumeId, null);
    }

    @Override
    public void attachVolume(String serverId, String volumeId, String device) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        Assert.hasText(volumeId, "volumeId cannot be null or empty.");

        BasicOSRequest<String> request = builder(String.class, endpoint())
            .path("/servers/%s/os-volume_attachments", serverId)
            .post(volumeAttachment(volumeId, device))
            .create();
        client.execute(request);
    }

    @Override
    public void detachVolume(String serverId, String volumeId) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        Assert.hasText(volumeId, "volumeId cannot be null or empty.");

        BasicOSRequest<String> request = builder(String.class, endpoint())
            .path("/servers/%s/os-volume_attachments/%s", serverId, volumeId)
            .delete()
            .create();
        client.execute(request);
    }

    @Override
    public String readAdminPassword(String serverId) throws OSClientException {
        BasicOSRequest<Password> request = builder(Password.class, endpoint())
            .path("/servers/%s/os-server-password", serverId)
            .create();
        Password password = client.execute(request).getEntity();
        return password != null ? password.passwordValue : null;
    }

    @Override
    public void addSecurityGroup(String serverId, String securityGroupId) throws OSClientException {
        Assert.hasText(serverId, "server id can not be null or empty");
        Assert.hasText(securityGroupId, "security group id can not be null or empty");
        invokeAction(serverId, ServerActions.addSecurityGroup(securityGroupId));
    }

    @Override
    public void removeSecurityGroup(String serverId, String securityGroupId) throws OSClientException {
        Assert.hasText(serverId, "server id can not be null or empty");
        Assert.hasText(securityGroupId, "security group id can not be null or empty");
        invokeAction(serverId, ServerActions.removeSecurityGroup(securityGroupId));
    }

    @Override
    public void stop(String serverId) throws OSClientException {
        invokeAction(serverId, ServerActions.stop());
    }

    @Override
    public void suspend(String serverId) throws OSClientException {
        invokeAction(serverId, ServerActions.suspend());
    }

    @Override
    public void start(String serverId) throws OSClientException {
        invokeAction(serverId, ServerActions.start());
    }

    @Override
    public void resume(String serverId) throws OSClientException {
        invokeAction(serverId, ServerActions.resume());
    }

    @Override
    public void resize(String serverId, String flavor) throws OSClientException {
        invokeAction(serverId, ServerActions.resize(flavor));
    }

    @Override
    public void confirmResize(String serverId) throws OSClientException {
        invokeActionAndGetResponse(serverId, ServerActions.confirmResize(), Void.class);
    }

    @Override
    public void reboot(String serverId, RebootType type) throws OSClientException {
        Assert.notNull(type);
        invokeAction(serverId, ServerActions.reboot(type));
    }

    @Override
    public boolean rebuild(String serverId, String machineImageId) throws OSClientException {
        Assert.hasText(serverId, "server id can not be null or empty");
        Assert.hasText(machineImageId, "machine image id can not be null or empty");
        return invokeAction(serverId, ServerActions.rebuildServer(machineImageId)).isFailed();
    }

    @Override
    public VncConsole getVncConsole(String serverId) throws OSClientException {
        VncConsoleWrapper vncConsoleWrapper = invokeActionAndGetResponse(serverId, ServerActions.vncConsole(), VncConsoleWrapper.class);
        return vncConsoleWrapper != null ? vncConsoleWrapper.console : null;
    }

    protected abstract Object getBootServerRequest(ServerBootInfo bootInfo);

    private Server processBootFailure(OSClientException e) throws OSClientException {
        Integer httpCode = e.getHttpCode();
        if (httpCode != null && httpCode.equals(HttpStatus.SC_REQUEST_TOO_LONG)) {
            throw new OverLimitException(e.getMessage());
        } else {
            throw e;
        }
    }

    private ActionResponse invokeAction(String serverId, ServerAction action) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        BasicOSRequest<String> request = actionRequest(serverId, action, String.class);

        String error = client.execute(request, WITH_NULL_SERIALIZER).getEntity();
        if (StringUtils.isNotEmpty(error)) {
            return ActionResponse.failure(error);
        }
        return ActionResponse.success();
    }

    private <T> T invokeActionAndGetResponse(String serverId, ServerAction action, Class<T> clazz) throws OSClientException {
        Assert.hasText(serverId, "serverId cannot be null or empty.");
        BasicOSRequest<T> request = actionRequest(serverId, action, clazz);

        IOSResponse<T> response = client.execute(request, WITH_NULL_SERIALIZER);
        return (response != null) ? response.getEntity() : null;
    }

    private <T> BasicOSRequest<T> actionRequest(String serverId, ServerAction action, Class<T> responseType) throws OSClientException {
        return builder(responseType, endpoint()).
            path("/servers/%s/action", serverId).
            post(action).
            create();
    }

    /**
     * Iterator class allowing to iterate through all servers on the tenant.
     * Servers are retrieved page by page with the specified limit.
     * Each page is stored inside iterator and following executions of next() method iterate through this page.
     * Once the whole page is inspected by executions of next() method, the new page is retrieved.<br/>
     * <p>
     * Allows to limit the amount of data retrieved at once.
     */
    private class LimitedServersIterator extends LimitedIterator<Server> {
        private LimitedServersIterator(int limit) {
            super(limit);
        }

        @Override
        protected String getBasePath() {
            return "/servers/detail";
        }

        @Override
        protected List<Server> retrieveResourcesPage(String path) throws OSClientException {
            String fullPath = path + "&sort_dir=asc";
            BasicOSRequest<Servers> request = builder(Servers.class, endpoint())
                .path(fullPath)
                .create();

            IOSResponse<Servers> response = client.execute(request);
            Servers wrapper = response.getEntity();
            List<NovaServer> novaServers = (wrapper == null) ? null : wrapper.serversList;
            if (CollectionUtils.isEmpty(novaServers)) {
                return null;
            }

            if (CollectionUtils.isNotEmpty(novaServers)) {
                return new ArrayList<>(novaServers);
            }
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static class ServerBootInfoWrapper {
        @SerializedName("server")
        private Object serverBootInfo;

        private ServerBootInfoWrapper(Object serverBootInfo) {
            this.serverBootInfo = serverBootInfo;
        }
    }

    @SuppressWarnings("unused")
    private static class ServerWrapper {
        private NovaServer server;
    }

    private static class Servers {
        @SerializedName("servers")
        List<NovaServer> serversList;
    }

    private VolumeAttachmentWrapper volumeAttachment(String volumeId, String device) {
        return new VolumeAttachmentWrapper(new VolumeAttachment(volumeId, device));
    }

    @SuppressWarnings("unused")
    private static class VolumeAttachmentWrapper {
        private final VolumeAttachment volumeAttachment;

        private VolumeAttachmentWrapper(VolumeAttachment volumeAttachment) {
            this.volumeAttachment = volumeAttachment;
        }
    }

    @SuppressWarnings("unused")
    private static class VolumeAttachment {
        private final String volumeId;
        private final String device;

        private VolumeAttachment(String volumeId, String device) {
            this.volumeId = volumeId;
            this.device = device;
        }
    }

    @SuppressWarnings("unused")
    private static class VncConsoleWrapper {
        private VncConsole console;
    }

    @SuppressWarnings("unused")
    private static class Password {
        @SerializedName("password")
        private String passwordValue;
    }
}
