/**
 * 
 *
 *  
 *
 *
 * 
 * @author MRoss
 * 
 */

package wccommon.cli.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import wccommon.cli.CliException;
import wccommon.cli.CliMethodForm;
import wccommon.cli.CliUtils;
import wccommon.cli.anno.CliPluginCommand;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author mross
 *
 */
public class CliUtilTests {

    /**
     *  Tests
     */
    
    @Test
    public void testCommandForms() throws CliException {
        for (Method method : getClass().getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType() == CliPluginCommand.class) {
                    String signature = CliUtils.getMethodSignature(method);
                    CliMethodForm actualMethodForm = CliUtils.getMethodForm(method);
                    CliPluginCommand cliPluginCommand = (CliPluginCommand) annotation;
                    CliMethodForm declaredMethodForm = cliPluginCommand.methodForm();
                    Assert.assertEquals(signature, declaredMethodForm, actualMethodForm);
                }
            }
        }
    }
    
    @Test
    public void testGetSignature() throws Exception {
        String simpleSignature = CliUtils.getMethodSignature(
                this.getClass().getMethod("getMethodSignatureForSimple", 
                        new Class<?>[] { String.class, String.class }));
        Assert.assertEquals("getMethodSignatureForSimple", 
                "wccommon.cli.test.CliUtilTests.getMethodSignatureForSimple(java.lang.String,java.lang.String)",
                simpleSignature);
        
        String complexSignature = CliUtils.getMethodSignature(
                this.getClass().getMethod("getMethodSignatureForComplex", 
                        new Class<?>[] { Map.class, String[].class, String.class }));
        Assert.assertEquals("getMethodSignatureForComplex", 
                "wccommon.cli.test.CliUtilTests.getMethodSignatureForComplex(java.util.Map,[Ljava.lang.String;,java.lang.String)",
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
    public void booleanOptionsStrings(Map<String, String> options, String string1, String string2, String string3) {
        //
    }
    
    @CliPluginCommand(methodForm = CliMethodForm.VOID_OPTIONS_STRINGARRAY)
    public void booleanOptionsStringArray(Map<String, String> options,
            String[] stringArray) {
        //
    }
    
    @CliPluginCommand(methodForm = CliMethodForm.VOID_OPTIONS)
    public void booleanOptions(Map<String, String> options) {
        //
    }
    
    @CliPluginCommand(methodForm = CliMethodForm.VOID_STRINGARRAY)
    public void booleanStringArray(String[] array) {
        //
    }
    
    @CliPluginCommand(methodForm = CliMethodForm.VOID_STRINGS)
    public void booleanStrings(String one, String two) {
        //
    }
    
    @CliPluginCommand(methodForm = CliMethodForm.VOID_NOPARAM)
    public void booleanNoParam() {
        //
    }
    
    @CliPluginCommand(methodForm = CliMethodForm.VOID_NOPARAM)
    public void voidNoParam() {
        //
    }

    
    //
    // methods to test signature
    //
    
    public Boolean getMethodSignatureForSimple(String a1, String a2) {
        return null;
    }

    public void getMethodSignatureForComplex(Map<String, String> m1,
            String[] sa1, String a2) {
        //
    }

}
