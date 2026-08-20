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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Representation of a W3C DID Document.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DidDocument {

    @JsonProperty("@context")
    private Object context;

    private String id;

    private Object controller;

    private Object alsoKnownAs;

    private List<VerificationMethod> verificationMethod;

    private List<Object> authentication;

    private List<Object> assertionMethod;

    private List<Object> keyAgreement;

    private List<Object> capabilityInvocation;

    private List<Object> capabilityDelegation;

    private List<Service> service;

    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    /**
     * Returns any additional, unmapped properties of the DID Document.
     *
     * @return the additional properties.
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Stores an additional, unmapped property of the DID Document.
     *
     * @param key   the property name.
     * @param value the property value.
     */
    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }
}
