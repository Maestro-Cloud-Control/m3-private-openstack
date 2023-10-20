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

package io.maestro3.agent.openstack.api.compute.extension.impl;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.common.KeyPair;
import io.maestro3.agent.openstack.api.compute.BasicComputeService;
import io.maestro3.agent.openstack.api.compute.bean.NovaKeyPair;
import io.maestro3.agent.openstack.api.compute.extension.IKeyPairExtension;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import io.maestro3.agent.openstack.transport.response.IOSResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class KeyPairExtension extends BasicComputeService implements IKeyPairExtension {

    public KeyPairExtension(IOSClient client) {
        super(client);
    }

    @Override
    public KeyPair importKeyPair(String name, String publicKey) throws OSClientException {
        BasicOSRequest<KeyPairWrapper> importKeyPair = builder(KeyPairWrapper.class,
                endpoint())
                .path("/os-keypairs")
                .post(new ImportKeyPair(name, publicKey))
                .create();

        KeyPairWrapper entity = client.execute(importKeyPair).getEntity();
        return entity == null ? null : entity.keypair;
    }

    @Override
    public List<NovaKeyPair> list() throws OSClientException {
        BasicOSRequest<Keypairs> list = builder(Keypairs.class, endpoint()).path("/os-keypairs").create();
        Keypairs entity = client.execute(list).getEntity();
        List<KeyPairWrapper> keyPairs = (entity == null) ? null : entity.keypairList;
        return (keyPairs != null) ? unwrap(keyPairs) : Collections.<NovaKeyPair>emptyList();
    }

    @Override
    public void deleteKeyPair(String name) throws OSClientException {
        BasicOSRequest<KeyPairWrapper> deleteKeyPair = builder(KeyPairWrapper.class, endpoint())
                .path("/os-keypairs/%s", name).delete()
                .create();
        client.execute(deleteKeyPair);
    }

    @Override
    public KeyPair inspect(String name) throws OSClientException {
        BasicOSRequest<KeyPairWrapper> deleteKeyPair = builder(KeyPairWrapper.class, endpoint())
                .path("/os-keypairs/%s", name)
                .create();
        IOSResponse<KeyPairWrapper> response = client.execute(deleteKeyPair);
        return (response.getEntity() != null) ? response.getEntity().keypair : null;
    }

    private List<NovaKeyPair> unwrap(List<KeyPairWrapper> keyPairs) {
        List<NovaKeyPair> result = new ArrayList<>();
        for (KeyPairWrapper keyPair : keyPairs) {
            result.add(keyPair.keypair);
        }
        return result;
    }

    private static class KeyPairWrapper {
        NovaKeyPair keypair;
    }

    private static class Keypairs {
        @SerializedName("keypairs")
        List<KeyPairWrapper> keypairList;
    }

    private static class ImportKeyPair {
        KeyPair keypair;

        ImportKeyPair(String name, String publicKey) {
            keypair = new KeyPair(name, publicKey);
        }

        static class KeyPair {
            String name;
            @SerializedName("public_key")
            String publicKey;

            KeyPair(String name, String publicKey) {
                this.name = name;
                this.publicKey = publicKey;
            }
        }
    }
}
