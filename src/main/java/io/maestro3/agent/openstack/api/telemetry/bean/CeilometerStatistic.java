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

package io.maestro3.agent.openstack.api.telemetry.bean;

import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.telemetry.Statistic;

import java.util.Date;


public class CeilometerStatistic implements Statistic {
    private int count;
    private double avg;
    private double min;
    private double max;
    private double sum;
    private String unit;
    @SerializedName("period_start")
    private Date date;

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getAvg() {
        return avg;
    }

    @Override
    public double getMin() {
        return min;
    }

    @Override
    public double getMax() {
        return max;
    }

    @Override
    public double getSum() {
        return sum;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public Date getDate() {
        return date;
    }
}
