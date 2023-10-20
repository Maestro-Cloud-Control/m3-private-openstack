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

import com.google.gson.annotations.SerializedName;


public class ComputeQuota implements IComputeQuota {

    private int ram;
    private int cores;
    private int instances;
    @SerializedName("fixed_ips")
    private int fixedIps;

    @Override
    public int getRamLimit() {
        return ram;
    }

    @Override
    public int getCoresLimit() {
        return cores;
    }

    @Override
    public int getInstancesLimit() {
        return instances;
    }

    @Override
    public int getFixedIpsLimit() {
        return fixedIps;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectQuota{");
        sb.append("ram=").append(ram);
        sb.append(", cores=").append(cores);
        sb.append(", instances=").append(instances);
        sb.append(", fixedIps=").append(fixedIps);
        sb.append('}');
        return sb.toString();
    }
}
