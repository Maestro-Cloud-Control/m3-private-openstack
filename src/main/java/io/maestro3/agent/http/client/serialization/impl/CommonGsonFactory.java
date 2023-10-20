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

package io.maestro3.agent.http.client.serialization.impl;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;


public final class CommonGsonFactory {

    private CommonGsonFactory() {
        throw new UnsupportedOperationException("Instantiation is forbidden.");
    }

    public static Gson create() {
        return new GsonBuilder().create();
    }

    public static Gson createWithAdapter(Type type, Object adapter) {
        return new GsonBuilder().registerTypeAdapter(type, adapter).create();
    }

}
