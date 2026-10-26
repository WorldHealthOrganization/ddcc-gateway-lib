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

import eu.europa.ec.dgc.did.model.DidResolutionMetadata;
import eu.europa.ec.dgc.did.model.DidResolutionResult;

/**
 * Skeleton {@link DidMethodDriver} for the {@code did:key} method.
 *
 * <p>The driver is intentionally left without a resolution implementation. It declares the method it is
 * responsible for and returns a {@code methodNotSupported} result until the {@code did:key} algorithm is
 * implemented in {@link #resolve(Did)}.
 *
 * <p>Registering it is opt-in, so the default {@link UniversalDidResolver} behaviour stays unchanged:
 *
 * <pre>{@code
 * UniversalDidResolver resolver = new UniversalDidResolver();
 * resolver.registerDriver(new DidKeyDriver());
 * }</pre>
 */
public class DidKeyDriver implements DidMethodDriver {

    /** DID method name this driver is responsible for. */
    public static final String METHOD = "key";

    @Override
    public boolean supports(String method) {
        return METHOD.equals(method);
    }

    @Override
    public DidResolutionResult resolve(Did did) {
        return DidResolutionResult.error(DidResolutionMetadata.ERROR_METHOD_NOT_SUPPORTED,
            "Resolution of DID method '" + METHOD + "' is not implemented yet.");
    }
}
