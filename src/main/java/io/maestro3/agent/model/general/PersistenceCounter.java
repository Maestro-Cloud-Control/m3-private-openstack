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

package io.maestro3.agent.model.general;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.maestro3.agent.model.enums.CounterType;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersistenceCounter {

    @Id
    private String id;
    @NotNull
    private CounterType counterType;
    @NotBlank
    private String counterBoundResourceId;
    @Range
    private double value;

    public String getId() {
        return id;
    }

    public CounterType getCounterType() {
        return counterType;
    }

    public void setCounterType(CounterType counterType) {
        this.counterType = counterType;
    }

    public String getCounterBoundResourceId() {
        return counterBoundResourceId;
    }

    public void setCounterBoundResourceId(String counterBoundResourceId) {
        this.counterBoundResourceId = counterBoundResourceId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
