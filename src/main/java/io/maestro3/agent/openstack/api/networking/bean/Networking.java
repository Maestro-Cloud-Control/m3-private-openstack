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

package io.maestro3.agent.openstack.api.networking.bean;


public class Networking {
    private Router router;
    private NovaSubnet subnet;
    private Network network;

    public Networking(Router router, NovaSubnet subnet, Network network) {
        this.router = router;
        this.subnet = subnet;
        this.network = network;
    }

    public Router getRouter() {
        return router;
    }

    public NovaSubnet getSubnet() {
        return subnet;
    }

    public Network getNetwork() {
        return network;
    }
}
