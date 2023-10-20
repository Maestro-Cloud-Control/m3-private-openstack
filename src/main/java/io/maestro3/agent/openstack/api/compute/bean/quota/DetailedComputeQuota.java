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


public class DetailedComputeQuota implements IComputeQuota {

    private QuotaItem ram;
    private QuotaItem cores;
    private QuotaItem instances;
    @SerializedName("fixed_ips")
    private QuotaItem fixedIps;

    public QuotaItem getRam() {
        return ram;
    }

    public void setRam(QuotaItem ram) {
        this.ram = ram;
    }

    public QuotaItem getCores() {
        return cores;
    }

    public void setCores(QuotaItem cores) {
        this.cores = cores;
    }

    public QuotaItem getInstances() {
        return instances;
    }

    public void setInstances(QuotaItem instances) {
        this.instances = instances;
    }

    public QuotaItem getFixedIps() {
        return fixedIps;
    }

    public void setFixedIps(QuotaItem fixedIps) {
        this.fixedIps = fixedIps;
    }

    @Override
    public int getRamLimit() {
        if (ram != null) {
            return ram.getLimit();
        }
        return 0;
    }

    @Override
    public int getCoresLimit() {
        if (cores != null) {
            return cores.getLimit();
        }
        return 0;
    }

    @Override
    public int getInstancesLimit() {
        if (instances != null) {
            return instances.getLimit();
        }
        return 0;
    }

    @Override
    public int getFixedIpsLimit() {
        if (fixedIps != null) {
            return fixedIps.getLimit();
        }
        return 0;
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
