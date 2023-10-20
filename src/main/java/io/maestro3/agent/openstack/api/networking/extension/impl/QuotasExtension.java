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

import io.maestro3.agent.openstack.api.networking.bean.NetworkingQuota;
import io.maestro3.agent.openstack.api.networking.bean.UpdateNetworkingQuotaRequest;
import io.maestro3.agent.openstack.api.networking.extension.IQuotasExtension;
import io.maestro3.agent.openstack.api.networking.impl.BasicNetworkingService;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.springframework.util.Assert;


public class QuotasExtension extends BasicNetworkingService implements IQuotasExtension {

    public QuotasExtension(IOSClient client) {
        super(client);
    }

    @Override
    public NetworkingQuota get(String projectId) throws OSClientException {
        Assert.hasText(projectId, "projectId cannot be null or empty.");

        BasicOSRequest<NetworkingQuotaWrapper> getQuota = BasicOSRequest.builder(NetworkingQuotaWrapper.class, endpoint())
                .path("/quotas/%s", projectId)
                .create();
        NetworkingQuotaWrapper wrapper = client.execute(getQuota).getEntity();
        return (wrapper != null) ? wrapper.quota : null;
    }

    @Override
    public NetworkingQuota update(String projectId, UpdateNetworkingQuotaRequest request) throws OSClientException {
        Assert.hasText(projectId, "projectId cannot be null or empty.");
        Assert.notNull(request, "request cannot be null or empty.");

        BasicOSRequest<NetworkingQuotaWrapper> getQuota = BasicOSRequest.builder(NetworkingQuotaWrapper.class, endpoint())
                .path("/quotas/%s", projectId)
                .put(new UpdateNetworkingQuotaWrapper(request))
                .create();
        NetworkingQuotaWrapper wrapper = client.execute(getQuota).getEntity();
        return (wrapper != null) ? wrapper.quota : null;
    }

    private static class NetworkingQuotaWrapper {
        private NetworkingQuota quota;
    }

    private static class UpdateNetworkingQuotaWrapper {
        private final UpdateNetworkingQuotaRequest quota;

        private UpdateNetworkingQuotaWrapper(UpdateNetworkingQuotaRequest quota) {
            this.quota = quota;
        }
    }
}
