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
import eu.europa.ec.dgc.did.model.VerificationMethod;
import java.net.http.HttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DidWebDriverTest {

    private MockWebServer server;

    private DidWebDriver driver;

    @BeforeEach
    void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        driver = new DidWebDriver(HttpClient.newHttpClient(), new ObjectMapper(), "http");
    }

    @AfterEach
    void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    void shouldBuildUrlForHostOnlyDid() {
        DidWebDriver httpsDriver = new DidWebDriver();
        Did did = Did.parse("did:web:example.com");
        Assertions.assertEquals("https://example.com/.well-known/did.json", httpsDriver.getDidDocumentUrl(did));
    }

    @Test
    void shouldBuildUrlForDidWithPathSegments() {
        DidWebDriver httpsDriver = new DidWebDriver();
        Did did = Did.parse(
            "did:web:raw.githubusercontent.com:WorldHealthOrganization:tng-participants-dev:main:XXA:onboarding:DDCC:UP");
        Assertions.assertEquals(
            "https://raw.githubusercontent.com/WorldHealthOrganization/tng-participants-dev/main/XXA/onboarding/DDCC/UP/did.json",
            httpsDriver.getDidDocumentUrl(did));
    }

    @Test
    void shouldBuildUrlWithDecodedPort() {
        DidWebDriver httpsDriver = new DidWebDriver();
        Did did = Did.parse("did:web:example.com%3A3000:user:alice");
        Assertions.assertEquals("https://example.com:3000/user/alice/did.json", httpsDriver.getDidDocumentUrl(did));
    }

    @Test
    void shouldResolveDidDocument() throws Exception {
        String host = server.getHostName() + "%3A" + server.getPort();
        String didValue = "did:web:" + host + ":user:alice";
        String didDocument = "{\n"
            + "  \"@context\": [\"https://www.w3.org/ns/did/v1\"],\n"
            + "  \"id\": \"" + didValue + "\",\n"
            + "  \"verificationMethod\": [{\n"
            + "    \"id\": \"" + didValue + "#key-1\",\n"
            + "    \"type\": \"JsonWebKey2020\",\n"
            + "    \"controller\": \"" + didValue + "\",\n"
            + "    \"publicKeyJwk\": {\"kty\": \"EC\", \"crv\": \"P-256\", \"x\": \"abc\", \"y\": \"def\"}\n"
            + "  }]\n"
            + "}";
        server.enqueue(new MockResponse().setBody(didDocument).setHeader("Content-Type", "application/json"));

        DidResolutionResult result = driver.resolve(Did.parse(didValue));

        Assertions.assertFalse(result.isError());
        Assertions.assertNotNull(result.getDidDocument());
        Assertions.assertEquals(didValue, result.getDidDocument().getId());
        Assertions.assertEquals(1, result.getDidDocument().getVerificationMethod().size());
        VerificationMethod method = result.getDidDocument().getVerificationMethod().get(0);
        Assertions.assertEquals("JsonWebKey2020", method.getType());
        Assertions.assertEquals("EC", method.getPublicKeyJwk().get("kty"));
        Assertions.assertEquals(DidResolutionResult.CONTENT_TYPE_DID_LD_JSON,
            result.getDidResolutionMetadata().getContentType());

        RecordedRequest recordedRequest = server.takeRequest();
        Assertions.assertEquals("/user/alice/did.json", recordedRequest.getPath());
    }

    @Test
    void shouldReturnNotFoundErrorOnHttp404() {
        server.enqueue(new MockResponse().setResponseCode(404));
        String didValue = "did:web:" + server.getHostName() + "%3A" + server.getPort() + ":user:missing";

        DidResolutionResult result = driver.resolve(Did.parse(didValue));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(DidResolutionMetadata.ERROR_NOT_FOUND,
            result.getDidResolutionMetadata().getError());
        Assertions.assertNull(result.getDidDocument());
    }

    @Test
    void shouldReturnInternalErrorOnInvalidJson() {
        server.enqueue(new MockResponse().setBody("this-is-not-json"));
        String didValue = "did:web:" + server.getHostName() + "%3A" + server.getPort() + ":user:broken";

        DidResolutionResult result = driver.resolve(Did.parse(didValue));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(DidResolutionMetadata.ERROR_INTERNAL,
            result.getDidResolutionMetadata().getError());
    }

    @Test
    void shouldReturnMethodNotSupportedForOtherMethods() {
        DidResolutionResult result = driver.resolve(Did.parse("did:key:z6Mk"));

        Assertions.assertTrue(result.isError());
        Assertions.assertEquals(DidResolutionMetadata.ERROR_METHOD_NOT_SUPPORTED,
            result.getDidResolutionMetadata().getError());
    }

    @Test
    void shouldSupportOnlyWebMethod() {
        Assertions.assertTrue(driver.supports("web"));
        Assertions.assertFalse(driver.supports("key"));
    }
}
