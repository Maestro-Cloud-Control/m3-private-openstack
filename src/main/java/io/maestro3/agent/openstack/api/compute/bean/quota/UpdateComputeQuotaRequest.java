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

package io.maestro3.agent.openstack.api.compute.bean.quota;


public class UpdateComputeQuotaRequest {

    private Integer cores;
    private Integer ram;
    private Integer instances;

    private UpdateComputeQuotaRequest() {
    }

    public static Builder build() {
        return new Builder();
    }

    public Integer getCores() {
        return cores;
    }

    public Integer getRam() {
        return ram;
    }

    public Integer getInstances() {
        return instances;
    }

    public static class Builder {
        private UpdateComputeQuotaRequest request;

        private Builder() {
            request = new UpdateComputeQuotaRequest();
        }

        public Builder cores(Integer cores) {
            request.cores = cores;
            return this;
        }

        public Builder ram(Integer ram) {
            request.ram = ram;
            return this;
        }

        public Builder instances(Integer instances) {
            request.instances = instances;
            return this;
        }

        public Builder unlimited() {
            request.instances = -1;
            request.cores = -1;
            request.ram = -1;
            return this;
        }

        public UpdateComputeQuotaRequest get() {
            return request;
        }
    }
}
