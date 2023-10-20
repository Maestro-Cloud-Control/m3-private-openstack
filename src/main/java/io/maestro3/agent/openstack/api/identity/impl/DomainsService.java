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

package io.maestro3.agent.openstack.api.identity.impl;

import io.maestro3.agent.openstack.api.BasicService;
import io.maestro3.agent.openstack.api.ServiceType;
import io.maestro3.agent.openstack.api.identity.IDomainService;
import io.maestro3.agent.openstack.api.identity.bean.Domain;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;

import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


class DomainsService extends BasicService implements IDomainService {

    DomainsService(IOSClient client) {
        super(ServiceType.IDENTITY, client);
    }

    @Override
    public List<Domain> list() throws OSClientException {
        BasicOSRequest<DomainsWrapper> listDomains = builder(DomainsWrapper.class, endpoint())
                .path("/domains")
                .create();
        DomainsWrapper wrapper = client.execute(listDomains).getEntity();
        return wrapper != null ? wrapper.domains : null;
    }

    @Override
    public Domain inspect(String name) throws OSClientException {
        BasicOSRequest<DomainWrapper> inspectDomain = builder(DomainWrapper.class, endpoint())
                .path("/domains/%s", name)
                .create();
        DomainWrapper wrapper = client.execute(inspectDomain).getEntity();
        return wrapper != null ? wrapper.domain : null;
    }

    @Override
    public void delete(String name) throws OSClientException {
        BasicOSRequest<Void> listDomains = builder(Void.class, endpoint())
                .path("/domains/%s", name)
                .delete()
                .create();
        client.execute(listDomains);
    }

    @Override
    public void disable(String name) throws OSClientException {
        BasicOSRequest<Void> listDomains = builder(Void.class, endpoint())
                .path("/domains/%s", name)
                .patch(new DisableDomainWrapper())
                .create();
        client.execute(listDomains);
    }

    private static class DisableDomainWrapper {
        private DisableDomain domain = new DisableDomain();
    }

    private static class DisableDomain {
        private boolean enabled;
    }

    private static class DomainWrapper {
        private Domain domain;
    }

    private static class DomainsWrapper {
        private List<Domain> domains;
    }
}
