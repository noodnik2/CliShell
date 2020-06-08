package clishell;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A {@link URLStreamHandler} that handles resources on the classpath.
 */
public class ClasspathProtocolHandler extends URLStreamHandler {

    /** The classloader to find resources from. */
    private final ClassLoader mClassLoader;

    /**
     * @param classLoader
     */
    public ClasspathProtocolHandler(ClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        final URL resourceUrl = mClassLoader.getResource(u.getPath());
        return resourceUrl.openConnection();
    }

}
