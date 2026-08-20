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

package eu.europa.ec.dgc.did;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.did.model.DidDocument;
import eu.europa.ec.dgc.did.model.DidResolutionMetadata;
import eu.europa.ec.dgc.did.model.DidResolutionResult;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.StringJoiner;
import lombok.extern.slf4j.Slf4j;

/**
 * DID method driver implementing the {@code did:web} resolution algorithm.
 *
 * <p>The driver transforms a {@code did:web} identifier into an HTTPS URL, downloads the DID Document
 * and wraps it into a {@link DidResolutionResult}.
 */
@Slf4j
public class DidWebDriver implements DidMethodDriver {

    /** DID method name supported by this driver. */
    public static final String METHOD = "web";

    private static final String DID_DOCUMENT_FILENAME = "did.json";

    private static final String WELL_KNOWN = ".well-known";

    private static final String DEFAULT_SCHEME = "https";

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private final String scheme;

    /**
     * Creates a driver using default HTTPS transport and a fresh {@link ObjectMapper}.
     */
    public DidWebDriver() {
        this(HttpClient.newBuilder().connectTimeout(DEFAULT_TIMEOUT).build(), new ObjectMapper());
    }

    /**
     * Creates a driver with the given HTTP client and object mapper using the HTTPS scheme.
     *
     * @param httpClient   the HTTP client used to download DID Documents.
     * @param objectMapper the object mapper used to parse DID Documents.
     */
    public DidWebDriver(HttpClient httpClient, ObjectMapper objectMapper) {
        this(httpClient, objectMapper, DEFAULT_SCHEME);
    }

    /**
     * Creates a driver with a configurable URL scheme. Intended for testing against plain HTTP servers.
     *
     * @param httpClient   the HTTP client used to download DID Documents.
     * @param objectMapper the object mapper used to parse DID Documents.
     * @param scheme       the URL scheme to use (e.g. {@code https} or {@code http}).
     */
    public DidWebDriver(HttpClient httpClient, ObjectMapper objectMapper, String scheme) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.scheme = scheme;
    }

    @Override
    public boolean supports(String method) {
        return METHOD.equals(method);
    }

    /**
     * Transforms a {@code did:web} identifier into the URL of its DID Document.
     *
     * @param did the parsed {@code did:web} identifier.
     * @return the DID Document URL.
     */
    public String getDidDocumentUrl(Did did) {
        String[] segments = did.getMethodSpecificId().split(":");
        StringJoiner pathJoiner = new StringJoiner("/");

        String host = URLDecoder.decode(segments[0], StandardCharsets.UTF_8);
        StringBuilder url = new StringBuilder(scheme).append("://").append(host);

        if (segments.length == 1) {
            url.append('/').append(WELL_KNOWN);
        } else {
            for (int i = 1; i < segments.length; i++) {
                pathJoiner.add(URLDecoder.decode(segments[i], StandardCharsets.UTF_8));
            }
            url.append('/').append(pathJoiner);
        }

        url.append('/').append(DID_DOCUMENT_FILENAME);
        return url.toString();
    }

    @Override
    public DidResolutionResult resolve(Did did) {
        if (!supports(did.getMethod())) {
            return DidResolutionResult.error(DidResolutionMetadata.ERROR_METHOD_NOT_SUPPORTED,
                "DID method '" + did.getMethod() + "' is not supported by the did:web driver.");
        }

        String documentUrl = getDidDocumentUrl(did);
        log.debug("Resolving did:web {} via {}", did, documentUrl);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(documentUrl))
            .timeout(DEFAULT_TIMEOUT)
            .header("Accept", "application/did+json, application/json")
            .GET()
            .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return DidResolutionResult.error(DidResolutionMetadata.ERROR_NOT_FOUND,
                "Could not download DID Document from " + documentUrl + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DidResolutionResult.error(DidResolutionMetadata.ERROR_INTERNAL,
                "DID Document download was interrupted.");
        }

        if (response.statusCode() == 404) {
            return DidResolutionResult.error(DidResolutionMetadata.ERROR_NOT_FOUND,
                "DID Document not found at " + documentUrl);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return DidResolutionResult.error(DidResolutionMetadata.ERROR_NOT_FOUND,
                "Unexpected HTTP status " + response.statusCode() + " while resolving " + documentUrl);
        }

        try {
            DidDocument didDocument = objectMapper.readValue(response.body(), DidDocument.class);
            return DidResolutionResult.success(didDocument);
        } catch (IOException e) {
            return DidResolutionResult.error(DidResolutionMetadata.ERROR_INTERNAL,
                "Could not parse DID Document from " + documentUrl + ": " + e.getMessage());
        }
    }
}
