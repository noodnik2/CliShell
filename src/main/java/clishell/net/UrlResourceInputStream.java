package clishell.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;

/**
 *  An input stream that will retrieve the network-based resource
 *  located at the specified URL, and handles authentication using
 *  the user-specified credentials (if any).
 */
public class UrlResourceInputStream extends InputStream {

    /** the underlying InputStream */
    private final InputStream mInputStream;

    /**
     * @param inputStreamUrl URL endpoint of network-based resource
     * @param userName default user name to use when authenticating URL request
     * (may be <code>null</code> if no authentication is to be used); will be
     * overridden if authentication credentials passed in URL
     * @param password default password to use when authenticating URL request
     * (may be <code>null</code> if no authentication is to be used); will be
     * overridden if authentication credentials passed in URL
     */
    public UrlResourceInputStream(URL inputStreamUrl,
        String userName, String password) throws IOException {

        // if authentication credentials were specified in URL, use them
        String userInfo = inputStreamUrl.getUserInfo();
        if (userInfo != null) {
            String[] userInfoArray = userInfo.split(":");
            userName = userInfoArray[0];
            password = userInfoArray[1];
        }

        mInputStream = getRawInputStream(inputStreamUrl, userName, password);
    }

    /**
     * @param inputStreamUrl URL endpoint of network-based resource
     * (may contain authentication credentials)
     */
    public UrlResourceInputStream(URL inputStreamUrl) throws IOException {
        this(inputStreamUrl, null, null);
    }

    /**
     * @see InputStream#read()
     */
    @Override
    public int read() throws IOException {
        // TODO Auto-generated method stub
        return mInputStream.read();
    }

    /**
     * @see InputStream#close()
     */
    @Override
    public void close() throws IOException {
        mInputStream.close();
    }


    //
    //  Protected instance methods
    //

    /**
     * @param inputStreamUrl URL endpoint of network-based resource
     * @param userName user name to use when authenticating URL request
     * (may be <code>null</code> if no authentication is to be used)
     * @param password password to use when authenticating URL request
     * (may be <code>null</code> if no authentication is to be used)
     */
    protected InputStream getRawInputStream(URL inputStreamUrl,
            final String userName, final String password)
        throws IOException {

        if (userName != null || password != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName,
                            password.toCharArray());
                }
            });
        }

        // connect to the website
        URLConnection urlConnection = inputStreamUrl.openConnection();
        try {
            EasyX509TrustManager.allowSelfsignedSSLCertificates(urlConnection);
        } catch(GeneralSecurityException gse) {
            throw new IOException(
                "couldn't configure SSL socket to accept self signed certificate", gse);
        }
        urlConnection.connect();

        return urlConnection.getInputStream();
    }

}
