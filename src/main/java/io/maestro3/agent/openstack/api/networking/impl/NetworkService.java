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

package io.maestro3.agent.openstack.api.networking.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.networking.INetworkService;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.request.CreateNetwork;
import io.maestro3.agent.openstack.api.networking.request.UpdateNetwork;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.filter.impl.NetworkApiFilter;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


class NetworkService extends BasicNetworkingService implements INetworkService {

    NetworkService(IOSClient client) {
        super(client);
    }

    @Override
    public Network create(CreateNetwork configuration) throws OSClientException {
        IOSRequest<NetworkWrapper> create = BasicOSRequest
                .builder(NetworkWrapper.class, endpoint())
                .path("/networks")
                .post(configuration)
                .create();

        NetworkWrapper result = client.execute(create).getEntity();
        return result == null ? null : result.network;
    }

    @Override
    public Network update(String networkId, UpdateNetwork configuration) throws OSClientException {
        IOSRequest<NetworkWrapper> update = BasicOSRequest
                .builder(NetworkWrapper.class, endpoint())
                .path("/networks/%s", networkId)
                .put(configuration)
                .create();

        NetworkWrapper result = client.execute(update).getEntity();
        return result == null ? null : result.network;
    }

    @Override
    public Network get(String networkId) throws OSClientException {
        IOSRequest<NetworkWrapper> get = BasicOSRequest.
                builder(NetworkWrapper.class, endpoint())
                .path("/networks/%s", networkId)
                .create();

        NetworkWrapper result = client.execute(get).getEntity();
        return result == null ? null : result.network;
    }

    @Override
    public void delete(String networkId) throws OSClientException {
        IOSRequest<Void> delete = BasicOSRequest
                .builder(Void.class, endpoint())
                .path("/networks/%s", networkId)
                .delete()
                .create();

        client.execute(delete);
    }

    @Override
    public List<Network> listByName(String name) throws OSClientException {
        BasicOSRequest.BasicOSRequestBuilder<Networks> list = BasicOSRequest.builder(Networks.class, endpoint())
                .path("/networks?name=" + encode(name));
        Networks result = client.execute(list.create()).getEntity();
        return result == null ? null : result.networkList;
    }

    private String encode(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public List<Network> list() throws OSClientException {
        return list(null);
    }

    @Override
    public List<Network> list(NetworkApiFilter filter) throws OSClientException {
        String path = "/networks";
        if (filter != null) {
            path = filter.apply(path);
        }
        BasicOSRequest.BasicOSRequestBuilder<Networks> list = BasicOSRequest.builder(Networks.class, endpoint())
                .path(path);
        Networks result = client.execute(list.create()).getEntity();
        return result == null ? null : result.networkList;
    }

    //wrappers

    private static class NetworkWrapper {
        private Network network;
    }

    private static class Networks {
        @SerializedName("networks")
        private List<Network> networkList;
    }
}
