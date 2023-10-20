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

public class UpdateDnsNamesPolicy {

    private boolean deleteDnsNamesRightOff;
    private boolean makeDnsNamesAvailableRightOff;
    private int dnsLookupIntervalHours;

    public UpdateDnsNamesPolicy() {
    }

    public UpdateDnsNamesPolicy(boolean deleteDnsNamesRightOff, boolean makeDnsNamesAvailableRightOff, int dnsLookupIntervalHours) {
        this.deleteDnsNamesRightOff = deleteDnsNamesRightOff;
        this.makeDnsNamesAvailableRightOff = makeDnsNamesAvailableRightOff;
        this.dnsLookupIntervalHours = dnsLookupIntervalHours;
    }

    public boolean isDeleteDnsNamesRightOff() {
        return deleteDnsNamesRightOff;
    }

    public boolean isMakeDnsNamesAvailableRightOff() {
        return makeDnsNamesAvailableRightOff;
    }

    public int getDnsLookupIntervalHours() {
        return dnsLookupIntervalHours;
    }
}
