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

package eu.europa.ec.dgc.gateway.connector.mapper;

import eu.europa.ec.dgc.gateway.connector.dto.TrustListItemDto;
import eu.europa.ec.dgc.gateway.connector.dto.TrustedCertificateTrustListDto;
import eu.europa.ec.dgc.gateway.connector.model.TrustedCertificateTrustListItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrustedCertificateMapper {

    List<TrustListItemDto> mapToTrustList(List<TrustedCertificateTrustListDto> trustedCertificateTrustListDto);

    @Mapping(source = "group", target = "certificateType")
    @Mapping(source = "certificate", target = "rawData")
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "thumbprint", ignore = true)
    TrustListItemDto mapToTrustList(TrustedCertificateTrustListDto trustedCertificateTrustListDto);

    TrustedCertificateTrustListItem map(TrustedCertificateTrustListDto dto);

}
