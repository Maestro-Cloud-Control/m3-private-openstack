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

public class ManageDnsNamesPolicy {

    private boolean unregisterDnsRecordsAllowed;
    private boolean registerDnsRecordsAllowed;

    public ManageDnsNamesPolicy() {
    }

    public ManageDnsNamesPolicy(boolean unregisterDnsRecordsAllowed, boolean registerDnsRecordsAllowed) {
        this.unregisterDnsRecordsAllowed = unregisterDnsRecordsAllowed;
        this.registerDnsRecordsAllowed = registerDnsRecordsAllowed;
    }

    public boolean isUnregisterDnsRecordsAllowed() {
        return unregisterDnsRecordsAllowed;
    }

    public boolean isRegisterDnsRecordsAllowed() {
        return registerDnsRecordsAllowed;
    }

    public static ManageDnsNamesPolicyBuilder builder() {
        return new ManageDnsNamesPolicyBuilder();
    }

    public static class ManageDnsNamesPolicyBuilder {

        private ManageDnsNamesPolicy manageDnsNamesPolicy;
        private boolean unregisterDnsRecordsAllowed;
        private boolean registerDnsRecordsAllowed;

        public ManageDnsNamesPolicyBuilder unregisterDnsRecordsAllowed(boolean unregisterDnsRecordsAllowed) {
            this.unregisterDnsRecordsAllowed = unregisterDnsRecordsAllowed;
            return this;
        }

        public ManageDnsNamesPolicyBuilder registerDnsRecordsAllowed(boolean registerDnsRecordsAllowed) {
            this.registerDnsRecordsAllowed = registerDnsRecordsAllowed;
            return this;
        }

        public ManageDnsNamesPolicy build() {
            manageDnsNamesPolicy = new ManageDnsNamesPolicy(unregisterDnsRecordsAllowed, registerDnsRecordsAllowed);

            return manageDnsNamesPolicy;
        }
    }
}
