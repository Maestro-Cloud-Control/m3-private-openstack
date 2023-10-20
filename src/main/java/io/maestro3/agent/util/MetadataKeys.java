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

package io.maestro3.agent.util;


public final class MetadataKeys {

    public static final String ADMIN_GROUP = "admin_group";
    public static final String ADMIN_USER = "admin_user";
    public static final String ADMIN_PASS = "admin_pass";
    public static final String OWNER = "owner";
    public static final String JOIN_TO_ACTIVE_DIRECTORY = "join_to_ad";
    public static final String OS_PREVIOUS_PROJECT_ID = "os_previous_project_id";
    public static final String EO_PREVIOUS_PROJECT_ID = "eo_previous_project_id";
    public static final String EO_PREVIOUS_MAC_ADDRESS = "eo_previous_mac_address";
    public static final String EO_PREVIOUS_NETWORK_ID = "eo_previous_network_id";
    public static final String EO_IP_ADDRESS_ID_TO_ASSIGN = "eo_ip_address_id_to_assign";
    public static final String OS_PROJECT_ID_BEFORE_MOVE_TO_RECYCLE_BIN = "os_project_id_before_move_to_recycle_bin";
    public static final String EO_PROJECT_NAME_BEFORE_MOVE_TO_RECYCLE_BIN = "eo_project_name_before_move_to_recycle_bin";
    public static final String EO_INSTANCE_NAME_BEFORE_MOVE_TO_RECYCLE_BIN = "eo_instance_name_before_move_to_recycle_bin";
    public static final String EO_MOVE_INSTANCE_TO_RECYCLE_BIN_DATE = "eo_move_instance_to_recycle_bin_date";
    public static final String EO_MOVE_INSTANCE_TO_RECYCLE_BIN_DATE_STRING = "eo_move_instance_to_recycle_bin_date_string";
    public static final String EO_ALLOW_TO_DESCRIBE_IN_RECYCLE_BIN = "eo_allow_to_describe_in_recycle_bin";
    public static final String EO_TEMPORARY_RESOURCE_FOR_MIGRATION = "eo_temporary_resource_for_migration";

    public static final String EO_FIXED_IP_ADDRESS = "eo_fixed_ip_address";
    public static final String EO_PREVIOUS_DESCRIPTION = "eo_previous_description";
    public static final String EO_OWNER_ID = "eo_owner_id";
    public static final String EO_OWNER_EMAIL = "eo_owner_email";
    public static final String EO_LINKED_IMAGE_ID = "eo_linked_image_id";

    public static final String CURRENT_SERVER_STATUS = "current_server_status";
    public static final String CURRENT_SERVER_POWER_STATE = "current_server_power_state";
    public static final String CURRENT_SERVER_TASK_STATE = "current_server_task_state";

    public static final String VOLUME_PURPOSE = "orch_volume_purpose";
    public static final String VOLUME_DEVICE = "target_volume_device";
    public static final String VOLUME_CREATED_FOR_IMAGE_WITH_ID = "orch_image_id";
    public static final String VOLUME_RESERVED_FOR_SERVER_WITH_ID = "volume_reserved_for_server_with_id";
    public static final String VOLUME_RESERVED_ATTACHMENT_MEET_PRECONDITIONS = "volume_reserved_attachment_meet_preconditions";
    public static final String VOLUME_RESERVED_ATTACHMENT_ALLOWED = "volume_reserved_attachment_allowed";

    public static final String FLAVOR_AGGREGATE_INSTANCE_SSD_EXTRA_SPEC_PARAM = "aggregate_instance_extra_specs:ssd";
}
