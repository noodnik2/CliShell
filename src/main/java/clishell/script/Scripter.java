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

package clishell.script;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * Class to execute scripts
 * Uses Java 6's scripting engine;
 * @see <a href="http://java.sun.com/developer/technicalArticles/J2SE/Desktop/scripting/">Article: Scripting for the Java Platform</a>
 *
 */
public class Scripter {

    /** */
    private final ScriptEngine mScriptEngine;

    /**
     * @param scriptLanguageName
     * @throws ScripterException
     */
    public Scripter(String scriptLanguageName) throws ScripterException{
        mScriptEngine = new ScriptEngineManager().getEngineByName(scriptLanguageName);
        if (mScriptEngine == null) {
            throw new ScripterException("no scripting engine found for: '"
                + scriptLanguageName
                + "'");
        }
    }

    /**
     * @param scriptScript
     * @throws ScripterException
     */
    public void executeScript(String scriptScript) throws ScripterException {
        try {
            mScriptEngine.eval(scriptScript);
          } catch (ScriptException ex) {
              ex.printStackTrace();
              throw new ScripterException(ex);
          }
    }

    /**
     * @param functionName
     * @throws ScripterException
     */
    public void executeFunction(String functionName) throws ScripterException {
        try {
            ((Invocable) mScriptEngine).invokeFunction(functionName);
        } catch(NoSuchMethodException nsme) {
            throw new ScripterException(nsme);
        } catch(ScriptException se) {
            throw new ScripterException(se);
        }
    }

}
