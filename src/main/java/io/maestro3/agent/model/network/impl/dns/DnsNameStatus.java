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


public enum DnsNameStatus {

    /**
     * When DNS record just created and we wait until it is available on DNS lookup.
     */
    REGISTERING,

    /**
     * When DNS lookup returns IP address different from the one we registered DNS name for,
     * so we attempt to re-register it pointing to fresh IP address.
     */
    RE_REGISTERING,

    /**
     * When DNS lookup returns IP address the DNS name was created for.
     */
    AVAILABLE,

    /**
     * When DNS state already was AVAILABLE but now DNS lookup returns nothing or returns errors.
     */
    UNAVAILABLE,

    /**
     * When IP address was disassociated from instance, DNS name is temporarily removed.
     * This DNS name will exist if instance exists. Once instance is terminated, DNS name is terminated too.
     * The purpose of persisting this document is to recreate DNS name again if instance obtains new IP address.
     */
    TEMPORARILY_DELETED,

    /**
     * When, for example, DNS name was created by unknown user and Orchestrator tries to delete it.
     * Of course, Orchestrator will get error, because "I gave birth to you, I'll kill you".
     */
    REFUSED_TO_DELETE,

    /**
     * In cases of failed RE_REGISTERING or TEMPORARILY_DELETED actions, when instances still available
     * and IP changed or temporary unavailable.
     */
    REFUSED_TO_DELETE_FROM_ALIVE_RESOURCE,

    /**
     * When deleting DNS record and wait until it becomes not available.
     */
    DELETING;

    public boolean ne(DnsNameStatus status) {
        return this != status;
    }

    public boolean is(DnsNameStatus status) {
        return this == status;
    }

    public boolean in(DnsNameStatus... statuses) {
        for (DnsNameStatus status : statuses) {
            if (this == status) {
                return true;
            }
        }
        return false;
    }
}
