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

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.danubetech.keyformats.crypto.ByteVerifier;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import foundation.identity.jsonld.JsonLDException;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.LdProof;
import info.weboftrust.ldsignatures.verifier.JsonWebSignature2020LdVerifier;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

/**
 * Verifies {@code JsonWebSignature2020} DID proof signatures using {@code ld-signatures-java}.
 */
@Slf4j
public class DidVerificationService {

    private static final Map<String, String> JWS_ALGORITHMS = Map.ofEntries(
        Map.entry("ES256", "SHA256withECDSA"),
        Map.entry("ES256K", "SHA256withECDSA"),
        Map.entry("ES384", "SHA384withECDSA"),
        Map.entry("ES512", "SHA512withECDSA"),
        Map.entry("RS256", "SHA256withRSA"),
        Map.entry("RS384", "SHA384withRSA"),
        Map.entry("RS512", "SHA512withRSA"),
        Map.entry("PS256", "SHA256withRSAandMGF1"),
        Map.entry("PS384", "SHA384withRSAandMGF1"),
        Map.entry("PS512", "SHA512withRSAandMGF1"),
        Map.entry("EdDSA", "Ed25519"));

    private final DocumentLoader documentLoader;

    /**
     * Creates a service which resolves JSON-LD contexts from classpath with cached remote fallback.
     */
    public DidVerificationService() {
        this(new DidContextDocumentLoader());
    }

    /**
     * Creates a service with a custom JSON-LD document loader.
     *
     * @param documentLoader loader for JSON-LD context resolution.
     */
    public DidVerificationService(DocumentLoader documentLoader) {
        if (documentLoader == null) {
            throw new IllegalArgumentException("documentLoader must not be null.");
        }
        this.documentLoader = documentLoader;
    }

    /**
     * Verifies a DID document proof.
     *
     * @param didDocumentJson did document including {@code proof}.
     * @param publicKey signer key as PEM or Base64 SPKI.
     * @return {@code true} if valid, else {@code false}.
     */
    public boolean verify(String didDocumentJson, String publicKey) {
        try {
            return verify(didDocumentJson, parsePublicKey(publicKey));
        } catch (IllegalArgumentException e) {
            log.debug("Failed to parse provided public key: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies a DID document proof.
     *
     * @param didDocumentJson did document including {@code proof}.
     * @param publicKey signer key.
     * @return {@code true} if valid, else {@code false}.
     */
    public boolean verify(String didDocumentJson, PublicKey publicKey) {
        if (didDocumentJson == null || didDocumentJson.isBlank() || publicKey == null) {
            return false;
        }

        try {
            JsonLDObject jsonLdObject = JsonLDObject.fromJson(didDocumentJson);
            jsonLdObject.setDocumentLoader(documentLoader);
            return verify(jsonLdObject, publicKey);
        } catch (RuntimeException e) {
            log.debug("Provided DID document is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies a DID document proof.
     *
     * @param didDocument parsed DID document including {@code proof}.
     * @param publicKey signer key.
     * @return {@code true} if valid, else {@code false}.
     */
    public boolean verify(JsonLDObject didDocument, PublicKey publicKey) {
        if (didDocument == null || publicKey == null) {
            return false;
        }

        try {
            LdProof proof = LdProof.getFromJsonLDObject(didDocument);
            if (proof == null || proof.getJws() == null || proof.getJws().isBlank()) {
                log.debug("DID document does not contain a proof jws.");
                return false;
            }

            String jwsAlgorithm = readAndValidateJwsAlgorithm(proof.getJws());
            if (jwsAlgorithm == null) {
                return false;
            }

            ByteVerifier byteVerifier = new DerAwareByteVerifier(publicKey, jwsAlgorithm);
            JsonWebSignature2020LdVerifier verifier = new JsonWebSignature2020LdVerifier(byteVerifier);
            return verifier.verify(didDocument, proof);

        } catch (GeneralSecurityException | JsonLDException | IOException | JOSEException | ParseException e) {
            log.debug("DID proof verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses a public key as PEM or Base64 encoded SPKI.
     *
     * @param publicKey key string.
     * @return parsed key.
     */
    public static PublicKey parsePublicKey(String publicKey) {
        if (publicKey == null || publicKey.isBlank()) {
            throw new IllegalArgumentException("publicKey must not be empty.");
        }

        String base64 = publicKey
            .replaceAll("-----(BEGIN|END)[^-]*-----", "")
            .replaceAll("\\s", "");

        try {
            byte[] encoded = Base64.getDecoder().decode(base64);
            return new JcaPEMKeyConverter()
                .setProvider(new BouncyCastleProvider())
                .getPublicKey(SubjectPublicKeyInfo.getInstance(encoded));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse the provided public key.", e);
        }
    }

    /**
     * Returns supported JWS alg values.
     *
     * @return supported algorithms.
     */
    public static List<String> getSupportedAlgorithms() {
        return List.copyOf(JWS_ALGORITHMS.keySet());
    }

    private static String readAndValidateJwsAlgorithm(String jws) throws JOSEException, ParseException {
        String[] parts = jws.split("\\.", -1);
        if (parts.length != 3 || !parts[1].isEmpty()) {
            log.debug("Expected detached JWS with empty payload section.");
            return null;
        }

        JWSHeader header = JWSObject.parse(jws).getHeader();
        if (header.isBase64URLEncodePayload()) {
            log.debug("Only b64=false detached JWS is supported.");
            return null;
        }

        Set<String> criticalHeaders = header.getCriticalParams();
        if (criticalHeaders != null && !criticalHeaders.isEmpty() && !criticalHeaders.contains("b64")) {
            log.debug("Unsupported critical headers in proof JWS.");
            return null;
        }

        String algorithm = header.getAlgorithm() == null ? null : header.getAlgorithm().getName();
        if (algorithm == null || !JWS_ALGORITHMS.containsKey(algorithm)) {
            log.debug("Unsupported JWS algorithm '{}'.", algorithm);
            return null;
        }

        return algorithm;
    }

    private static final class DerAwareByteVerifier extends ByteVerifier {

        private final PublicKey publicKey;
        private final String signatureAlgorithm;

        private DerAwareByteVerifier(PublicKey publicKey, String jwsAlgorithm) {
            super(jwsAlgorithm);
            this.publicKey = publicKey;
            this.signatureAlgorithm = JWS_ALGORITHMS.get(jwsAlgorithm);
        }

        @Override
        protected boolean verify(byte[] content, byte[] signature) throws GeneralSecurityException {
            if (verifySignature(content, signature)) {
                return true;
            }

            try {
                byte[] converted = convertRawEcdsaSignature(signature);
                return converted != null && verifySignature(content, converted);
            } catch (IOException e) {
                throw new GeneralSecurityException("Failed to convert raw ECDSA signature to DER.", e);
            }
        }

        private boolean verifySignature(byte[] content, byte[] signature) throws GeneralSecurityException {
            Signature verifier = Signature.getInstance(signatureAlgorithm, new BouncyCastleProvider());
            verifier.initVerify(publicKey);
            verifier.update(content);
            return verifier.verify(signature);
        }

        private byte[] convertRawEcdsaSignature(byte[] signature) throws IOException {
            if (!(publicKey instanceof ECPublicKey ecPublicKey)) {
                return null;
            }

            int fieldSize = (ecPublicKey.getParams().getOrder().bitLength() + 7) / 8;
            if (signature.length != fieldSize * 2) {
                return null;
            }

            BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature, 0, fieldSize));
            BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, fieldSize, signature.length));

            ASN1EncodableVector vector = new ASN1EncodableVector();
            vector.add(new ASN1Integer(r));
            vector.add(new ASN1Integer(s));
            return new DERSequence(vector).getEncoded();
        }
    }
}
