package clishell.test;

import org.junit.Test;


public class ClasspathProtocolHandlerTest {

    @Test
    public void testOne() {

        String classpath = System.getProperty("java.class.path");
        String separator = System.getProperty("path.separator");
        String[] paths = classpath.split(separator);
        for (String path : paths) {
            System.out.println("pathelement = '" + path + "'");
        }
    }
}
