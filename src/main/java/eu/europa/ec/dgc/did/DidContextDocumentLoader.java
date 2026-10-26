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

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.LRUDocumentCache;
import com.apicatalog.jsonld.loader.SchemeRouter;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON-LD {@link DocumentLoader} which serves the contexts used by DDCC/TNG DID Documents from the classpath.
 *
 * <p>Resolving JSON-LD contexts is a mandatory step of the RDF canonicalization which in turn is the basis of
 * the {@code JsonWebSignature2020} LD-Proof verification. Loading them from the classpath keeps the verification
 * deterministic (a changed remote context would change the canonical form and therefore break every signature)
 * and allows the verification to run without network access.
 *
 * <p>Contexts which are not bundled are delegated to the fallback loader, which by default performs a cached
 * remote lookup. Pass {@code null} as fallback to disable remote lookups completely.
 */
public class DidContextDocumentLoader implements DocumentLoader {

    /** URI of the JSON-LD context which the LD-Proof options are canonicalized with. */
    public static final String SECURITY_V3_CONTEXT = "https://w3id.org/security/v3";

    private static final String RESOURCE_PATH = "did_contexts/";

    private static final Map<String, String> DEFAULT_MAPPINGS = Map.ofEntries(
        Map.entry(SECURITY_V3_CONTEXT, "security-v3-unstable.jsonld"),
        Map.entry("https://www.w3.org/ns/did/v1", "did_v1.json"),
        Map.entry("https://w3id.org/security/suites/jws-2020/v1", "jws-2020_v1.json"),
        Map.entry("https://smart.who.int/trust/tng-context/v1.jsonld", "v1.jsonld"),
        Map.entry("https://smart.who.int/trust/tng-context/v1-DEV.jsonld", "v1-dev.jsonld"),
        Map.entry("https://smart.who.int/trust/tng-context/v1-UAT.jsonld", "v1-uat.jsonld"));

    private final Map<String, String> mappings = new HashMap<>(DEFAULT_MAPPINGS);

    private final Map<String, Document> cache = new ConcurrentHashMap<>();

    private final DocumentLoader fallback;

    /**
     * Creates a loader which falls back to a cached remote lookup for non bundled contexts.
     */
    public DidContextDocumentLoader() {
        this(new LRUDocumentCache(SchemeRouter.defaultInstance(), 32));
    }

    /**
     * Creates a loader with a custom fallback.
     *
     * @param fallback the loader used for contexts which are not bundled, may be {@code null} to reject them.
     */
    public DidContextDocumentLoader(DocumentLoader fallback) {
        this.fallback = fallback;
    }

    /**
     * Registers an additional context which should be served from the classpath.
     *
     * @param contextUri       the URI as it appears in the {@code @context} of the DID Document.
     * @param classpathResource the classpath resource holding the context document.
     */
    public void registerContext(String contextUri, String classpathResource) {
        mappings.put(contextUri, classpathResource);
        cache.remove(contextUri);
    }

    @Override
    public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
        String key = url.toString();

        Document cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        String resource = mappings.get(key);
        if (resource != null) {
            Document document = loadFromClasspath(resource, url);
            cache.put(key, document);
            return document;
        }

        if (fallback == null) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED,
                "JSON-LD context '" + key + "' is not bundled and remote loading is disabled.");
        }

        return fallback.loadDocument(url, options);
    }

    private Document loadFromClasspath(String resource, URI url) throws JsonLdError {
        String location = resource.contains("/") ? resource : RESOURCE_PATH + resource;

        try (InputStream inputStream = DidContextDocumentLoader.class.getClassLoader()
            .getResourceAsStream(location)) {

            if (inputStream == null) {
                throw new JsonLdError(JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED,
                    "Bundled JSON-LD context resource '" + location + "' not found.");
            }

            JsonDocument document = JsonDocument.of(MediaType.JSON_LD, inputStream);
            document.setDocumentUrl(url);
            return document;

        } catch (JsonLdError e) {
            throw e;
        } catch (Exception e) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED, e);
        }
    }
}
