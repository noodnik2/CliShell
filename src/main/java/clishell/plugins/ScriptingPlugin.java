/**
 *
 *
 * Command Line Interface Harness
 *
 *
 *
 *
 * @author MRoss
 *
 */

package clishell.plugins;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.util.ReaderInputStream;
import clishell.CliCommandOptions;
import clishell.CliRunner;
import clishell.anno.CliPlugin;
import clishell.anno.CliPluginCommand;
import clishell.anno.CliPluginInitializer;
import clishell.ex.CliRejectedInputException;
import clishell.ex.CliRunnerException;
import clishell.net.UrlResourceInputStream;
import clishell.script.Scripter;
import clishell.script.ScripterException;

@CliPlugin(
     name = "scripting"
, version = "0.1.$Rev$"
)
public class ScriptingPlugin {


    //
    //  Private instance data
    //

    /** IPC buffers for capturing CLI command output */
    private Map<String, String> mStringBuffers;

    /** Scripting engine - script context */
    private Scripter mScripter;


    //
    //  Public CLI Plugin initialization
    //

    @CliPluginInitializer
    public void init() throws ScripterException {
        mStringBuffers = new HashMap<String, String>();
        resetContext();
    }


    //
    //  Public CLI Plugin instance command methods
    //

    @CliPluginCommand(
         names = { "javascript", "jscript" }
    ,   syntax = "[-r] <script-filename>"
    ,  options = "r"
    , helptext = "use the '-r' option if you need to run the script in a new scripting context"
    , ordering = "a"
    )
    public void runScript(CliCommandOptions options, String scriptFilename)
        throws ScripterException, IOException {

        FileReader fileReader = new FileReader(scriptFilename);
        StringBuffer stringBuffer = new StringBuffer();
        int len;
        char[] buffer = new char[4096];
        while((len = fileReader.read(buffer))> 0) {
            stringBuffer.append(buffer, 0, len);
        }
        fileReader.close();

        // run in new context if option set
        if (options.isOptionSet('r')) {
            resetContext();
        }

        mScripter.executeScript(stringBuffer.toString());
    }

    @CliPluginCommand(
          name = "capture buffer"
    ,   syntax = "[-qe] <buffer-name> <command> [<command-arg> [<command-arg> [...]]"
    ,  options = "qe"
    , helptext = {
        "Captures output from <command> and writes it to the buffer specified by <buffer-name>,"
      , "so that it may be fed to another command as input (using the \"feed buffer\" command),"
      , "or retrieved from a script (such as javascript) using the getBuffer(<buffer-name>)"
      , "method of this CliShell plugin"
      ,   "Options:"
      ,   "  -q  (quiet) suppress echoing the output to the current message console"
      ,   "  -e  (errors) also captures error message(s) into the buffer"
      }
    ,  minargs = 2
    , ordering = "b"
    )
    public void captureBuffer(CliCommandOptions options, String... args)
        throws CliRunnerException {

        // allocate a string buffer to capture console output
        StringWriter bufferWriter = new StringWriter();

        // setup to write output to specified file
        PrintWriter messageConsole = new PrintWriter(bufferWriter);

        // dispatch the command in args[1..n] using the new message console
        CliPluginUtil.dispatchCommand(options, null, messageConsole,
            Arrays.copyOfRange(args, 1, args.length));

        // store buffer
        mStringBuffers.put(args[0], bufferWriter.toString());

    }

    // TODO: should this command get moved up to the "builtin" (or another) plugin?
    // also, we had to add the second command alias for this ("get url") since due
    // to a bug in "FullCommandNameTree" wherein it can't distinguish between two
    // candidate commands "get resource users" and "get resource", we need an alternate
    // way to call this command - TODO: please fix that bug (think it's in the
    // implementation of the "collectFullCommandWordsFromAbbreviated()" method)
    @CliPluginCommand(
          names = {
              "get resource"
          ,   "get url"
          }
    ,   syntax = "<resource-identifier>"
    , helptext = {
        "Retrieves the resource data indicated by <resource-identifier>, which may"
      , "be a URL or a filename."
      }
    , ordering = "c"
    )
    public void getResource(CliCommandOptions options, String resourceIdentifier)
        throws CliRunnerException, MalformedURLException, IOException {

        URL urlToLoad;
        if (!resourceIdentifier.contains("://")) {
            urlToLoad = new URL("file://" + resourceIdentifier);
        } else {
            urlToLoad = new URL(resourceIdentifier);
        }

        Writer outputWriter = CliRunner.getInstance().getMessageConsole();

        UrlResourceInputStream inputStream = new UrlResourceInputStream(urlToLoad);
        Reader inputReader = new InputStreamReader(inputStream);

        try {

            char[] buffer = new char[4096];
            int length;
            while((length = inputReader.read(buffer)) > 0) {
                outputWriter.write(buffer, 0, length);
            }
            outputWriter.flush();

        } finally {

            try {
                inputReader.close();
            } catch(Throwable t) {
                // "old college try" ;-)
            }

        }

    }

    @CliPluginCommand(
          name = "feed buffer"
    ,   syntax = "<buffer-name> <command> [<command-arg> [<command-arg> [...]]"
    ,  options = "qe"
    , helptext = {
        "Feeds input to <command> from the buffer specified by <buffer-name>;"
      , "NOTE: only commands that obtain their console input from the stream"
      , "returned by the CliRunner.getInputConsole() method will accept this"
      , "input."
      }
    ,  minargs = 2
    , ordering = "d"
    )
    public void feedBuffer(CliCommandOptions options, String... args)
            throws CliRunnerException {

        String bufferName = args[0];
        String bufferContents = mStringBuffers.get(bufferName);

        if (bufferContents == null) {
            throw new CliRejectedInputException("buffer '"
                + bufferName
                + "' not found");
        }

        // allocate a input stream to read the buffer as console input
        InputStream bufferInputStream = new ReaderInputStream(
            new StringReader(bufferContents));

        // dispatch the command in args[1..n] using the new input console
        CliPluginUtil.dispatchCommand(options, bufferInputStream, null,
            Arrays.copyOfRange(args, 1, args.length));
    }

    @CliPluginCommand(
          name = "list buffers"
    ,   syntax = "[-v] [<buffer-name> [<buffer-name> [...]]]"
    ,  options = "v"
    , helptext = {
        "Lists the currently defined buffers"
      , "Options:"
      , "  -v   Also prints out the contents (value) of each buffer"
      }
    , ordering = "e"
    )
    public void listBuffers(CliCommandOptions options, String... bufferNames) {

        Set<String> bufferNameSet;
        if (bufferNames.length == 0) {
            bufferNameSet = mStringBuffers.keySet();
        } else {
            bufferNameSet = new HashSet<String>();
            for (String bufferName : bufferNames) {
                bufferNameSet.add(bufferName);
            }
        }

        PrintWriter messageConsole = CliRunner.getInstance().getMessageConsole();
        for (String bufferName : bufferNameSet) {
            String bufferContents = mStringBuffers.get(bufferName);
            String bufferStatus = (bufferContents == null)
                ? "not found"
                : ("" + bufferContents.length() + " characters");
            messageConsole.println(bufferName + ": (" + bufferStatus + ")");
            if (bufferContents != null) {
                if (bufferNameSet.contains(bufferName)) {
                    if (options.isOptionSet('v')) {
                        printMultilineStringWithPrefix(messageConsole,
                            "    ", bufferContents);
                    }
                }
            }
        }
    }

    @CliPluginCommand(
          name = "delete buffers"
    ,   syntax = "<buffer-name> [<buffer-name> [...]]"
    ,  minargs = 1
    , helptext = {
        "Deletes the specified buffer(s)"
      }
    , ordering = "f"
    )
    public void deleteBuffers(CliCommandOptions options, String... bufferNames) {

        Set<String> bufferNameSet = new HashSet<String>();
        for (String bufferName : bufferNames) {
            bufferNameSet.add(bufferName);
        }

        PrintWriter messageConsole = CliRunner.getInstance().getMessageConsole();
        for (String bufferName : bufferNameSet) {
            String bufferStatus = (mStringBuffers.remove(bufferName) != null)
                ? "deleted" : "not found";
            messageConsole.println("buffer '" + bufferName + "' " + bufferStatus);
        }
    }


    //
    //  Plugin instance java utility methods
    //

    /**
     * This method is designed to be called from a scripting language,
     * to get access to the contents of the named buffer.
     * @param bufferName name of buffer
     * @return value of the specified string buffer, or <code>null</code>
     * if the named buffer was not found
     */
    public String getBuffer(String bufferName) {
        return mStringBuffers.get(bufferName);
    }


    //
    //  Private instance methods
    //

    /**
     * @throws ScripterException unhandled exception while resetting scripting context
     */
    private void resetContext() throws ScripterException {
        mScripter = new Scripter("JavaScript");
    }

    /**
     * @param printWriter writer on which to output formatted string
     * @param linePrefix prefix for each line outputted
     * @param string string text to present, each line prefixed by <code>linePrefix</code>
     */
    private void printMultilineStringWithPrefix(PrintWriter printWriter,
        String prefix, String string) {

        for (String line : string.split("(\\r\\n|\\n)")) {
            printWriter.println(prefix + line);
        }

    }

}
