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
import io.maestro3.agent.openstack.api.networking.ISubnetService;
import io.maestro3.agent.openstack.api.networking.bean.NovaSubnet;
import io.maestro3.agent.openstack.api.networking.request.CreateSubnet;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.filter.impl.SubnetApiFilter;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.request.IOSRequest;

import java.util.List;


class SubnetService extends BasicNetworkingService implements ISubnetService {

    SubnetService(IOSClient client) {
        super(client);
    }

    @Override
    public List<NovaSubnet> list(SubnetApiFilter filter) throws OSClientException {
        String path = "/subnets";
        if (filter != null) {
            path = filter.apply(path);
        }
        IOSRequest<Subnets> list = BasicOSRequest.
                builder(Subnets.class, endpoint())
                .path(path)
                .create();

        Subnets result = client.execute(list).getEntity();
        return result == null ? null : result.novaSubnets;
    }

    @Override
    public List<NovaSubnet> list() throws OSClientException {
        return list(null);
    }

    @Override
    public NovaSubnet create(CreateSubnet configuration) throws OSClientException {
        IOSRequest<SubnetWrapper> list = BasicOSRequest.
                builder(SubnetWrapper.class, endpoint())
                .path("/subnets").post(configuration)
                .create();

        SubnetWrapper result = client.execute(list).getEntity();
        return result == null ? null : result.subnet;
    }

    @Override
    public void delete(String subnetId) throws OSClientException {
        IOSRequest<Void> delete = BasicOSRequest.
                builder(Void.class, endpoint())
                .path("/subnets/%s", subnetId)
                .delete()
                .create();

        client.execute(delete);
    }

    private static class Subnets {
        @SerializedName("subnets")
        private List<NovaSubnet> novaSubnets;
    }

    private static class SubnetWrapper {
        private NovaSubnet subnet;
    }
}
