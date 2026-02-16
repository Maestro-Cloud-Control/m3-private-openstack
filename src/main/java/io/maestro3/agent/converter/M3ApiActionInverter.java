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

package io.maestro3.agent.converter;

import io.maestro3.sdk.v3.core.M3ActionParamNames;
import io.maestro3.sdk.v3.core.M3ApiAction;
import io.maestro3.sdk.v3.model.SdkCloud;
import io.maestro3.sdk.v3.request.image.CreateImageRequest;
import io.maestro3.sdk.v3.request.image.DeleteImageRequest;
import io.maestro3.sdk.v3.request.image.DescribeImageRequest;
import io.maestro3.sdk.v3.request.instance.DescribeInstanceRequest;
import io.maestro3.sdk.v3.request.instance.GetInstanceHashedPasswordRequest;
import io.maestro3.sdk.v3.request.instance.RebootInstanceRequest;
import io.maestro3.sdk.v3.request.instance.RunInstanceRequest;
import io.maestro3.sdk.v3.request.instance.StartInstanceRequest;
import io.maestro3.sdk.v3.request.instance.StopInstanceRequest;
import io.maestro3.sdk.v3.request.instance.TerminateInstanceRequest;
import io.maestro3.sdk.v3.request.resource.ResourceRequest;
import io.maestro3.sdk.v3.request.ssh.AddKeyRequest;
import io.maestro3.sdk.v3.request.ssh.DeleteKeyRequest;
import io.maestro3.sdk.v3.request.ssh.DescribeKeysRequest;
import io.maestro3.sdk.v3.request.volume.AttachVolumeRequest;
import io.maestro3.sdk.v3.request.volume.CreateAndAttachVolumeRequest;
import io.maestro3.sdk.v3.request.volume.CreateVolumeRequest;
import io.maestro3.sdk.v3.request.volume.DetachVolumeRequest;
import io.maestro3.sdk.v3.request.volume.RemoveVolumeRequest;

import java.util.Map;


public final class M3ApiActionInverter {

    private M3ApiActionInverter() {
        throw new UnsupportedOperationException();
    }

    public static RunInstanceRequest toRunInstanceRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return RunInstanceRequest.builder()
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withInstanceName(String.valueOf(params.get(M3ActionParamNames.INSTANCE_NAME)))
                .withShape(String.valueOf(params.get("shape")))
                .withImageId(String.valueOf(params.get("imageId")))
                .withIp((String) params.get("ip"))
                .withInitScript(String.valueOf(params.get(M3ActionParamNames.INIT_SCRIPT)))
                .withKeyName(String.valueOf(params.get(M3ActionParamNames.KEY_NAME)))
                .withOwner(String.valueOf(params.get(M3ActionParamNames.OWNER)))
                .withCount(Integer.valueOf(String.valueOf(params.get(M3ActionParamNames.COUNT))))
                .build();

    }

    public static StartInstanceRequest toStartInstanceRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return StartInstanceRequest.builder()
            .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
            .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
            .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
            .build();
    }

    public static StopInstanceRequest toStopInstanceRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return StopInstanceRequest.builder()
            .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
            .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
            .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
            .build();
    }

    public static RebootInstanceRequest toRebootInstanceRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return RebootInstanceRequest.builder()
            .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
            .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
            .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
            .build();
    }

    public static TerminateInstanceRequest toTerminateInstanceRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return TerminateInstanceRequest.builder()
            .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
            .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
            .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
            .build();
    }

    public static GetInstanceHashedPasswordRequest toHashedPasswordRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return new GetInstanceHashedPasswordRequest.Builder()
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
                .build();
    }

    public static DescribeInstanceRequest toDescribeInstancesRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return DescribeInstanceRequest.builder()
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withAll(true)
                // M3ActionParamNames.INSTANCE_IDS ignored for now
                .build();
    }

    public static AddKeyRequest toAddKeyRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return AddKeyRequest.builder()
                .withName(String.valueOf(params.get(M3ActionParamNames.NAME)))
                .withPublicKey(String.valueOf(params.get("publicKey")))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .withCloud(SdkCloud.fromValue(String.valueOf(params.get(M3ActionParamNames.CLOUD))))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                // M3ActionParamNames.INSTANCE_IDS ignored for now
                .build();
    }

    public static DeleteKeyRequest toDeleteKeyRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return DeleteKeyRequest.builder()
                .withName(String.valueOf(params.get(M3ActionParamNames.NAME)))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                // M3ActionParamNames.INSTANCE_IDS ignored for now
                .build();
    }

    public static DescribeKeysRequest toDescribeKeyRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return DescribeKeysRequest.builder()
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                // M3ActionParamNames.INSTANCE_IDS ignored for now
                .build();
    }

    public static DescribeImageRequest toDescribeImageRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return DescribeImageRequest.builder()
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .build();
    }

    public static CreateImageRequest toCreateImageRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return CreateImageRequest.builder()
                .withDescription(String.valueOf(params.get("description")))
                .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
                .withName(String.valueOf(params.get(M3ActionParamNames.NAME)))
                .withOwner(String.valueOf(params.get(M3ActionParamNames.OWNER)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }

    public static DeleteImageRequest toDeleteImageRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return DeleteImageRequest.builder()
                .withImageId(String.valueOf(params.get("imageId")))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }

    public static CreateAndAttachVolumeRequest toCreateAndAttachVolumeRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return CreateAndAttachVolumeRequest.builder()
                .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withSizeInGB(Integer.valueOf(String.valueOf(params.get("sizeInGB"))))
                .withVolumeName(String.valueOf(params.get("volumeName")))
                .withVolumeId(String.valueOf(params.get("volumeId")))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }

    public static CreateVolumeRequest toCreateVolumeRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return CreateVolumeRequest.builder()
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withSizeInGB(Integer.valueOf(String.valueOf(params.get("sizeInGB"))))
                .withVolumeName(String.valueOf(params.get("volumeName")))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }

    public static AttachVolumeRequest toAttachVolumeRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return AttachVolumeRequest.builder()
                .withInstanceId(String.valueOf(params.get(M3ActionParamNames.INSTANCE_ID)))
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withVolumeId(String.valueOf(params.get("volumeId")))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }

    public static ResourceRequest toResourceRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return ResourceRequest.builder()
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }

    public static DetachVolumeRequest toDetachVolumeRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return DetachVolumeRequest.builder()
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withVolumeId(String.valueOf(params.get("volumeId")))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }

    public static RemoveVolumeRequest toRemoveVolumeRequest(M3ApiAction apiAction) {
        Map<String, Object> params = apiAction.getParams();
        return RemoveVolumeRequest.builder()
                .withRegion(String.valueOf(params.get(M3ActionParamNames.REGION)))
                .withVolumeId(String.valueOf(params.get("volumeId")))
                .withTenantName(String.valueOf(params.get(M3ActionParamNames.TENANT_NAME)))
                .build();
    }
}
