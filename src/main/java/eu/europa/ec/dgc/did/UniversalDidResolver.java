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
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Local implementation of the DIF Universal Resolver that dispatches DID resolution to registered method drivers.
 *
 * <p>By default a {@link DidWebDriver} is registered so that {@code did:web} identifiers can be resolved
 * out of the box. Additional {@link DidMethodDriver} implementations can be supplied to support further methods.
 */
@Slf4j
public class UniversalDidResolver implements DidResolver {

    private final List<DidMethodDriver> drivers;

    /**
     * Creates a resolver with the default set of drivers (currently {@code did:web}).
     */
    public UniversalDidResolver() {
        this(List.of(new DidWebDriver()));
    }

    /**
     * Creates a resolver with the given set of method drivers.
     *
     * @param drivers the method drivers to register.
     */
    public UniversalDidResolver(List<DidMethodDriver> drivers) {
        this.drivers = new ArrayList<>(drivers);
    }

    /**
     * Registers an additional method driver.
     *
     * @param driver the driver to register.
     */
    public void registerDriver(DidMethodDriver driver) {
        this.drivers.add(driver);
    }

    @Override
    public DidResolutionResult resolve(String didString) {
        Did did;
        try {
            did = Did.parse(didString);
        } catch (DidResolutionException e) {
            log.debug("Failed to parse DID {}: {}", didString, e.getMessage());
            return DidResolutionResult.error(DidResolutionMetadata.ERROR_INVALID_DID, e.getMessage());
        }

        for (DidMethodDriver driver : drivers) {
            if (driver.supports(did.getMethod())) {
                return driver.resolve(did);
            }
        }

        return DidResolutionResult.error(DidResolutionMetadata.ERROR_METHOD_NOT_SUPPORTED,
            "No driver registered for DID method '" + did.getMethod() + "'.");
    }
}
