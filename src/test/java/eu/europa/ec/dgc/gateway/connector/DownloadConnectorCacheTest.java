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

package eu.europa.ec.dgc.gateway.connector;

import eu.europa.ec.dgc.gateway.connector.client.DgcGatewayConnectorRestClient;
import eu.europa.ec.dgc.gateway.connector.dto.CertificateTypeDto;
import eu.europa.ec.dgc.testdata.DgcTestKeyStore;
import eu.europa.ec.dgc.utils.CertificateUtils;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest(properties = {
    "dgc.gateway.connector.max-cache-age=2"
})
@Slf4j
class DownloadConnectorCacheTest {

    @MockBean
    DgcGatewayConnectorRestClient restClientMock;

    @Autowired
    DgcGatewayDownloadConnector connector;

    @Autowired
    CertificateUtils certificateUtils;

    @Autowired
    DgcTestKeyStore testKeyStore;

    @Test
    void testDownloadCache() throws InterruptedException {
        when(restClientMock.getTrustList(CertificateTypeDto.CSCA))
            .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        when(restClientMock.getTrustList(CertificateTypeDto.DSC))
            .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        when(restClientMock.getTrustList(CertificateTypeDto.UPLOAD))
            .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        when(restClientMock.downloadTrustedIssuers(any()))
            .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        when(restClientMock.downloadTrustedReferences(any()))
            .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        connector.getTrustedCertificates();

        verify(restClientMock).getTrustList(CertificateTypeDto.CSCA);
        verify(restClientMock).getTrustList(CertificateTypeDto.DSC);
        verify(restClientMock).getTrustList(CertificateTypeDto.UPLOAD);

        clearInvocations(restClientMock);

        connector.getTrustedCertificates();

        verify(restClientMock, never()).getTrustList(CertificateTypeDto.CSCA);
        verify(restClientMock, never()).getTrustList(CertificateTypeDto.DSC);
        verify(restClientMock, never()).getTrustList(CertificateTypeDto.UPLOAD);

        // Wait 2 seconds to invalidate cache
        Thread.sleep(2500);

        connector.getTrustedCertificates();

        verify(restClientMock).getTrustList(CertificateTypeDto.CSCA);
        verify(restClientMock).getTrustList(CertificateTypeDto.DSC);
        verify(restClientMock).getTrustList(CertificateTypeDto.UPLOAD);

        clearInvocations(restClientMock);
    }
}
