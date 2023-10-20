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
import io.maestro3.agent.model.common.KeyPair;


public class NovaKeyPair implements KeyPair {

    private String fingerprint;
    private String name;
    @SerializedName("public_key")
    private String publicKey;
    @SerializedName("user_id")
    private String userId;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "NovaKeyPair{" +
                "fingerprint='" + fingerprint + '\'' +
                ", name='" + name + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
