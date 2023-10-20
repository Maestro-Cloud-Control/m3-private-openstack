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

package io.maestro3.agent.openstack.api.compute.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.compute.IQuotasExtension;
import io.maestro3.agent.openstack.api.compute.bean.quota.ComputeQuota;
import io.maestro3.agent.openstack.api.compute.bean.quota.DetailedComputeQuota;
import io.maestro3.agent.openstack.api.compute.bean.quota.IComputeQuota;
import io.maestro3.agent.openstack.api.compute.bean.quota.UpdateComputeQuotaRequest;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.springframework.util.Assert;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


class QuotasExtension extends BasicService implements IQuotasExtension {

    QuotasExtension(IOSClient client) {
        super(ServiceType.COMPUTE, client);
    }

    @Override
    public IComputeQuota getDetailed(String projectId) throws OSClientException {
        Assert.hasText(projectId, "projectId cannot be null or empty.");

        BasicOSRequest<DetailedProjectQuotaSet> getProjectQuotaRequest = builder(DetailedProjectQuotaSet.class, endpoint())
                .path("/os-quota-sets/%s/detail", projectId)
                .create();
        DetailedProjectQuotaSet quotaSet = client.execute(getProjectQuotaRequest).getEntity();
        return (quotaSet == null) ? null : quotaSet.projectQuota;
    }

    @Override
    public IComputeQuota get(String projectId) throws OSClientException {
        Assert.hasText(projectId, "projectId cannot be null or empty.");

        BasicOSRequest<ProjectQuotaSet> getProjectQuotaRequest = builder(ProjectQuotaSet.class, endpoint())
                .path("/os-quota-sets/%s", projectId)
                .create();
        ProjectQuotaSet quotaSet = client.execute(getProjectQuotaRequest).getEntity();
        return (quotaSet == null) ? null : quotaSet.computeQuota;
    }

    @Override
    public IComputeQuota update(String projectId, UpdateComputeQuotaRequest request) throws OSClientException {
        Assert.notNull(request, "request cannot be null.");

        BasicOSRequest<ProjectQuotaSet> setProjectQuotaRequest = builder(ProjectQuotaSet.class, endpoint())
                .path("/os-quota-sets/%s", projectId)
                .put(new UpdateProjectQuotaRequestWrapper(request))
                .create();
        ProjectQuotaSet quotaSet = client.execute(setProjectQuotaRequest).getEntity();
        return (quotaSet == null) ? null : quotaSet.computeQuota;
    }

    private static class DetailedProjectQuotaSet {
        @SerializedName("quota_set")
        private DetailedComputeQuota projectQuota;
    }

    private static class ProjectQuotaSet {
        @SerializedName("quota_set")
        private ComputeQuota computeQuota;
    }

    private static class UpdateProjectQuotaRequestWrapper {
        @SerializedName("quota_set")
        private final UpdateComputeQuotaRequest request;

        private UpdateProjectQuotaRequestWrapper(UpdateComputeQuotaRequest request) {
            this.request = request;
        }
    }
}
