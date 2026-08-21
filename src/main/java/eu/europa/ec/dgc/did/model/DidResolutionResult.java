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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Result of a DID resolution containing the DID Document and the associated resolution metadata.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DidResolutionResult {

    /** JSON-LD context of a DID resolution result. */
    public static final String DID_RESOLUTION_CONTEXT = "https://w3id.org/did-resolution/v1";

    /** Media type of a JSON-LD DID Document representation. */
    public static final String CONTENT_TYPE_DID_LD_JSON = "application/did+ld+json";

    @JsonProperty("@context")
    private String context;

    private DidDocument didDocument;

    private DidResolutionMetadata didResolutionMetadata;

    private DidDocumentMetadata didDocumentMetadata;

    /**
     * Creates a successful resolution result wrapping the given DID Document.
     *
     * @param didDocument the resolved DID Document.
     * @return a populated {@link DidResolutionResult}.
     */
    public static DidResolutionResult success(DidDocument didDocument) {
        DidResolutionResult result = new DidResolutionResult();
        result.context = DID_RESOLUTION_CONTEXT;
        result.didDocument = didDocument;

        DidResolutionMetadata resolutionMetadata = new DidResolutionMetadata();
        resolutionMetadata.setContentType(CONTENT_TYPE_DID_LD_JSON);
        result.didResolutionMetadata = resolutionMetadata;

        result.didDocumentMetadata = new DidDocumentMetadata();
        return result;
    }

    /**
     * Creates a failed resolution result with the given error code and message.
     *
     * @param error   the resolution error code.
     * @param message a human-readable error description.
     * @return a populated {@link DidResolutionResult}.
     */
    public static DidResolutionResult error(String error, String message) {
        DidResolutionResult result = new DidResolutionResult();
        result.context = DID_RESOLUTION_CONTEXT;

        DidResolutionMetadata resolutionMetadata = new DidResolutionMetadata();
        resolutionMetadata.setError(error);
        resolutionMetadata.setErrorMessage(message);
        result.didResolutionMetadata = resolutionMetadata;

        result.didDocumentMetadata = new DidDocumentMetadata();
        return result;
    }

    /**
     * Indicates whether this result represents a resolution error.
     *
     * @return {@code true} if an error code is present.
     */
    @JsonIgnore
    public boolean isError() {
        return didResolutionMetadata != null && didResolutionMetadata.getError() != null;
    }
}
