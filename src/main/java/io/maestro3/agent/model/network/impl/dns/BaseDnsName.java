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

package io.maestro3.agent.model.network.impl.dns;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;


public abstract class BaseDnsName {

    @Id
    private String id;
    @NotBlank
    private String projectId;
    @NotBlank
    private String projectName;
    @NotBlank
    private String zoneId;
    @NotBlank
    private String zoneName;
    @NotBlank
    private String instanceName;
    private String instanceId;
    @NotNull
    private Date creationDate;
    @NotNull
    private DnsNameStatus status;

    @NotBlank
    private String server;
    @NotBlank
    private String name;
    @NotBlank
    private String ipAddress;

    private Date lastDnsLookupDate;

    private Date lastDnsRefreshDate;

    /**
     * Time to live of the resource record (RR).  This field is a 32
     * bit integer in units of seconds, and is primarily used by
     * resolvers when they cache RRs.  The TTL describes how
     * long a RR can be cached before it should be discarded (<a href="https://www.ietf.org/rfc/rfc1034.txt">specification</a>).
     */
    @Min(0)
    private int ttl;

    private boolean forbidDescribe;

    private boolean createdBeyondOrchestrator;

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public DnsNameStatus getStatus() {
        return status;
    }

    public void setStatus(DnsNameStatus status) {
        this.status = status;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isForbidDescribe() {
        return forbidDescribe;
    }

    public void setForbidDescribe(boolean forbidDescribe) {
        this.forbidDescribe = forbidDescribe;
    }

    public boolean isCreatedBeyondOrchestrator() {
        return createdBeyondOrchestrator;
    }

    public void setCreatedBeyondOrchestrator(boolean createdBeyondOrchestrator) {
        this.createdBeyondOrchestrator = createdBeyondOrchestrator;
    }

    public Date getLastDnsLookupDate() {
        return lastDnsLookupDate;
    }

    public void setLastDnsLookupDate(Date lastDnsLookupDate) {
        this.lastDnsLookupDate = lastDnsLookupDate;
    }

    public Date getLastDnsRefreshDate() {
        return lastDnsRefreshDate;
    }

    public void setLastDnsRefreshDate(Date lastDnsRefreshDate) {
        this.lastDnsRefreshDate = lastDnsRefreshDate;
    }

    abstract public DnsRecordType getRecordType();
}
