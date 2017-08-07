package crypto;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Date;

/**
 * Created by jon on 8/4/17.
 */
public class CryptoUtils {

    public static final String ROOT_ALIAS = "root";
    public static final String INTERMEDIATE_ALIAS = "intermediate";
    public static final String END_ENTITY_ALIAS = "end";

    public static final String ROOT_KEY = "root.key.pem";
    public static final String ROOT_CERT = "root.cert.pem";

    public static final String SERVER_NAME = "server";
    public static final char[] SERVER_PASSWORD = SERVER_NAME.toCharArray();

    public static final String CLIENT_NAME = "client";
    public static final char[] CLIENT_PASSWORD = CLIENT_NAME.toCharArray();

    public static final String TRUST_STORE_NAME = "trustStore";
    public static final char[] TRUST_STORE_PASSWORD = TRUST_STORE_NAME.toCharArray();

    public static final long VALIDITY_PERIOD = 14 * (24 * 60 * 60) * 1000L;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        // 1) new keys -----------
        if (Security.getProvider("BC") != null) {
            System.out.println("BC is installed");
        }

        X500PrivateCredential rootCredential = createRootCredential();
        X500PrivateCredential interCredential = createIntermediateCredential(rootCredential.getPrivateKey(), rootCredential.getCertificate());
        X500PrivateCredential endCredential = createEndEntityCredential(interCredential.getPrivateKey(), interCredential.getCertificate());

        // client credentials
        KeyStore clientKeyStore = KeyStore.getInstance("JKS");
        clientKeyStore.load(null, null);
        clientKeyStore.setKeyEntry(CLIENT_NAME, endCredential.getPrivateKey(), CLIENT_PASSWORD,
                new Certificate[]{endCredential.getCertificate(),
                        interCredential.getCertificate(),
                        rootCredential.getCertificate()});
        clientKeyStore.store(new FileOutputStream(CLIENT_NAME + ".jks"), CLIENT_PASSWORD);

        // trust store for both client and server
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);
        trustStore.setCertificateEntry(SERVER_NAME, rootCredential.getCertificate());
        trustStore.store(new FileOutputStream(TRUST_STORE_NAME + ".jks"), TRUST_STORE_PASSWORD);

        // server credentials
        KeyStore serverKeyStore = KeyStore.getInstance("JKS");
        serverKeyStore.load(null, null);
        serverKeyStore.setKeyEntry(SERVER_NAME, rootCredential.getPrivateKey(), SERVER_PASSWORD,
                new Certificate[] {rootCredential.getCertificate()});
        serverKeyStore.store(new FileOutputStream(SERVER_NAME + ".jks"), SERVER_PASSWORD);

        // 2) Existing keys --------
        // If you have existing keys and certs, here is an example of how to set up SSL
//        createTrustStore();
    }

    /**
     * Create a random RSA key pair
     *
     * @return
     * @throws Exception
     */
    public static KeyPair generateRSAKeyPair(int keySize) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(keySize, new SecureRandom());

        return keyPairGenerator.generateKeyPair();
    }

    public static X500PrivateCredential createRootCredential() throws Exception {
        KeyPair rootPair = generateRSAKeyPair(4096);
        X509Certificate rootCert = buildRootCert(rootPair);

        JcaPKCS8Generator generator = new JcaPKCS8Generator(rootPair.getPrivate(), null);
        PemObject pemObject = generator.generate();
        writePEM(pemObject, ROOT_KEY);
        writePEM(rootCert, ROOT_CERT);

        return new X500PrivateCredential(rootCert, rootPair.getPrivate(), ROOT_ALIAS);
    }

    public static X500PrivateCredential createIntermediateCredential(PrivateKey caKey, X509Certificate caCert) throws Exception {
        KeyPair interPair = generateRSAKeyPair(2048);
        X509Certificate interCert = buildIntermediateCert(interPair.getPublic(), caKey, caCert);

        return new X500PrivateCredential(interCert, interPair.getPrivate(), INTERMEDIATE_ALIAS);
    }

    public static X500PrivateCredential createEndEntityCredential(PrivateKey caKey, X509Certificate caCert) throws Exception {
        KeyPair endPair = generateRSAKeyPair(2048);
        X509Certificate endCert = buildEndEntityCert(endPair.getPublic(), caKey, caCert);

        return new X500PrivateCredential(endCert, endPair.getPrivate(), END_ENTITY_ALIAS);
    }

    public static X509Certificate buildRootCert(KeyPair keyPair) throws Exception {
        X509v1CertificateBuilder certBuilder = new JcaX509v1CertificateBuilder(
                new X500Name("CN=Test Root Certificate"),
                BigInteger.valueOf(System.currentTimeMillis()),
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
                new X500Name("CN=Test Root Certificate"),
                keyPair.getPublic());

        // self signed root cert with own private key
        ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(keyPair.getPrivate());

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(signer));
    }

    public static X509Certificate buildIntermediateCert(PublicKey intKey, PrivateKey caKey, X509Certificate caCert) throws Exception {
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                caCert.getSubjectX500Principal(),
                BigInteger.valueOf(1),
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
                new X500Principal("CN=Test CA Certificate"),
                intKey);

        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();

        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, extensionUtils.createAuthorityKeyIdentifier(caCert))
                .addExtension(Extension.subjectKeyIdentifier, false, extensionUtils.createSubjectKeyIdentifier(intKey))
                .addExtension(Extension.basicConstraints, true, new BasicConstraints(0))
                .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign));

        ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(caKey);

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(signer));
    }

    public static X509Certificate buildEndEntityCert(PublicKey entityKey, PrivateKey caKey, X509Certificate caCert) throws Exception {
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                caCert.getSubjectX500Principal(),
                BigInteger.valueOf(1),
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + VALIDITY_PERIOD),
                new X500Principal("CN=Test End Entity Certificate"),
                entityKey);

        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();

        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, extensionUtils.createAuthorityKeyIdentifier(caCert))
                .addExtension(Extension.subjectKeyIdentifier, false, extensionUtils.createSubjectKeyIdentifier(entityKey))
                .addExtension(Extension.basicConstraints, true, new BasicConstraints(false))
                .addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        ContentSigner signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(caKey);

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(signer));
    }

    public static void writePEM(Object object, String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        JcaPEMWriter jcaPEMWriter = null;
        try {
            jcaPEMWriter = new JcaPEMWriter(fileWriter);
            jcaPEMWriter.writeObject(object);
        } finally {
            jcaPEMWriter.close();
            fileWriter.close();
        }
    }

    /**
     * Reads root credentials from existing system
     * @return
     * @throws Exception
     */
    private static X500PrivateCredential readRootCredentials() throws Exception {
        File privateKeyFile = new File(System.getProperty("user.dir") + File.separator + "mykey.pem");
        PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
        Object privateKeyInfo = pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey = converter.getPrivateKey((PrivateKeyInfo) privateKeyInfo);

        CertificateFactory certficateFactory = CertificateFactory.getInstance("X.509");
        FileInputStream fileInputStream = new FileInputStream(System.getProperty("user.dir") + File.separator + "mycert.pem");
        X509Certificate cer = (X509Certificate) certficateFactory.generateCertificate(fileInputStream);

        return new X500PrivateCredential(cer, privateKey, ROOT_ALIAS);
    }

    private static void createTrustStore() throws Exception {
        X500PrivateCredential rootCredential = readRootCredentials();
        X500PrivateCredential interCredential = createIntermediateCredential(rootCredential.getPrivateKey(), rootCredential.getCertificate());
        X500PrivateCredential endCredential = createEndEntityCredential(interCredential.getPrivateKey(), interCredential.getCertificate());

        // client credentials
        KeyStore clientKeyStore = KeyStore.getInstance("JKS");
        clientKeyStore.load(null, null);
        clientKeyStore.setKeyEntry(CLIENT_NAME, endCredential.getPrivateKey(), CLIENT_PASSWORD,
                new Certificate[]{endCredential.getCertificate(),
                        interCredential.getCertificate(),
                        rootCredential.getCertificate()});
        clientKeyStore.store(new FileOutputStream(CLIENT_NAME + ".jks"), CLIENT_PASSWORD);

        // create trust store for server and client
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry(SERVER_NAME, rootCredential.getCertificate());
        keyStore.store(new FileOutputStream(TRUST_STORE_NAME + ".jks"), TRUST_STORE_PASSWORD);

        // server creds
        keyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry(SERVER_NAME, rootCredential.getPrivateKey(), SERVER_PASSWORD,
                new Certificate[] {
                rootCredential.getCertificate()
                });
        keyStore.store(new FileOutputStream(SERVER_NAME + ".jks"), SERVER_PASSWORD);
    }
}
