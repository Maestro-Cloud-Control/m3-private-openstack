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

package io.maestro3.agent.openstack.api.compute.extension.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.bean.HostAggregate;
import io.maestro3.agent.openstack.api.compute.extension.IHostAggregatesExtension;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;

import java.util.List;
import java.util.Map;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class HostAggregatesExtension extends BasicComputeService implements IHostAggregatesExtension {

    public HostAggregatesExtension(IOSClient client) {
        super(client);
    }

    @Override
    public List<HostAggregate> list() throws OSClientException {
        BasicOSRequest<HostAggregatesHolder> request = builder(HostAggregatesHolder.class, endpoint())
                .path("/os-aggregates")
                .create();

        HostAggregatesHolder hostAggregatesHolder = client.execute(request).getEntity();
        return hostAggregatesHolder == null ? null : hostAggregatesHolder.aggregates;
    }

    @Override
    public HostAggregate get(String aggregateId) throws OSClientException {
        BasicOSRequest<HostAggregateHolder> request = builder(HostAggregateHolder.class, endpoint())
                .path("/os-aggregates/" + aggregateId)
                .create();

        HostAggregateHolder aggregateHolder = client.execute(request).getEntity();
        return aggregateHolder == null ? null : aggregateHolder.aggregate;
    }

    @Override
    public void updateMetadata(String aggregateId, Map<String, String> metadata) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/os-aggregates/" + aggregateId + "/action")
                .post(new SetMetadataHolder(metadata))
                .create();
        client.execute(request);
    }

    private static class HostAggregatesHolder {
        private List<HostAggregate> aggregates;
    }

    private static class HostAggregateHolder {
        private HostAggregate aggregate;
    }

    private static class SetMetadataHolder {
        @SerializedName("set_metadata")
        private final SetMetadata setMetadata;

        private SetMetadataHolder(Map<String, String> metadata) {
            this.setMetadata = new SetMetadata(metadata);
        }
    }

    private static class SetMetadata {
        private final Map<String, String> metadata;

        private SetMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }
    }
}
