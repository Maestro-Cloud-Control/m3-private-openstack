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

package io.maestro3.agent.service.proccessor;

import io.maestro3.agent.model.base.PlatformType;
import io.maestro3.agent.model.compute.Flavor;
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.model.region.OpenStackRegionConfig;
import io.maestro3.agent.openstack.api.networking.bean.Network;
import io.maestro3.agent.openstack.api.networking.bean.SecurityGroup;
import io.maestro3.agent.util.PrivateWizardUtils;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkCellItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkOptionItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkSelectItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableHeaderItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableHeaderType;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableRowItem;
import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenstackWizardItems {
    private static final String YYYY_MM_DD_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern(YYYY_MM_DD_FORMAT);


    public static SdkSelectItem buildSelectRegions(List<OpenStackRegionConfig> osRegions, String regionName) {
        List<SdkOptionItem> options = new ArrayList<>();
        for (OpenStackRegionConfig osRegion : osRegions) {
            SdkOptionItem optionItem = PrivateWizardUtils.getOptionItem(osRegion.getRegionAlias(), false);
            if (osRegion.getRegionAlias().equals(regionName)) {
                optionItem.setSelect(buildUseFilledParameters());
                optionItem.setSelected(true);
            }
            options.add(optionItem);
        }
        return SdkSelectItem.builder()
            .withName(OpenstackConfigurationWizardConstant.REGION_SELECT_ITEM)
            .withTitle("Private region")
            .withType("list")
            .withDescription("Regions that were configured on private agent")
            .withOrder("0")
            .withOptions(options)
            .withSelectedIndex(PrivateWizardUtils.getAndFixSingleSelectedIndex(options))
            .build();
    }

    public static SdkTableItem buildFlavorTable(List<Flavor> flavors) {
        List<SdkTableHeaderItem> headers = new ArrayList<>();
        headers.add(SdkTableHeaderItem.builder().withTitle("Name").withName("name").build());
        headers.add(SdkTableHeaderItem.builder().withTitle("vCPU").withName("vcpu").build());
        headers.add(SdkTableHeaderItem.builder().withTitle("RAM").withName("ram").build());
        headers.add(SdkTableHeaderItem.builder().withTitle("Storage").withName("storage").build());
        headers.add(buildCheckboxHeader());
        return SdkTableItem.builder()
            .withName(OpenstackConfigurationWizardConstant.FLAVOR_TABLE)
            .withOrder("1")
            .withHeaders(headers)
            .withRow(buildFlavorTableRows(flavors))
            .withTitle("Openstack flavor configuration")
            .withDescription("Select openstack flavors to use its as shapes in maestro")
            .build();
    }

    public static SdkTableItem buildImagesTable(List<Image> images) {
        List<SdkTableHeaderItem> headers = new ArrayList<>();
        headers.add(SdkTableHeaderItem.builder().withTitle("Name").withName("name").build());
        headers.add(SdkTableHeaderItem.builder().withTitle("Updated at").withName("updated").build());
        headers.add(SdkTableHeaderItem.builder().withTitle("Owner").withName("owner").build());
        headers.add(buildCheckboxHeader());
        return SdkTableItem.builder()
            .withName(OpenstackConfigurationWizardConstant.IMAGE_TABLE)
            .withOrder("0")
            .withHeaders(headers)
            .withRow(buildImageTableRows(images))
            .withTitle("Openstack image configuration")
            .withDescription("Select openstack images to use its in maestro")
            .build();
    }

    public static SdkSelectItem buildEnableScheduleSelectItem() {
        List<SdkOptionItem> optionItems = Collections.singletonList(PrivateWizardUtils.getOptionItem("Enable schedules", true));
        return SdkSelectItem.builder()
            .withName(OpenstackConfigurationWizardConstant.ENABLE_SCHEDULE_ITEM)
            .withType("BOX")
            .withOrder("2")
            .withDescription("Enable scheduled describers")
            .withMultiple(true)
            .withSelectedIndex(0)
            .withOptions(optionItems)
            .build();
    }

    public static SdkSelectItem buildEnableManagementSelectItem() {
        List<SdkOptionItem> optionItems = Collections.singletonList(PrivateWizardUtils.getOptionItem("Enable management", true));
        return SdkSelectItem.builder()
            .withName(OpenstackConfigurationWizardConstant.ENABLE_MANAGEMENT_ITEM)
            .withType("BOX")
            .withOrder("4")
            .withDescription("Enable vm management")
            .withMultiple(true)
            .withSelectedIndex(0)
            .withOptions(optionItems)
            .build();
    }

    public static SdkSelectItem buildUseFilledParameters() {
        List<SdkOptionItem> optionItems = Collections.singletonList(PrivateWizardUtils.getOptionItem("Use filled parameters", true));
        return SdkSelectItem.builder()
            .withName(OpenstackConfigurationWizardConstant.FILLED_PARAMS_ITEM)
            .withType("BOX")
            .withOrder("2")
            .withDescription("Use parameters from region configuration wizard")
            .withMultiple(true)
            .withSelectedIndex(0)
            .withOptions(optionItems)
            .build();
    }

    public static SdkSelectItem buildNetworkSelect(List<Network> networks) {
        List<SdkOptionItem> options = new ArrayList<>();
        for (Network network : networks) {
            options.add(PrivateWizardUtils.getOptionItem(network.getName(), network.getId(), false));
        }
        return SdkSelectItem.builder()
            .withName(OpenstackConfigurationWizardConstant.NETWORK_SELECT_ITEM)
            .withTitle("Openstack network")
            .withType("list")
            .withDescription("Default network that will be used for instances in tenant")
            .withOrder("0")
            .withOptions(options)
            .withSelectedIndex(PrivateWizardUtils.getAndFixSingleSelectedIndex(options))
            .build();
    }

    public static SdkSelectItem buildSecurityGroupSelect(List<SecurityGroup> securityGroups) {
        List<SdkOptionItem> options = new ArrayList<>();
        for (SecurityGroup securityGroup : securityGroups) {
            options.add(PrivateWizardUtils.getOptionItem(securityGroup.getName(), securityGroup.getId(), false));
        }
        return SdkSelectItem.builder()
            .withName(OpenstackConfigurationWizardConstant.SECURITY_GROUP_SELECT_ITEM)
            .withTitle("Openstack security group")
            .withType("list")
            .withDescription("Default security group that will be used for instances in tenant")
            .withOrder("1")
            .withOptions(options)
            .withSelectedIndex(PrivateWizardUtils.getAndFixSingleSelectedIndex(options))
            .build();
    }

    public static SdkSelectItem buildDescriberModeSelect() {
        List<SdkOptionItem> options = new ArrayList<>();
        for (String mode : Arrays.asList(OpenstackConfigurationWizardConstant.ALL_MODE, OpenstackConfigurationWizardConstant.MAESTRO_MODE)) {
            options.add(PrivateWizardUtils.getOptionItem(mode, false));
        }
        return SdkSelectItem.builder()
            .withName(OpenstackConfigurationWizardConstant.DESCRIBER_SELECT_ITEM)
            .withTitle("Describer mode")
            .withType("list")
            .withDescription("Default describer mode that will be used for vm on this tenant")
            .withOrder("2")
            .withOptions(options)
            .withSelectedIndex(PrivateWizardUtils.getAndFixSingleSelectedIndex(options))
            .build();
    }

    public static SdkTableItem buildImageToPlatformTable(Map<String, String> idToName) {
        List<SdkTableRowItem> defaultRows = new ArrayList<>();
        for (Map.Entry<String, String> imageParam : idToName.entrySet()) {
            List<SdkCellItem> cells = Arrays.asList(
                SdkCellItem.builder()
                    .withHint("Image native name")
                    .withValue(imageParam.getValue())
                    .withDisabled(true)
                    .build(),
                SdkCellItem.builder()
                    .withHint("Image platform type")
                    .withValue(PlatformType.OTHER.name())
                    .withDisabled(false)
                    .build());
            defaultRows.add(SdkTableRowItem.builder()
                .withCell(cells)
                .withHiddenData("id", imageParam.getKey())
                .withHiddenData("name", imageParam.getValue())
                .build());
        }
        List<SdkTableHeaderItem> tableHeaderItems = new ArrayList<>();
        SdkTableHeaderItem maestroHeader = SdkTableHeaderItem.builder()
            .withName("imageName")
            .withOverridable(false)
            .withTitle("Image name")
            .build();
        tableHeaderItems.add(maestroHeader);
        List<SdkOptionItem> options = Arrays.stream(PlatformType.values())
            .map(t -> SdkOptionItem.builder()
                .withName(t.name())
                .withValue(t.name())
                .withSelectedIndex(0)
                .build())
            .collect(Collectors.toList());
        SdkTableHeaderItem nativeHeader = SdkTableHeaderItem.builder()
            .withName("imageType")
            .withTitle("Platform type")
            .withOptions(options)
            .withOverridable(false)
            .build();
        tableHeaderItems.add(nativeHeader);
        return SdkTableItem.builder()
            .withName(OpenstackConfigurationWizardConstant.IMAGE_TO_PLATFORM_TABLE)
            .withTitle("Set platform mapping for selected images")
            .withOrder("0")
            .withRow(defaultRows)
            .withHeaders(tableHeaderItems)
            .build();
    }

    private static List<SdkTableRowItem> buildFlavorTableRows(List<Flavor> flavors) {
        List<SdkTableRowItem> rows = new ArrayList<>();
        flavors.stream()
            .sorted(Comparator.comparing(Flavor::getName).thenComparing(Flavor::getVcpus))
            .forEach(flavor -> {
                List<SdkCellItem> cells = new ArrayList<>();
                cells.add(buildBaseCell(flavor.getName(), flavor.getName()));
                String vcpus = flavor.getVcpus() + " vCPU";
                cells.add(buildBaseCell(vcpus, vcpus));
                String ram = flavor.getRam() + " MB";
                cells.add(buildBaseCell(ram, ram));
                String disk = flavor.getDisk() == 0 ? "UNKNOWN" : flavor.getDisk() + " GB";
                cells.add(buildBaseCell(disk, disk));
                cells.add(buildBooleanCell(false));
                rows.add(SdkTableRowItem.builder().withCell(cells)
                    .withHiddenData("id", flavor.getId())
                    .withHiddenData("name", String.valueOf(flavor.getName()))
                    .withHiddenData("ram", String.valueOf(flavor.getRam()))
                    .withHiddenData("cpu", String.valueOf(flavor.getVcpus()))
                    .withHiddenData("disk", String.valueOf(flavor.getDisk())).build());
            });
        return rows;
    }

    private static List<SdkTableRowItem> buildImageTableRows(List<Image> images) {
        List<SdkTableRowItem> rows = new ArrayList<>();
        images.stream()
            .sorted(Comparator.comparing(Image::getName).thenComparing(Image::getUpdated))
            .forEach(image -> {
                List<SdkCellItem> cells = new ArrayList<>();
                cells.add(buildBaseCell(image.getName(), image.getName()));
                String updatedAt = getYearMonthDayUtcString(new DateTime(image.getUpdated()));
                cells.add(buildBaseCell(updatedAt, updatedAt));
                cells.add(buildBaseCell(image.getOwner(), image.getOwner()));
                cells.add(buildBooleanCell(false));
                rows.add(SdkTableRowItem.builder().withCell(cells)
                    .withHiddenData("id", image.getId())
                    .withHiddenData("name", image.getName()).build());
            });
        return rows;
    }

    private static SdkTableHeaderItem buildCheckboxHeader() {
        return SdkTableHeaderItem.builder()
            .withName("needConfirmation")
            .withTitle("Activate")
            .withType(SdkTableHeaderType.CHECKBOX)
            .build();
    }

    private static SdkCellItem buildBaseCell(String title, String value) {
        return SdkCellItem.builder()
            .withValue(value)
            .withTitle(title)
            .withDisabled(true)
            .build();
    }

    private static SdkCellItem buildBooleanCell(boolean value) {
        return SdkCellItem.builder()
            .withValue(String.valueOf(value))
            .withDisabled(false)
            .build();
    }

    private static String getYearMonthDayUtcString(DateTime dateTime) {
        return FORMATTER_YYYY_MM_DD.format(toLocalDateTime(dateTime));
    }


    private static LocalDateTime toLocalDateTime(DateTime dateTime) {
        return LocalDateTime.ofInstant(dateTime.toDate().toInstant(), ZoneId.of("UTC"));
    }

    private OpenstackWizardItems() {
    }
}
