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
import io.maestro3.agent.openstack.api.compute.extension.IMetadataExtension;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.response.IOSResponse;

import java.util.Map;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class MetadataExtension extends BasicComputeService implements IMetadataExtension {

    public MetadataExtension(IOSClient client) {
        super(client);
    }

    @Override
    public Map<String, String> list(String serverId) throws OSClientException {
        BasicOSRequest<Metadata> request = builder(Metadata.class, endpoint())
                .path("/servers/%s/metadata", serverId)
                .create();
        IOSResponse<Metadata> response = client.execute(request);
        return response.getEntity() != null ? response.getEntity().metadataMap : null;
    }

    @Override
    public void update(String serverId, Map<String, String> metadata) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/servers/%s/metadata", serverId)
                .post(new Metadata(metadata))
                .create();
        client.execute(request);
    }

    @Override
    public void upsert(String serverId, Map<String, String> metadata) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/servers/%s/metadata", serverId)
                .put(new Metadata(metadata))
                .create();
        client.execute(request);
    }

    @Override
    public void delete(String serverId, String metadataKey) throws OSClientException {
        BasicOSRequest<Void> request = builder(Void.class, endpoint())
                .path("/servers/%s/metadata/%s", serverId, metadataKey)
                .delete().create();
        client.execute(request);
    }

    private static class Metadata {
        @SerializedName("metadata")
        private final Map<String, String> metadataMap;

        private Metadata(Map<String, String> metadata) {
            this.metadataMap = metadata;
        }
    }
}
