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

package io.maestro3.agent.model.region;

import io.maestro3.agent.amqp.model.SimpleRabbitConfiguration;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;


public class OpenStackRegionNotificationConfig extends SimpleRabbitConfiguration {

    @Range // positive by default
    private int replyTimeoutMillis;
    @Range // positive by default
    private int shutdownTimeoutMillis;

    @Range(min = 1, max = 30) // thread number - better not be too large
    private int minConcurrentConsumers;
    @Range(min = 1, max = 30) // thread number - better not be too large
    private int maxConcurrentConsumers;

    private String novaExchangeName;
    private String cinderExchangeName;
    private String glanceExchangeName;

    public int getReplyTimeoutMillis() {
        return replyTimeoutMillis;
    }

    public void setReplyTimeoutMillis(int replyTimeoutMillis) {
        this.replyTimeoutMillis = replyTimeoutMillis;
    }

    public int getShutdownTimeoutMillis() {
        return shutdownTimeoutMillis;
    }

    public void setShutdownTimeoutMillis(int shutdownTimeoutMillis) {
        this.shutdownTimeoutMillis = shutdownTimeoutMillis;
    }

    public int getMinConcurrentConsumers() {
        return minConcurrentConsumers;
    }

    public void setMinConcurrentConsumers(int minConcurrentConsumers) {
        this.minConcurrentConsumers = minConcurrentConsumers;
    }

    public int getMaxConcurrentConsumers() {
        return maxConcurrentConsumers;
    }

    public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
        this.maxConcurrentConsumers = maxConcurrentConsumers;
    }

    public String getNovaExchangeName() {
        return novaExchangeName;
    }

    public void setNovaExchangeName(String novaExchangeName) {
        this.novaExchangeName = novaExchangeName;
    }

    public String getCinderExchangeName() {
        return cinderExchangeName;
    }

    public void setCinderExchangeName(String cinderExchangeName) {
        this.cinderExchangeName = cinderExchangeName;
    }

    public String getGlanceExchangeName() {
        return glanceExchangeName;
    }

    public void setGlanceExchangeName(String glanceExchangeName) {
        this.glanceExchangeName = glanceExchangeName;
    }
}
