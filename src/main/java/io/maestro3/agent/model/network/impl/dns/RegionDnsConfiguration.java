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

import io.maestro3.sdk.internal.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

public class RegionDnsConfiguration {

    private static final int DEFAULT_RR_CACHE_TTL = 600;
    private static final String DEFAULT_ZONE = "cloud.com";

    private static final boolean DEFAULT_DELETE_DNS_NAMES_RIGHT_OFF = true;
    private static final boolean DEFAULT_MAKE_DNS_NAMES_AVAILABLE_RIGHT_OFF = true;
    private static final int DEFAULT_DNS_LOOKUP_INTERVAL_HOURS = 4;
    private static final boolean DELETE_DNS_RECORDS_AVAILABLE = true;
    private static final boolean REGISTER_DNS_RECORDS_AVAILABLE = true;

    private String zone;
    private List<String> servers;
    private Set<String> restrictedDnsNames;
    private int ttl;

    private UpdateDnsNamesPolicy updateDnsNamesPolicy;
    private ManageDnsNamesPolicy manageDnsNamesPolicy;
    private int dnsPriority;

    public RegionDnsConfiguration() {
        ttl = DEFAULT_RR_CACHE_TTL;
        zone = DEFAULT_ZONE;
        updateDnsNamesPolicy = new UpdateDnsNamesPolicy(
            DEFAULT_DELETE_DNS_NAMES_RIGHT_OFF,
            DEFAULT_MAKE_DNS_NAMES_AVAILABLE_RIGHT_OFF,
            DEFAULT_DNS_LOOKUP_INTERVAL_HOURS
        );
        manageDnsNamesPolicy = new ManageDnsNamesPolicy(
            DELETE_DNS_RECORDS_AVAILABLE,
            REGISTER_DNS_RECORDS_AVAILABLE
        );
    }

    public String getZone() {
        if (StringUtils.isNotBlank(zone)) {
            return zone;
        }
        return DEFAULT_ZONE;
    }

    public List<String> getServers() {
        return servers;
    }

    public int getTtl() {
        return ttl;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public UpdateDnsNamesPolicy getUpdateDnsNamesPolicy() {
        return updateDnsNamesPolicy;
    }

    public ManageDnsNamesPolicy getManageDnsNamesPolicy() {
        return manageDnsNamesPolicy;
    }

    public void setManageDnsNamesPolicy(ManageDnsNamesPolicy manageDnsNamesPolicy) {
        this.manageDnsNamesPolicy = manageDnsNamesPolicy;
    }

    public int getDnsPriority() {
        return dnsPriority;
    }

    public void setDnsPriority(int dnsPriority) {
        this.dnsPriority = dnsPriority;
    }

    public String[] getServersArray() {
        if (CollectionUtils.isEmpty(servers)) {
            return null;
        }
        return servers.toArray(new String[servers.size()]);
    }

    public int getNotZeroTtl() {
        if (ttl == 0) {
            return DEFAULT_RR_CACHE_TTL;
        }
        return ttl;
    }

    public Set<String> getRestrictedDnsNames() {
        return restrictedDnsNames;
    }
}
