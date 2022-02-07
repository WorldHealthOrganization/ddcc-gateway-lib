/*-
 * ---license-start
 * WHO Digital Documentation Covid Certificate Gateway Service / ddcc-gateway-lib
 * ---
 * Copyright (C) 2022 T-Systems International GmbH and all other contributors
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

package eu.europa.ec.dgc.gateway.connector.dto;

// Type of hash for revocation lists
public enum RevocationHashTypeDto {

    // The hash is calculated over the UCI string encoded in UTF-8 and converted to a byte array.
    UCI,

    // The hash is calculated over the bytes of the COSE_SIGN1 signature from the CWT
    SIGNATURE,

    // The CountryCode encoded as a UTF-8 string concatenated with the UCI encoded with a
    // UTF-8 string. This is then converted to a byte array and used as input to the hash function.")
    COUNTRYCODEUCI

}
