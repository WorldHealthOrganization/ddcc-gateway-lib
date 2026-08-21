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

import eu.europa.ec.dgc.did.model.DidResolutionResult;

/**
 * Driver capable of resolving DIDs of one specific DID method.
 */
public interface DidMethodDriver {

    /**
     * Checks whether this driver can resolve DIDs of the given method.
     *
     * @param method the DID method name (e.g. {@code web}).
     * @return {@code true} if this driver supports the method.
     */
    boolean supports(String method);

    /**
     * Resolves the given parsed DID into a DID resolution result.
     *
     * @param did the parsed DID to resolve.
     * @return the resolution result, possibly containing an error.
     */
    DidResolutionResult resolve(Did did);
}
