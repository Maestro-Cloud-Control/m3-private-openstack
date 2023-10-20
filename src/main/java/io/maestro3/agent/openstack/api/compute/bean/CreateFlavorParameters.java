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


public class CreateFlavorParameters {

    private String id;
    private String name;
    private int ram;
    private int disk;
    private int vcpus;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CreateFlavorParameters{" + "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ram=" + ram +
                ", disk=" + disk +
                ", vcpus=" + vcpus +
                '}';
    }

    private CreateFlavorParameters() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private int ramMb;
        private int diskGb;
        private int cpu;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withRamMb(int ramMb) {
            this.ramMb = ramMb;
            return this;
        }

        public Builder withDiskGb(int diskGb) {
            this.diskGb = diskGb;
            return this;
        }

        public Builder withCpu(int cpu) {
            this.cpu = cpu;
            return this;
        }

        public CreateFlavorParameters get() {
            CreateFlavorParameters parameters = new CreateFlavorParameters();
            parameters.id = id;
            parameters.name = name;
            parameters.ram = ramMb;
            parameters.disk = diskGb;
            parameters.vcpus = cpu;
            return parameters;
        }
    }
}
