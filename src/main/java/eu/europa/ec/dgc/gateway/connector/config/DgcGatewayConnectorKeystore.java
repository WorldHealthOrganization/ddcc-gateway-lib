/*-
 * ---license-start
 * EU Digital Green Certificate Gateway Service / dgc-lib
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
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

package eu.europa.ec.dgc.gateway.connector.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty("dgc.gateway.connector.enabled")
@Slf4j
public class DgcGatewayConnectorKeystore {

    private final DgcGatewayConnectorConfigProperties dgcConfigProperties;

    /**
     * Creates a KeyStore instance with key for DGC Certificate Upload (Upload Certificate).
     *
     * @return KeyStore Instance
     * @throws KeyStoreException        if no implementation for the specified type found
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     */
    @Bean
    @Qualifier("upload")
    @ConditionalOnProperty("dgc.gateway.connector.upload-key-store.path")
    public KeyStore uploadKeyStore() throws KeyStoreException,
        CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("JKS");

        loadKeyStore(
            keyStore,
            dgcConfigProperties.getUploadKeyStore().getPath(),
            dgcConfigProperties.getUploadKeyStore().getPassword());

        return keyStore;
    }

    /**
     * Creates a KeyStore instance with keys for DGC TrustAnchor.
     *
     * @return KeyStore Instance
     * @throws KeyStoreException        if no implementation for the specified type found
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     */
    @Bean
    @Qualifier("trustAnchor")
    @ConditionalOnProperty("dgc.gateway.connector.trust-anchor.path")
    public KeyStore trustAnchorKeyStore() throws KeyStoreException,
        CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("JKS");

        loadKeyStore(
            keyStore,
            dgcConfigProperties.getTrustAnchor().getPath(),
            dgcConfigProperties.getTrustAnchor().getPassword());

        return keyStore;
    }

    private void loadKeyStore(KeyStore keyStore, String path, char[] password)
        throws CertificateException, NoSuchAlgorithmException {
        try {

            InputStream stream;

            if (path.startsWith("$ENV:")) {
                String env = path.substring(6);
                String b64 = System.getenv(env);
                stream = new ByteArrayInputStream(Base64.getDecoder().decode(b64));
            } else {
                stream = new FileInputStream(ResourceUtils.getFile(path));
            }

            if (stream.available() > 0) {
                keyStore.load(stream, password);
                stream.close();
            } else {
                keyStore.load(null);
                log.info("Could not load Keystore {}", path);
            }
        } catch (IOException e) {
            log.info("Could not load Keystore {}", path);
        }
    }
}
