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

package io.maestro3.agent.openstack.api.images.extension;

import io.maestro3.agent.openstack.api.compute.bean.NovaImage;
import io.maestro3.agent.openstack.api.images.impl.BaseImageService;
import io.maestro3.agent.openstack.client.IOSClient;
import io.maestro3.agent.openstack.exception.OSClientException;
import io.maestro3.agent.openstack.helper.functions.EnforceVersionToUrl;
import io.maestro3.agent.model.compute.Image;
import io.maestro3.agent.openstack.transport.request.BasicOSRequest;
import org.springframework.util.Assert;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static io.maestro3.agent.openstack.transport.request.BasicOSRequest.builder;


public class ImageExtension extends BaseImageService implements IImageExtension {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";

    public ImageExtension(IOSClient client) {
        super(client);
    }

    @Override
    public Image get(String imageId) throws OSClientException {
        Assert.notNull(imageId, "imageId cannot be null or empty.");

        BasicOSRequest<NovaImage> request = builder(NovaImage.class, v2Endpoint())
                .path("/images/%s", imageId)
                .create();
        return client.execute(request).getEntity();
    }

    @Override
    public Image getProject(String tenantId, String imageId) throws OSClientException {
        Assert.notNull(imageId, "imageId cannot be null or empty.");

        BasicOSRequest<NovaImage> request = builder(NovaImage.class, v2Endpoint())
                .path("/images/%s?owner=%s", imageId, tenantId)
                .create();
        return client.execute(request).getEntity();
    }

    @Override
    public List<Image> listPublic() throws OSClientException {
        BasicOSRequest<ImagesWrapper> request = builder(ImagesWrapper.class, v2Endpoint())
                // by default, limit value is 25!
                .path("/images?visibility=public&limit=1000")
                .create();
        ImagesWrapper imagesWrapper = client.execute(request).getEntity();
        if (imagesWrapper == null) {
            return null;
        }
        return toImagesList(imagesWrapper.images);
    }

    @Override
    public List<Image> listPublic(Date updatedDate) throws OSClientException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        df.setTimeZone(tz);
        String dateInISO = df.format(updatedDate);
        BasicOSRequest<ImagesWrapper> request = builder(ImagesWrapper.class, v2Endpoint())
                .path("/images?visibility=public&limit=1000&status=active&updated_at=gte:%s", dateInISO)
                .create();
        ImagesWrapper imagesWrapper = client.execute(request).getEntity();
        if (imagesWrapper == null) {
            return null;
        }
        return toImagesList(imagesWrapper.images);
    }

    @Override
    public List<Image> listProject(String tenantId) throws OSClientException {
        BasicOSRequest<ImagesWrapper> request = builder(ImagesWrapper.class, v2Endpoint())
                .path("/images?owner=%s", tenantId)
                .create();
        ImagesWrapper imagesWrapper = client.execute(request).getEntity();
        return imagesWrapper == null ? null : toImagesList(imagesWrapper.images);
    }

    @Override
    public void delete(String imageId) throws OSClientException {
        Assert.hasText(imageId, "imageId cannot be null or empty.");

        BasicOSRequest<Void> request = builder(Void.class, v2Endpoint())
                .path("/images/%s", imageId)
                .delete()
                .create();
        client.execute(request);
    }

    private List<Image> toImagesList(List<NovaImage> images) {
        return images.stream().map(novaImage -> (Image) novaImage).collect(Collectors.toList());
    }

    private URL v2Endpoint() throws OSClientException {
        return endpoint(EnforceVersionToUrl.to("/v2", false));
    }

    private static class ImagesWrapper {
        List<NovaImage> images;
    }
}
