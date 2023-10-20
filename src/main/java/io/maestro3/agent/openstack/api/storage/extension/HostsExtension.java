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

package io.maestro3.agent.openstack.api.storage.extension;

import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.storage.bean.CinderHost;
import io.maestro3.agent.openstack.api.storage.bean.CinderHostDetails;
import io.maestro3.agent.openstack.api.storage.request.CinderServiceType;
import io.maestro3.agent.openstack.api.storage.request.ListHostsRequest;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HostsExtension extends BasicService implements IHostsExtension {

    public HostsExtension(IOSClient client) {
        super(ServiceType.VOLUME, client);
    }

    @Override
    public List<CinderHost> list(ListHostsRequest request) throws OSClientException {
        BasicOSRequest<CinderHostsHolder> listHosts = BasicOSRequest.builder(CinderHostsHolder.class, endpoint())
                .path("/os-hosts")
                .create();

        CinderHostsHolder hostsHolder = client.execute(listHosts).getEntity();
        if (hostsHolder == null || CollectionUtils.isEmpty(hostsHolder.hosts)) {
            return Collections.emptyList();
        }
        return pickHosts(hostsHolder.hosts, request);
    }

    @Override
    public CinderHostDetails details(String tenantId, String hostName) throws OSClientException {
        Assert.hasText(tenantId, "projectId cannot be null or empty.");
        Assert.hasText(hostName, "hostName cannot be null or empty.");

        BasicOSRequest<CinderHostDetailsHolder> listHostDetails = BasicOSRequest.builder(CinderHostDetailsHolder.class, endpoint())
                .path("/os-hosts/" + hostName)
                .create();
        CinderHostDetailsHolder hostDetailsHolder = client.execute(listHostDetails).getEntity();
        if (hostDetailsHolder == null) {
            return null;
        }

        List<CinderHostDetailsResourceHolder> resources = hostDetailsHolder.host;
        if (CollectionUtils.isEmpty(resources)) {
            return null;
        }

        for (CinderHostDetailsResourceHolder resource : resources) {
            CinderHostDetails hostDetails = resource.resource;
            if (tenantId.equals(hostDetails.getProject())) {
                return hostDetails;
            }
        }
        return null;
    }

    private List<CinderHost> pickHosts(List<CinderHost> hosts, ListHostsRequest request) {
        if (CollectionUtils.isEmpty(request.getTypes())) {
            return hosts;
        }
        List<String> serviceTypes = getListOfStringServiceTypes(request.getTypes());
        List<CinderHost> targetHosts = new ArrayList<>();
        for (CinderHost host : hosts) {
            if (serviceTypes.contains(host.getService())) {
                targetHosts.add(host);
            }
        }
        return targetHosts;
    }

    private List<String> getListOfStringServiceTypes(Collection<CinderServiceType> types) {
        if (CollectionUtils.isEmpty(types)) {
            return Collections.emptyList();
        }
        Set<String> serviceTypes = new HashSet<>();
        for (CinderServiceType cinderServiceType : types) {
            serviceTypes.add(cinderServiceType.getName());
        }
        return new ArrayList<>(serviceTypes);
    }

    private static final class CinderHostsHolder {
        private List<CinderHost> hosts;
    }

    private static final class CinderHostDetailsHolder {
        private List<CinderHostDetailsResourceHolder> host;
    }

    private static final class CinderHostDetailsResourceHolder {
        private CinderHostDetails resource;
    }
}
