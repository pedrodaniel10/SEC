package pt.ulisboa.tecnico.sec.server.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.log4j.Logger;
import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public final class CcUtils {

    private static final Logger logger = Logger.getLogger(CcUtils.class);
    private static long p11Session;
    private static PublicKey notaryPublicKey;
    private static PKCS11 pkcs11;

    public static void init() throws PteidException, ClassNotFoundException, NoSuchMethodException,
                                      InvocationTargetException, IllegalAccessException, PKCS11Exception,
                                      CertificateException {

        System.loadLibrary("pteidlibj");
        pteid.Init("");
        pteid.SetSODChecking(false);

        String osName = System.getProperty("os.name");
        String libName = "libpteidpkcs11.so";

        if (osName.contains("Windows")) {
            libName = "pteidpkcs11.dll";
        } else if (osName.contains("Mac")) {
            libName = "libpteidpkcs11.dylib";
        }

        Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance",
            new Class[]{String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
        pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, "C_GetFunctionList", null, false});

        /* Open the PKCS11 session
         * Opens a session between an application and a token in a particular slot. */
        p11Session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

        // Token login - C_Login logs a user into a token - requires authentication pin.
        pkcs11.C_Login(p11Session, 1, null);

        notaryPublicKey = CcUtils.generateNotaryPublicKey();
    }

    private static PublicKey generateNotaryPublicKey() throws CertificateException, PteidException {
        PTEID_Certif[] certificates = pteid.GetCertificates();
        byte[] certificateBytes = certificates[0].certif; //certificado 0 é o de autenticacao

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateBytes);
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);

        return certificate.getPublicKey();
    }

    public static long getP11Session() {
        return p11Session;
    }

    public static PublicKey getNotaryPublicKey() {
        return notaryPublicKey;
    }

    public static byte[] signMessage(String... message) throws PKCS11Exception {
        String messageConcat = String.join("", message);

        // Get available keys
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

        pkcs11.C_FindObjectsInit(p11Session, attributes); //Initiate search for private keys
        long[] keyHandles = pkcs11.C_FindObjects(p11Session, 5);
        /* keyHandles will have the authentication and signature keys - keyHandles[0] = auth_key and keyHandles[1] = sign_key */
        /* In the project, we will only use the authentication key, also to sign */

        long signatureKey = keyHandles[0];
        pkcs11.C_FindObjectsFinal(p11Session);

        // Initialize the signature method
        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA256_RSA_PKCS;
        mechanism.pParameter = null;
        pkcs11.C_SignInit(p11Session, mechanism, signatureKey);
        return pkcs11.C_Sign(p11Session, messageConcat.getBytes(Charset.forName("UTF-8")));
    }
}
