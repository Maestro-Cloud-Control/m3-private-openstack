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

package io.maestro3.agent.openstack.api.networking.extension.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.networking.bean.Router;
import io.maestro3.agent.openstack.api.networking.extension.IRouterExtension;
import io.maestro3.agent.openstack.api.networking.impl.BasicNetworkingService;
import io.maestro3.agent.openstack.api.networking.request.AddRouterInterface;
import io.maestro3.agent.openstack.api.networking.request.CreateRouter;
import io.maestro3.agent.openstack.api.networking.request.RemoveRouterInterface;
import io.maestro3.agent.openstack.api.networking.request.UpdateRouter;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class RouterExtension extends BasicNetworkingService implements IRouterExtension {

    public RouterExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<Router> list() throws OSClientException {
        BasicOSRequest<Routers> list = builder(Routers.class,
            endpoint()).
            path("/routers").
            create();

        Routers entity = client.execute(list).getEntity();
        return entity == null ? null : entity.routerList;
    }

    @Override
    public List<Router> listByName(String name) throws OSClientException {
        BasicOSRequest<Routers> list = builder(Routers.class,
            endpoint()).
            path("/routers?name=" + encode(name)).
            create();

        Routers entity = client.execute(list).getEntity();
        return entity == null ? null : entity.routerList;
    }

    @Override
    public Router create(CreateRouter configuration) throws OSClientException {
        IOSRequest<RouterWrapper> create = BasicOSRequest.
            builder(RouterWrapper.class, endpoint())
            .path("/routers")
            .post(configuration)
            .create();

        RouterWrapper result = client.execute(create).getEntity();
        return result == null ? null : result.router;
    }

    @Override
    public Router detail(String routerId) throws OSClientException {
        IOSRequest<RouterWrapper> getRouterDetailRequest = BasicOSRequest.builder(RouterWrapper.class, endpoint())
            .path("/routers/%s", routerId)
            .create();
        RouterWrapper result = client.execute(getRouterDetailRequest).getEntity();
        return result == null ? null : result.router;
    }

    @Override
    public void update(String routerId, UpdateRouter configuration) throws OSClientException {
        IOSRequest<Void> updateRouterRequest = BasicOSRequest.builder(Void.class, endpoint())
            .path("/routers/%s", routerId)
            .put(configuration)
            .create();
        client.execute(updateRouterRequest);
    }

    @Override
    public void delete(String routerId) throws OSClientException {
        IOSRequest<Void> deleteRouterRequest = BasicOSRequest.builder(Void.class, endpoint())
            .path("/routers/%s", routerId)
            .delete()
            .create();
        client.execute(deleteRouterRequest);
    }

    @Override
    public void addInterface(String routerId, AddRouterInterface configuration) throws OSClientException {
        IOSRequest<Void> addInterfaceRequest = BasicOSRequest.builder(Void.class, endpoint())
            .path("/routers/%s/add_router_interface", routerId)
            .put(configuration)
            .create();
        client.execute(addInterfaceRequest);
    }

    @Override
    public void removeInterface(String routerId, RemoveRouterInterface configuration) throws OSClientException {
        IOSRequest<Void> addInterfaceRequest = BasicOSRequest.builder(Void.class, endpoint())
            .path("/routers/%s/remove_router_interface", routerId)
            .put(configuration)
            .create();
        client.execute(addInterfaceRequest);
    }

    protected String encode(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static class Routers {
        @SerializedName("routers")
        private List<Router> routerList;
    }

    private static class RouterWrapper {
        private Router router;
    }
}
