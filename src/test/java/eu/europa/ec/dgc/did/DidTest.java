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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DidTest {

    @Test
    void shouldParseDidWebWithPathSegments() {
        Did did = Did.parse(
            "did:web:raw.githubusercontent.com:WorldHealthOrganization:tng-participants-dev:main:XXA:onboarding:DDCC:UP");

        Assertions.assertEquals("web", did.getMethod());
        Assertions.assertEquals(
            "raw.githubusercontent.com:WorldHealthOrganization:tng-participants-dev:main:XXA:onboarding:DDCC:UP",
            did.getMethodSpecificId());
        Assertions.assertNull(did.getPath());
        Assertions.assertNull(did.getQuery());
        Assertions.assertNull(did.getFragment());
    }

    @Test
    void shouldParseDidUrlComponents() {
        Did did = Did.parse("did:web:example.com:alice/path/to/resource?versionId=1#key-1");

        Assertions.assertEquals("web", did.getMethod());
        Assertions.assertEquals("example.com:alice", did.getMethodSpecificId());
        Assertions.assertEquals("/path/to/resource", did.getPath());
        Assertions.assertEquals("versionId=1", did.getQuery());
        Assertions.assertEquals("key-1", did.getFragment());
        Assertions.assertEquals("did:web:example.com:alice", did.getDid());
    }

    @Test
    void shouldRejectNullOrBlank() {
        Assertions.assertThrows(DidResolutionException.class, () -> Did.parse(null));
        Assertions.assertThrows(DidResolutionException.class, () -> Did.parse("   "));
    }

    @Test
    void shouldRejectNonDidScheme() {
        Assertions.assertThrows(DidResolutionException.class, () -> Did.parse("urn:web:example.com"));
    }

    @Test
    void shouldRejectMissingMethodSpecificId() {
        Assertions.assertThrows(DidResolutionException.class, () -> Did.parse("did:web:"));
    }

    @Test
    void shouldRejectInvalidMethodName() {
        Assertions.assertThrows(DidResolutionException.class, () -> Did.parse("did:WEB:example.com"));
    }
}
