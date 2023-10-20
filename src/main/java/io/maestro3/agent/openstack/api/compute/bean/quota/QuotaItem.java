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


public class QuotaItem {

    @SerializedName("reserved")
    private int reserved;
    @SerializedName("limit")
    private int limit;
    @SerializedName("in_use")
    private int inUse;

    public int getReserved() {
        return reserved;
    }

    public int getLimit() {
        return limit;
    }

    public int getInUse() {
        return inUse;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QuotaItem{");
        sb.append("reserved=").append(reserved);
        sb.append(", limit=").append(limit);
        sb.append(", inUse=").append(inUse);
        sb.append('}');
        return sb.toString();
    }
}
