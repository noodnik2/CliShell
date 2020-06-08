/**
 *
 *
 * CliShell Plugin - Transformations
 *
 * $Id$
 * $URL$
 *
 * @author MRoss
 *
 */

package clishell.plugins;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import clishell.CliRunner;
import clishell.anno.CliPlugin;
import clishell.anno.CliPluginCommand;
import clishell.anno.CliPluginFinalizer;
import clishell.anno.CliPluginInitializer;
import clishell.net.UrlResourceInputStream;
import org.mortbay.util.WriterOutputStream;

@CliPlugin(
     name = "transformer"
, version = "0.1.$Rev$"
)
public class TransformerPlugin {


    //
    // Private instance data
    //


    //
    //  Public plugin "main" methods
    //

    /**
     * Plugin initialization
     */
    @CliPluginInitializer
    public void init() {
        // nothing to do - yet!
    }

    /**
     * Performs cleanup for this module,
     * before it's unloaded
     */
    @CliPluginFinalizer
    public void fini() {
        // nothing to do - yet!
    }

    //
    //  Public plugin command methods
    //

    @CliPluginCommand(
          name = "transform"
    ,   syntax = "<stylesheet> [<input-filename-or-url> [<output-filename>]]"
    , helptext = {
            "Transforms input to output using specified stylesheet."
            }
    ,  minargs = 1
    ,  maxargs = 3
    )
    public void performTransformation(String styleSheetFilename, String inputFilenameOrUrl,
            String outputFilename) throws IOException, TransformerException {

        InputStream styleSheetInputStream = null;

        InputStream inputStream = null;
        boolean isInputStreamOurs = true;

        OutputStream outputStream = null;
        boolean isOutputStreamOurs = true;

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        try {

            styleSheetInputStream = new FileInputStream(styleSheetFilename);

            Transformer transformer = transformerFactory
                .newTransformer(new StreamSource(styleSheetInputStream));

            if (inputFilenameOrUrl == null) {
                inputStream = CliRunner.getInstance().getInputConsole();
                isInputStreamOurs = false;
            } else if (inputFilenameOrUrl.contains("://")) {
                inputStream = new UrlResourceInputStream(new URL(inputFilenameOrUrl));
            } else {
                inputStream = new FileInputStream(inputFilenameOrUrl);
            }

            if (outputFilename == null) {
                outputStream = new WriterOutputStream(CliRunner.getInstance().getMessageConsole());
                isOutputStreamOurs = false;
            } else {
                outputStream = new FileOutputStream(outputFilename);
            }

            transformer.transform(new StreamSource(inputStream), new StreamResult(outputStream));

        } finally {

            if (outputStream != null && isOutputStreamOurs) {
                try {
                    outputStream.close();
                } catch(Throwable ex) {
                    // "old college try" ;-)
                }
            }
            if (inputStream != null && isInputStreamOurs) {
                try {
                    inputStream.close();
                } catch(Throwable ex) {
                    // "old college try" ;-)
                }
            }
            if (styleSheetInputStream != null) {
                try {
                    styleSheetInputStream.close();
                } catch(Throwable ex) {
                    // "old college try" ;-)
                }
            }

        }

    }

}
