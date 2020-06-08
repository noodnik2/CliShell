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

package clishell.test;

import java.io.PrintWriter;

import clishell.CliRunner;
import clishell.anno.CliPlugin;
import clishell.anno.CliPluginCommand;
import clishell.anno.CliPluginFinalizer;
import clishell.anno.CliPluginInitializer;
import clishell.ex.CliException;

@CliPlugin
public class CliRunnerPluginTest {

    /**
     *
     */
    private PrintWriter mConsole;

    @CliPluginInitializer
    public void init1() {
        mConsole = CliRunner.getInstance().getMessageConsole();
        mConsole.println(getClass().getName() + " signing on init1");
    }

    @CliPluginInitializer
    public void init2() {
        mConsole.println(getClass().getName() + " signing on init3");
    }

    @CliPluginInitializer
    public void init3() {
        mConsole.println(getClass().getName() + " signing on init3");
    }

    @CliPluginFinalizer
    public void fini1() {
        mConsole.println(getClass().getName() + " signing off fini1");
    }

    @CliPluginFinalizer
    public void fini2() {
        mConsole.println(getClass().getName() + " signing off fini2");
    }

    @CliPluginCommand(name = "hi1", syntax = "<text>")
    public void hiCommand(String arg) {
        mConsole.println("hi '" + arg + "' from 'hi1' command");
    }

    @CliPluginCommand(name = "hi2", syntax = "<text1> <text2>")
    public void hiCommand(String arg1, String arg2) {
        mConsole.println("hi2 '" + arg1 + "', '" + arg2 + "' from 'hi2' command");
    }

    @CliPluginCommand(name = "hia", syntax = "[<text1> [<text2> [...]]]")
    public void hiCommand(String[] args) {
        for (String arg : args) {
            mConsole.println("arg: '" + arg + "'");
        }
    }

    @CliPluginCommand
    public void echo(String[] args) throws CliException {
        for (String commandToken : args) {
            mConsole.println("\"" + commandToken + "\"");
        }
    }

}
