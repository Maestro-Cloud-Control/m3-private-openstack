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

package io.maestro3.agent.openstack.api.telemetry.bean;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.telemetry.Link;
import io.maestro3.agent.model.telemetry.Resource;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class CeilometerResource implements Resource {
    @SerializedName("resource_id")
    private String id;
    private String userId;
    private List<Link> links;
    private String source;
    @SerializedName("first_sample_timestamp")
    private Date firstSampleTimestamp;
    @SerializedName("last_sample_timestamp")
    private Date lastSampleTimestamp;
    @SerializedName("project_id")
    private String projectId;
    private Map<String, String> metadata;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public List<Link> getLinks() {
        return links;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public Date getFirstSampleTimestamp() {
        return firstSampleTimestamp;
    }

    @Override
    public Date getLastSampleTimestamp() {
        return lastSampleTimestamp;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
