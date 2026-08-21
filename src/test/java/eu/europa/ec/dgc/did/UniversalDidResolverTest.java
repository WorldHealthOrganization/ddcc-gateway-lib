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
import eu.europa.ec.dgc.did.model.DidResolutionMetadata;
import eu.europa.ec.dgc.did.model.DidResolutionResult;
import java.net.http.HttpClient;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UniversalDidResolverTest {

    private MockWebServer server;

    private UniversalDidResolver resolver;

    @BeforeEach
    void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        DidWebDriver driver = new DidWebDriver(HttpClient.newHttpClient(), new ObjectMapper(), "http");
        resolver = new UniversalDidResolver(List.of(driver));
    }

    @AfterEach
    void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    void shouldResolveViaWebDriver() {
        String host = server.getHostName() + "%3A" + server.getPort();
        String didValue = "did:web:" + host + ":user:alice";
        server.enqueue(new MockResponse().setBody("{\"id\": \"" + didValue + "\"}"));

        DidResolutionResult result = resolver.resolve(didValue);

        Assertions.assertFalse(result.isError());
        Assertions.assertEquals(didValue, result.getDidDocument().getId());
    }

    @Test
    void shouldReturnInvalidDidErrorForMalformedDid() {
        DidResolutionResult result = resolver.resolve("not-a-did");

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(DidResolutionMetadata.ERROR_INVALID_DID,
            result.getDidResolutionMetadata().getError());
    }

    @Test
    void shouldReturnMethodNotSupportedForUnknownMethod() {
        DidResolutionResult result = resolver.resolve("did:key:z6MkExample");

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(DidResolutionMetadata.ERROR_METHOD_NOT_SUPPORTED,
            result.getDidResolutionMetadata().getError());
    }

    @Test
    void shouldAllowRegisteringAdditionalDrivers() {
        UniversalDidResolver customResolver = new UniversalDidResolver(List.of());
        customResolver.registerDriver(new DidMethodDriver() {
            @Override
            public boolean supports(String method) {
                return "example".equals(method);
            }

            @Override
            public DidResolutionResult resolve(Did did) {
                return DidResolutionResult.error("custom", "handled by custom driver");
            }
        });

        DidResolutionResult result = customResolver.resolve("did:example:123");

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals("custom", result.getDidResolutionMetadata().getError());
    }

    @Test
    void shouldUseDefaultWebDriverConstructor() {
        UniversalDidResolver defaultResolver = new UniversalDidResolver();
        DidResolutionResult result = defaultResolver.resolve("did:unknownmethod:123");

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(DidResolutionMetadata.ERROR_METHOD_NOT_SUPPORTED,
            result.getDidResolutionMetadata().getError());
    }
}
