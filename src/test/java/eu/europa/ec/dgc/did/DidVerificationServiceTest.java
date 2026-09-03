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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DidVerificationServiceTest {

    /**
     * Public key (Base64 encoded X.509 SubjectPublicKeyInfo) of
     * {@code did:web:raw.githubusercontent.com:WorldHealthOrganization:tng-participants-dev:main:WHO:signing:DID}.
     */
    private static final String WHO_DID_SIGNING_KEY =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEmB9T6oJ57ZKlC1f150+34M3JkxN22Ld1BQVPqJfgH6m9bsaYKfl43NuN2Mjw9ha9"
            + "qei9Yghiiq/RkT+W+4PZ3w==";

    /** Public key of an unrelated participant, used for the negative test. */
    private static final String OTHER_KEY =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE8qxYlgLQPfDxo/M7d3z/jMCJ2iqnGAH8yFyAxCbkG+XVvr8FqXjDMVU4l8mtwPHJ"
            + "AP10B1IIcU3jKLSCkFxf0g==";

    private DidVerificationService service;

    private String didDocument;

    @BeforeEach
    void setUp() throws Exception {
        // No remote fallback: everything required to verify has to be bundled with the library.
        service = new DidVerificationService(new DidContextDocumentLoader(null));

        try (InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream("did/tng-dev-trustlist-dcc-and.json")) {
            Assertions.assertNotNull(inputStream);
            didDocument = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void shouldVerifyValidProof() {
        Assertions.assertTrue(service.verify(didDocument, WHO_DID_SIGNING_KEY));
    }

    @Test
    void shouldVerifyValidProofWithParsedKey() {
        PublicKey publicKey = DidVerificationService.parsePublicKey(WHO_DID_SIGNING_KEY);
        Assertions.assertTrue(service.verify(didDocument, publicKey));
    }

    @Test
    void shouldRejectProofOfDifferentKey() {
        Assertions.assertFalse(service.verify(didDocument, OTHER_KEY));
    }

    @Test
    void shouldRejectTamperedDocument() {
        String tampered = didDocument.replace("\"crv\":\"P-256\"", "\"crv\":\"P-384\"");
        Assertions.assertNotEquals(didDocument, tampered);
        Assertions.assertFalse(service.verify(tampered, WHO_DID_SIGNING_KEY));
    }

    @Test
    void shouldRejectTamperedSignature() {
        String tampered = didDocument.replace("..MEQCIERlP7", "..MEQCIERlP8");
        Assertions.assertNotEquals(didDocument, tampered);
        Assertions.assertFalse(service.verify(tampered, WHO_DID_SIGNING_KEY));
    }

    @Test
    void shouldRejectDocumentWithoutProof() {
        Assertions.assertFalse(service.verify("{\"id\":\"did:web:example.org\"}", WHO_DID_SIGNING_KEY));
    }

    @Test
    void shouldRejectInvalidInput() {
        Assertions.assertFalse(service.verify((String) null, WHO_DID_SIGNING_KEY));
        Assertions.assertFalse(service.verify("not-json", WHO_DID_SIGNING_KEY));
        Assertions.assertFalse(service.verify(didDocument, "not-a-key"));
    }

    @Test
    void shouldExposeSupportedAlgorithms() {
        Assertions.assertTrue(DidVerificationService.getSupportedAlgorithms().contains("ES256"));
    }
}
