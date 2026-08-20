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

import java.util.regex.Pattern;
import lombok.Getter;

/**
 * Immutable representation of a parsed Decentralized Identifier (DID) according to the W3C DID Core syntax.
 */
@Getter
public final class Did {

    private static final String SCHEME = "did";

    private static final Pattern METHOD_PATTERN = Pattern.compile("^[a-z0-9]+$");

    private final String did;

    private final String method;

    private final String methodSpecificId;

    private final String path;

    private final String query;

    private final String fragment;

    private Did(String did, String method, String methodSpecificId, String path, String query, String fragment) {
        this.did = did;
        this.method = method;
        this.methodSpecificId = methodSpecificId;
        this.path = path;
        this.query = query;
        this.fragment = fragment;
    }

    /**
     * Parses a DID (or DID URL) string into its components.
     *
     * @param didString the DID string to parse.
     * @return the parsed {@link Did}.
     * @throws DidResolutionException if the given string is not a syntactically valid DID.
     */
    public static Did parse(String didString) {
        if (didString == null || didString.isBlank()) {
            throw new DidResolutionException("DID must not be null or empty.");
        }

        String work = didString;
        String fragment = null;
        int hashIndex = work.indexOf('#');
        if (hashIndex >= 0) {
            fragment = work.substring(hashIndex + 1);
            work = work.substring(0, hashIndex);
        }

        String query = null;
        int queryIndex = work.indexOf('?');
        if (queryIndex >= 0) {
            query = work.substring(queryIndex + 1);
            work = work.substring(0, queryIndex);
        }

        String path = null;
        int slashIndex = work.indexOf('/');
        if (slashIndex >= 0) {
            path = work.substring(slashIndex);
            work = work.substring(0, slashIndex);
        }

        String[] parts = work.split(":", 3);
        if (parts.length < 3 || !SCHEME.equals(parts[0])) {
            throw new DidResolutionException("Invalid DID syntax: " + didString);
        }

        String method = parts[1];
        String methodSpecificId = parts[2];

        if (!METHOD_PATTERN.matcher(method).matches()) {
            throw new DidResolutionException("Invalid DID method name: " + method);
        }

        if (methodSpecificId.isEmpty()) {
            throw new DidResolutionException("Invalid DID, method specific identifier is empty: " + didString);
        }

        return new Did(work, method, methodSpecificId, path, query, fragment);
    }

    @Override
    public String toString() {
        return did;
    }
}
