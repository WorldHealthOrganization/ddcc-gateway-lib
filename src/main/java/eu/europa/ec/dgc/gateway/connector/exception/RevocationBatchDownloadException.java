/*-
 * ---license-start
 * WHO Digital Documentation Covid Certificate Gateway Service / ddcc-gateway-lib
 * ---
 * Copyright (C) 2022 - 2024 T-Systems International GmbH and all other contributors
 * ---
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
 * ---license-end
 */

package eu.europa.ec.dgc.gateway.connector.exception;

import lombok.Getter;

@Getter
public class RevocationBatchDownloadException extends RuntimeException {


    private final int status;

    public RevocationBatchDownloadException(String message, Throwable inner) {
        super(message, inner);
        this.status = 500;
    }

    public RevocationBatchDownloadException(String message) {
        super(message);
        this.status = 500;
    }

    public RevocationBatchDownloadException(String message, Throwable inner, int status) {
        super(message, inner);
        this.status = status;
    }

    public RevocationBatchDownloadException(String message, int status) {
        super(message);
        this.status = status;
    }
}
