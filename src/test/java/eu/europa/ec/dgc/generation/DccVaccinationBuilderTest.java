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

package eu.europa.ec.dgc.generation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class DccVaccinationBuilderTest {
    @Test
    void genTestJson() {
        DccVaccinationBuilder dccVaccinationBuilder = new DccVaccinationBuilder();
        dccVaccinationBuilder.gn("Artur").fn("Trzewik").gnt("ARTUR").fnt("TRZEWIK").dob(LocalDate.parse("1973-01-01"));
        dccVaccinationBuilder.dgci("URN:UVCI:01:OS:B5921A35D6A0D696421B3E2462178297I")
            .country("DE")
            .certificateIssuer("Dr Who")
            .doseNumber(1)
            .totalSeriesOfDoses(2)
            .dateOfVaccination(LocalDateTime.now())
            .vaccineOrProphylaxis("1119349007")
            .medicalProduct("EU/1/20/1507")
            .marketingAuthorization("ORG-100001699");
        String jsonString = dccVaccinationBuilder.toJsonString();
        assertNotNull(jsonString);
        System.out.println(jsonString);
    }
}
