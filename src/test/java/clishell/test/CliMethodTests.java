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


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import clishell.CliCommandOptions;
import clishell.CliMethod;
import clishell.CliMethodForm;
import clishell.anno.CliPluginCommand;
import clishell.ex.CliException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author mross
 *
 */
public class CliMethodTests {

    /**
     *  Tests
     */

    @Test
    public void testCommandForms() throws CliException {
        for (Method method : getClass().getMethods()) {
            CliMethod cliMethod = new CliMethod(method);
            String signature = cliMethod.getMethodSignature();
            CliMethodForm actualMethodForm = cliMethod.getCliMethodForm();
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType() == CliPluginCommand.class) {
                    CliPluginCommand cliPluginCommand = (CliPluginCommand) annotation;
                    CliMethodForm declaredMethodForm = cliPluginCommand.methodForm();
                    Assert.assertEquals(signature, declaredMethodForm, actualMethodForm);
                }
            }
        }
    }

    @Test
    public void testGetSignature() throws Exception {
        String simpleSignature = new CliMethod(getClass().getMethod("getMethodSignatureForSimple",
            new Class<?>[] {String.class, String.class })).getMethodSignature();
        Assert.assertEquals("getMethodSignatureForSimple",
                "clishell.test.CliMethodTests.getMethodSignatureForSimple(String,String)",
                simpleSignature);

        String complexSignature = new CliMethod(getClass().getMethod("getMethodSignatureForComplex",
                        new Class<?>[] {CliCommandOptions.class, String[].class, String.class })).getMethodSignature();
        Assert.assertEquals("getMethodSignatureForComplex",
                "clishell.test.CliMethodTests.getMethodSignatureForComplex(CliCommandOptions,String[],String)",
                complexSignature);

    }


    /**
     *
     * Test data - methods to be introspected
     *
     */


    //
    //  Methods to test ClPluginCommands
    //

    @CliPluginCommand(methodForm = CliMethodForm.VOID_OPTIONS_STRINGS)
    public void booleanOptionsStrings(CliCommandOptions options, String string1, String string2, String string3) {
        // nothing to do
    }

    @CliPluginCommand(methodForm = CliMethodForm.VOID_OPTIONS_STRINGARRAY)
    public void booleanOptionsStringArray(CliCommandOptions options, String[] stringArray) {
        // nothing to do
    }

    @CliPluginCommand(methodForm = CliMethodForm.VOID_OPTIONS)
    public void booleanOptions(CliCommandOptions options) {
        // nothing to do
    }

    @CliPluginCommand(methodForm = CliMethodForm.VOID_STRINGARRAY)
    public void booleanStringArray(String[] array) {
        // nothing to do
    }

    @CliPluginCommand(methodForm = CliMethodForm.VOID_STRINGS)
    public void booleanStrings(String one, String two) {
        // nothing to do
    }

    @CliPluginCommand(methodForm = CliMethodForm.VOID_NOPARAM)
    public void booleanNoParam() {
        // nothing to do
    }

    @CliPluginCommand(methodForm = CliMethodForm.VOID_NOPARAM)
    public void voidNoParam() {
        // nothing to do
    }


    //
    // methods to test signature
    //

    public Boolean getMethodSignatureForSimple(String a1, String a2) {
        return null;
    }

    public void getMethodSignatureForComplex(CliCommandOptions m1, String[] sa1, String a2) {
        // nothing to do
    }

}
