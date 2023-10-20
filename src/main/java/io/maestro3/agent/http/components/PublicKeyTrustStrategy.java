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

package io.maestro3.agent.http.components;

import org.apache.http.conn.ssl.TrustStrategy;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class PublicKeyTrustStrategy implements TrustStrategy {

    private final PublicKey publicKey;

    public PublicKeyTrustStrategy(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (X509Certificate certificate : chain) {
            try {
                certificate.verify(publicKey);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
