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

package io.maestro3.agent.model.general;

import io.maestro3.agent.model.base.IPrivateInstance;
import io.maestro3.agent.model.base.ResourceWthTags;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;


public abstract class ServerConfig extends ResourceWthTags implements IPrivateInstance {

    @Id
    private String id;
    @NotBlank
    private String nameAlias;
    @NotBlank
    private String regionId;
    @NotBlank
    private String tenantId;
    private long startTime;
    private boolean instanceRunSuccess;

    public boolean isInstanceRunSuccess() {
        return instanceRunSuccess;
    }

    public void setInstanceRunSuccess(boolean instanceRunSuccess) {
        this.instanceRunSuccess = instanceRunSuccess;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getId() {
        return id;
    }

    public String getNameAlias() {
        return nameAlias;
    }

    public void setNameAlias(String nameAlias) {
        this.nameAlias = nameAlias;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
