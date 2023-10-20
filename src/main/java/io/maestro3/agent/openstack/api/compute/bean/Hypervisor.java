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

package io.maestro3.agent.openstack.api.compute.bean;

import com.google.gson.annotations.SerializedName;


public class Hypervisor {
    @SerializedName("hypervisor_hostname")
    private String hostName;
    @SerializedName("host_ip")
    private String hostIp;
    @SerializedName("status")
    private String status;
    @SerializedName("state")
    private String state;
    @SerializedName("vcpus")
    private int cpu;
    @SerializedName("vcpus_used")
    private int usedCpu;
    @SerializedName("local_gb")
    private int localGb;
    @SerializedName("local_gb_used")
    private int usedLocalGb;
    @SerializedName("memory_mb")
    private int memoryMb;
    @SerializedName("memory_mb_used")
    private int usedMemoryMb;
    @SerializedName("running_vms")
    private int runningInstancesCount;
    @SerializedName("free_disk_gb")
    private int freeDiskGb;
    @SerializedName("disk_available_least")
    private int diskAvailableLeast;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getUsedCpu() {
        return usedCpu;
    }

    public void setUsedCpu(int usedCpu) {
        this.usedCpu = usedCpu;
    }

    public int getLocalGb() {
        return localGb;
    }

    public void setLocalGb(int localGb) {
        this.localGb = localGb;
    }

    public int getUsedLocalGb() {
        return usedLocalGb;
    }

    public void setUsedLocalGb(int usedLocalGb) {
        this.usedLocalGb = usedLocalGb;
    }

    public int getMemoryMb() {
        return memoryMb;
    }

    public void setMemoryMb(int memoryMb) {
        this.memoryMb = memoryMb;
    }

    public int getUsedMemoryMb() {
        return usedMemoryMb;
    }

    public void setUsedMemoryMb(int usedMemoryMb) {
        this.usedMemoryMb = usedMemoryMb;
    }

    public int getRunningInstancesCount() {
        return runningInstancesCount;
    }

    public void setRunningInstancesCount(int runningInstancesCount) {
        this.runningInstancesCount = runningInstancesCount;
    }

    public int getFreeDiskGb() {
        return freeDiskGb;
    }

    public void setFreeDiskGb(int freeDiskGb) {
        this.freeDiskGb = freeDiskGb;
    }

    public int getDiskAvailableLeast() {
        return diskAvailableLeast;
    }

    public void setDiskAvailableLeast(int diskAvailableLeast) {
        this.diskAvailableLeast = diskAvailableLeast;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Hypervisor{" + "hostName='" + hostName + '\'' +
                ", hostIp='" + hostIp + '\'' +
                ", status='" + status + '\'' +
                ", state='" + state + '\'' +
                ", cpu=" + cpu +
                ", usedCpu=" + usedCpu +
                ", localGb=" + localGb +
                ", usedLocalGb=" + usedLocalGb +
                ", memoryMb=" + memoryMb +
                ", usedMemoryMb=" + usedMemoryMb +
                ", runningInstancesCount=" + runningInstancesCount +
                ", freeDiskGb=" + freeDiskGb +
                ", diskAvailableLeast=" + diskAvailableLeast +
                '}';
    }
}
