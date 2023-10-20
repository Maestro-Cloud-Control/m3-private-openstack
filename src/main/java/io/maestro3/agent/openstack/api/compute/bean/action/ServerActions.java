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

package io.maestro3.agent.openstack.api.compute.bean.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.maestro3.agent.model.compute.RebootType;


public final class ServerActions {

    private static final ServerAction START_ACTION = new StartServer();
    private static final ServerAction STOP_ACTION = new StopServer();
    private static final ServerAction SUSPEND_ACTION = new SuspendServer();
    private static final ServerAction RESUME_ACTION = new ResumeServer();
    private static final ServerAction CONFIRM_RESIZE = new ConfirmResize();
    private static final ServerAction SOFT_REBOOT_ACTION = new RebootServer(RebootType.SOFT);
    private static final ServerAction HARD_REBOOT_ACTION = new RebootServer(RebootType.HARD);
    private static final ServerAction GET_VNC_CONSOLE = new GetVncConsoleAction();

    private ServerActions() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    /**
     * Gets start server action
     *
     * @return start server action
     */
    public static ServerAction start() {
        return START_ACTION;
    }

    /**
     * Gets stop server action
     *
     * @return stop server action
     */
    public static ServerAction stop() {
        return STOP_ACTION;
    }

    /**
     * Gets suspend server action
     *
     * @return suspend server action
     */
    public static ServerAction suspend() {
        return SUSPEND_ACTION;
    }

    /**
     * Gets reboot server action
     *
     * @param type type of reboot
     * @return reboot server action
     */
    public static ServerAction reboot(RebootType type) {
        switch (type) {
            case SOFT:
                return SOFT_REBOOT_ACTION;
            case HARD:
                return HARD_REBOOT_ACTION;
            default:
                throw new RuntimeException("Unknown reboot type");
        }
    }

    public static ServerAction addSecurityGroup(String securityGroup) {
        return new AddSecurityGroupAction(securityGroup);
    }

    public static ServerAction removeSecurityGroup(String securityGroup) {
        return new RemoveSecurityGroupAction(securityGroup);
    }

    public static ServerAction resume() {
        return RESUME_ACTION;
    }

    public static ServerAction resize(String flavor) {
        return new ResizeServer(flavor, "AUTO");
    }

    public static ServerAction confirmResize() {
        return CONFIRM_RESIZE;
    }

    public static ServerAction vncConsole() {
        return GET_VNC_CONSOLE;
    }

    public static ServerAction createImage(String imageName) {
        return new CreateImageAction(imageName);
    }

    public static class ConfirmResize implements ServerAction {
        @JsonProperty("confirmResize")
        private String confirmResizeValue;
    }

    private static class RebootServer implements ServerAction {
        Reboot reboot;

        RebootServer(RebootType type) {
            reboot = new Reboot();
            reboot.type = type;
        }

        static class Reboot {
            RebootType type;
        }
    }private static class AddSecurityGroupAction implements ServerAction {
        private AddSecurityGroup addSecurityGroup;

        public AddSecurityGroupAction(String name) {
            addSecurityGroup = new AddSecurityGroup();
            addSecurityGroup.name = name;
        }

        static class AddSecurityGroup {
            private String name;
        }
    }

    private static class RemoveSecurityGroupAction implements ServerAction {

        private RemoveSecurityGroup removeSecurityGroup;

        RemoveSecurityGroupAction(String name) {
            removeSecurityGroup = new RemoveSecurityGroup();
            removeSecurityGroup.name = name;
        }

        static class RemoveSecurityGroup {
            private String name;
        }
    }

    private static class StartServer implements ServerAction {
        @SerializedName("os-start")
        private String start;
    }

    private static class StopServer implements ServerAction {
        @SerializedName("os-stop")
        private String stop;
    }

    private static class ResizeServer implements ServerAction {
        private Resize resize;

        public ResizeServer(String flavorRef, String diskConfig) {
            this.resize = new Resize();
            this.resize.flavorRef = flavorRef;
            this.resize.diskConfig = diskConfig;
        }

        static class Resize {
            private String flavorRef;
            @SerializedName("OS-DCF:diskConfig")
            private String diskConfig;
        }
    }

    private static class SuspendServer implements ServerAction {
        private String suspend;
    }

    private static class ResumeServer implements ServerAction {
        private String resume;
    }

    private static class GetVncConsoleAction implements ServerAction {
        @SerializedName("os-getVNCConsole")
        private final GetConsole console;

        private GetVncConsoleAction() {
            this.console = new GetConsole("novnc");
        }
    }

    private static class GetConsole {
        private final String type;

        private GetConsole(String type) {
            this.type = type;
        }
    }

    public static ServerAction rebuildServer(String imageRef) {
        return new RebuildServerAction(imageRef);
    }

    private static class RebuildServerAction implements ServerAction {
        private Rebuild rebuild;

        public RebuildServerAction(String imageRef) {
            rebuild = new Rebuild();
            rebuild.imageRef = imageRef;
        }

        static class Rebuild {
            private String imageRef;
        }
    }

    private static class CreateImageAction implements ServerAction {
        private final CreateImage createImage;

        private CreateImageAction(String imageName) {
            this.createImage = new CreateImage(imageName);
        }
    }

    private static class CreateImage {
        private final String name;

        private CreateImage(String name) {
            this.name = name;
        }
    }
}
