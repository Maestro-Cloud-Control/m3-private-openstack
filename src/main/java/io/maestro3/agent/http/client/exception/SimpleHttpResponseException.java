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

package io.maestro3.agent.http.client.exception;


public class SimpleHttpResponseException extends SimpleHttpClientException {

    private final String errorMessage;
    private final int statusCode;
    private final String reasonPhrase;

    public SimpleHttpResponseException(String errorMessage, int statusCode, String reasonPhrase) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return (errorMessage == null) ? null : errorMessage.trim();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

}
