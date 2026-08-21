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

package eu.europa.ec.dgc.did.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Metadata about the DID resolution process as defined by the DID Resolution specification.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DidResolutionMetadata {

    /** Error code indicating that the supplied DID is syntactically invalid. */
    public static final String ERROR_INVALID_DID = "invalidDid";

    /** Error code indicating that the DID Document could not be found. */
    public static final String ERROR_NOT_FOUND = "notFound";

    /** Error code indicating that the DID method is not supported by any registered driver. */
    public static final String ERROR_METHOD_NOT_SUPPORTED = "methodNotSupported";

    /** Error code indicating an unexpected internal error during resolution. */
    public static final String ERROR_INTERNAL = "internalError";

    private String contentType;

    private String error;

    private String errorMessage;

    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    /**
     * Returns any additional, unmapped resolution metadata properties.
     *
     * @return the additional properties.
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Stores an additional, unmapped resolution metadata property.
     *
     * @param key   the property name.
     * @param value the property value.
     */
    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }
}
