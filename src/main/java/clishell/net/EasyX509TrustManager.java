package clishell.net;

import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *  TODO:   Refactor refactor refactor alert!
 *  NOTE:   This class was copied from AppMonitor on Aug 9 2011
 *          it should be refactored out from both places!
 *
 */
public class EasyX509TrustManager implements X509TrustManager {


    //
    //  Private instance data
    //

    /** the underlying trust manager (system default) */
    private X509TrustManager mStandardTrustManager = null;

    /** true if we're supposed to print interesting stuff while we work */
    private boolean mVerboseFlag;


    //
    //  Public class methods
    //

    /**
     * Run a <code>URLConnection</code> object through this method in order to
     * allow it to accept self-signed SSL certificates.
     * @param urlConnection the <code>URLConnection</code> object to modify
     * @throws NoSuchAlgorithmException thrown if SSL (TLS) algorithm not configured properly
     * or <code>EasyX509TrustManager</code> object could not be instantiated
     * @throws KeyStoreException <code>EasyX509TrustManager</code> object could not be instantiated
     * @throws KeyManagementException <code>SSLContext</code> could not be initialized
     */
    public static void allowSelfsignedSSLCertificates(URLConnection urlConnection)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        if (!(urlConnection instanceof HttpsURLConnection)) {
            // nothing to do if this connection is not an HTTPS connection
            return;
        }

        SSLContext context = SSLContext.getInstance("TLS"); // "SSL"?
        context.init(null, new TrustManager[] {new EasyX509TrustManager(null)}, null);
        SSLSocketFactory factory = context.getSocketFactory();

        ((HttpsURLConnection) urlConnection).setSSLSocketFactory(factory);

    }


    //
    //  Public instance constructors
    //

    /**
     * @param keystore
     * @throws NoSuchAlgorithmException couldn't get default algorithm
     * from configured <code>TrustManagerFactory</code>, or there were
     * no configured trust managers returned by it
     * @throws KeyStoreException problem initializing the configured
     * <code>TrustManagerFactory</code> object
     */
    public EasyX509TrustManager(KeyStore keystore)
            throws NoSuchAlgorithmException, KeyStoreException {

        TrustManagerFactory factory = TrustManagerFactory
            .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        factory.init(keystore);

        TrustManager[] trustmanagers = factory.getTrustManagers();

        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }

        mStandardTrustManager = (X509TrustManager) trustmanagers[0];
    }


    //
    //  Public instance methods
    //

    /**
     * @param flag true if to set "verbose" mode on, wherein class
     * will print interesting stuff to stdout as it works
     */
    public void setVerboseFlag(boolean flag) {
        mVerboseFlag = flag;
    }


    //
    //  Implementation of javax.net.ssl.X509TrustManager interface
    //

    /**
     * @see X509TrustManager#checkClientTrusted(X509Certificate[], String)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType)
            throws CertificateException {

        mStandardTrustManager.checkClientTrusted(certificates,authType);
    }

    /**
     * @see X509TrustManager#checkServerTrusted(X509Certificate[], String)
     */
    public void checkServerTrusted(X509Certificate[] certificates,String authType) throws CertificateException {

        if (certificates != null && mVerboseFlag) {
            System.out.println("Server certificate chain:");
            for (int i = 0; i < certificates.length; i++) {
                System.out.println("X509Certificate[" + i + "]=" + certificates[i]);
            }
        }

        if (certificates != null && certificates.length == 1) {
            certificates[0].checkValidity();
            return;
        }

        mStandardTrustManager.checkServerTrusted(certificates,authType);
    }

    /**
     * @see X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return mStandardTrustManager.getAcceptedIssuers();
    }

}

