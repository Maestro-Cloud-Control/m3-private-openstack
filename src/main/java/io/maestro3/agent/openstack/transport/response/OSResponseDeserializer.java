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

package io.maestro3.agent.openstack.transport.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.maestro3.agent.http.client.serialization.Deserializer;
import io.maestro3.agent.model.common.MappedEnum;
import io.maestro3.agent.openstack.api.compute.bean.NovaBlockStorageMapping;
import io.maestro3.agent.openstack.api.compute.bean.NovaImage;
import io.maestro3.sdk.internal.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;


public class OSResponseDeserializer implements Deserializer {

    private static final Logger LOG = LoggerFactory.getLogger(OSResponseDeserializer.class);

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(Type responseType, HttpResponse response) throws Exception {
        if (response == null || Void.class.equals(responseType)) {
            return null;
        }
        String json = EntityUtils.toString(response.getEntity());

        LOG.debug("Response json: {}", json);

        if (String.class.equals(responseType)) {
            return (T) json;
        }
        if (StringUtils.isNotEmpty(json)) {
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(MappedEnum.class, new MappedEnumDeserializer())
                    .registerTypeAdapter(Date.class, new UtcDateDeserializer())
                    .registerTypeAdapter(NovaImage.class, new BlockStorageMappingDeserializer())
                    .create();
            return gson.fromJson(json, responseType);
        }
        return null;
    }

    private static class MappedEnumDeserializer implements JsonDeserializer<MappedEnum>, JsonSerializer<MappedEnum> {
        @Override
        public MappedEnum deserialize(JsonElement json, Type typeOfEnum, JsonDeserializationContext context) throws JsonParseException {
            if (!(typeOfEnum instanceof Class)) {
                return null;
            }
            Object[] enumConstants = ((Class) typeOfEnum).getEnumConstants();
            if (enumConstants == null) {
                return null;
            }
            for (Object constant : enumConstants) {
                if (constant instanceof MappedEnum) {
                    MappedEnum mappedEnumConstant = (MappedEnum) constant;
                    if (mappedEnumConstant.getMapping().equals(json.getAsString())) {
                        return mappedEnumConstant;
                    }
                }
            }
            return null;
        }

        @Override
        public JsonElement serialize(MappedEnum src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.getMapping());
        }
    }

    private static class UtcDateDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfEnum, JsonDeserializationContext context) throws JsonParseException {
            return DateUtils.parseDate(json.getAsString(), DateUtils.FORMAT_TIME);
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(DateUtils.formatDate(src, DateUtils.FORMAT_TIME));
        }
    }

    private static class BlockStorageMappingDeserializer implements JsonDeserializer<NovaImage> {

        private static final String BLOCK_DEVICE_MAPPING = "block_device_mapping";

        @Override
        public NovaImage deserialize(JsonElement json, Type typeOfEnum, JsonDeserializationContext context) throws JsonParseException {
            JsonObject asJsonObject = json.getAsJsonObject();

            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(MappedEnum.class, new MappedEnumDeserializer())
                    .registerTypeAdapter(Date.class, new UtcDateDeserializer())
                    .create();

            NovaImage novaImage = gson.fromJson(json, typeOfEnum);
            setupStorageMappingIfNeeded(asJsonObject, gson, novaImage);

            return novaImage;
        }

        private void setupStorageMappingIfNeeded(JsonObject asJsonObject, Gson gson, NovaImage novaImage) {
            JsonElement blockMappingList = asJsonObject.get(BLOCK_DEVICE_MAPPING);
            if (blockMappingList == null) {
                return;
            }

            String blockMappingListAsString = blockMappingList.getAsString();
            Reader reader = new InputStreamReader(new ByteArrayInputStream(blockMappingListAsString.getBytes()));
            JsonElement fromJson = gson.fromJson(reader, JsonElement.class);
            JsonArray blockMappingListAsJsonArray = fromJson.getAsJsonArray();
            JsonElement blockMapping = blockMappingListAsJsonArray.get(0);
            NovaBlockStorageMapping blockStorageMapping = gson.fromJson(blockMapping, NovaBlockStorageMapping.class);
            novaImage.setBlockStorageMappings(Collections.singletonList(blockStorageMapping));
        }
    }
}
