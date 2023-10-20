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

package io.maestro3.agent.model.network.impl.ip.parameters;


public class DisassociateOpenStackStaticIpAddressParameters extends DisassociateStaticIpAddressParameters {
    private boolean createNewInstead;
    private boolean refreshInstanceAfter;

    private DisassociateOpenStackStaticIpAddressParameters(DisassociateOpenStackStaticIpAddressBuilder builder) {
        super(builder);
        this.createNewInstead = builder.createNewInstead;
        this.refreshInstanceAfter = builder.refreshInstanceAfter;
    }

    public static DisassociateOpenStackStaticIpAddressBuilder builder() {
        return new DisassociateOpenStackStaticIpAddressBuilder();
    }

    public boolean isCreateNewInstead() {
        return createNewInstead;
    }

    public boolean isRefreshInstanceAfter() {
        return refreshInstanceAfter;
    }

    public static class DisassociateOpenStackStaticIpAddressBuilder extends Builder {
        private boolean createNewInstead;
        private boolean refreshInstanceAfter;

        private DisassociateOpenStackStaticIpAddressBuilder() {
        }

        public DisassociateOpenStackStaticIpAddressBuilder createNewInstead(boolean createNewInstead) {
            this.createNewInstead = createNewInstead;
            return this;
        }

        public DisassociateOpenStackStaticIpAddressBuilder refreshInstanceAfter(boolean refreshInstanceAfter) {
            this.refreshInstanceAfter = refreshInstanceAfter;
            return this;
        }

        @Override
        public DisassociateOpenStackStaticIpAddressBuilder staticIp(String staticIp) {
            super.staticIp(staticIp);
            return this;
        }

        @Override
        public DisassociateOpenStackStaticIpAddressParameters build() {
            return new DisassociateOpenStackStaticIpAddressParameters(this);
        }
    }
}
