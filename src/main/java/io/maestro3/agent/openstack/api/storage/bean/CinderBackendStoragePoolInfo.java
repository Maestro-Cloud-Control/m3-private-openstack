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

package io.maestro3.agent.openstack.api.storage.bean;


public class CinderBackendStoragePoolInfo {
    private String hostName;
    private int totalVolumes;
    private double totalCapacityGb;
    private double freeCapacityGb;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getTotalVolumes() {
        return totalVolumes;
    }

    public void setTotalVolumes(int totalVolumes) {
        this.totalVolumes = totalVolumes;
    }

    public double getTotalCapacityGb() {
        return totalCapacityGb;
    }

    public void setTotalCapacityGb(double totalCapacityGb) {
        this.totalCapacityGb = totalCapacityGb;
    }

    public double getFreeCapacityGb() {
        return freeCapacityGb;
    }

    public void setFreeCapacityGb(double freeCapacityGb) {
        this.freeCapacityGb = freeCapacityGb;
    }

    @Override
    public String toString() {
        return "CinderBackendStoragePoolInfo{" + "hostName='" + hostName + '\'' +
                ", totalVolumes=" + totalVolumes +
                ", totalCapacityGb=" + totalCapacityGb +
                ", freeCapacityGb=" + freeCapacityGb +
                '}';
    }
}
