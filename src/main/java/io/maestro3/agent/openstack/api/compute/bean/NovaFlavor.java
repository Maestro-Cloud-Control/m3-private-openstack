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

package io.maestro3.agent.openstack.api.compute.bean;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.compute.Flavor;
import org.apache.commons.lang3.StringUtils;



public class NovaFlavor implements Flavor {

    private String id;
    private String name;
    private Integer ram;
    private Integer vcpus;
    private Integer disk;
    @SerializedName("OS-FLV-EXT-DATA:ephemeral")
    private Integer ephemeral;
    private String swap;
    @SerializedName("rxtx_factor")
    private Float rxtxFactor;
    @SerializedName("OS-FLV-DISABLED:disabled")
    private Boolean disabled;
    @SerializedName("rxtx_quota")
    private Integer rxtxQuota;
    @SerializedName("rxtx_cap")
    private Integer rxtxCap;
    @SerializedName("os-flavor-access:is_public")
    private Boolean isPublic;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRam() {
        return ram == null ? 0 : ram;
    }

    public int getVcpus() {
        return vcpus == null ? 0 : vcpus;
    }

    public int getDisk() {
        return disk == null ? 0 : disk;
    }

    public int getEphemeral() {
        return ephemeral == null ? 0 : ephemeral;
    }

    public int getSwap() {
        try {
            return StringUtils.isEmpty(swap) ? 0 : Integer.parseInt(swap);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public float getRxtxFactor() {
        return rxtxFactor == null ? 1.0f : rxtxFactor;
    }

    public int getRxtxQuota() {
        return rxtxQuota == null ? 0 : rxtxQuota;
    }

    public int getRxtxCap() {
        return rxtxCap == null ? 0 : rxtxCap;
    }

    public boolean isPublic() {
        return isPublic == null ? Boolean.TRUE : isPublic;
    }

    @Override
    public boolean isDisabled() {
        return disabled == null ? Boolean.FALSE : disabled;
    }

    @Override
    public String toString() {
        return "NovaFlavor{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", ram=" + getRam() +
                ", vcpus=" + getVcpus() +
                ", disk=" + getDisk() +
                ", ephemeral=" + getEphemeral() +
                ", swap=" + getSwap() +
                ", rxtxFactor=" + getRxtxFactor() +
                ", disabled=" + isDisabled() +
                ", rxtxQuota=" + getRxtxQuota() +
                ", rxtxCap=" + getRxtxCap() +
                ", isPublic=" + isPublic() +
                '}';
    }
}
