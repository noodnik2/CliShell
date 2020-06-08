/**
 *
 *
 * Command Line Interface Harness - Javascript
 *
 *
 *
 *
 * @author MRoss
 *
 * @see <a href="http://java.sun.com/developer/technicalArticles/J2SE/Desktop/scripting/">Scripting for the Java Platform
</a>
 * 
 */

// say that we're alive
print("Hello, javascript world - we're alive!\n");

// import the CLI java package
importPackage(clishell);
print("clishell java package imported\n");

//grab the CLI instance
var cli = CliRunner.getInstance();

//execute a CLI command that should succeed
cli.dispatchCommand("echo 'hi - you should see this!'");
print("Back from execution of CLI command!\n");

//execute a CLI command that should fail
cli.dispatchCommand("echox 'hi - you should NOT see this!'");

for (i = 1; i < 10; i++) {
	cli.dispatchCommand("echo 'hi' - " + i + " - yea!");
}
print("Back from looping CLI commands!\n");

// define a JavaScript function
function newFunction(arg) {
	println("Hi '" + arg + "' from a function!\n");
}

// test calling the newly defined function passing an argument to it
newFunction('oops');

// issue a CLI command and capture its output into a buffer
cli.dispatchCommand("capture buffer one echo hi from one");

// get a reference to the scripting plugin
// (must be loaded since otherwise we wouldn't be executing...)
var scriptingPlugin = cli.getPluginInstance("scripting");

// exit if can't locate it (maybe loaded under different name?)
if (scriptingPlugin == null) {
	throw "can't get Scripting plugin instance";
}

// get & print the buffer that has the command output
var buffer = scriptingPlugin.getBuffer("one");
println("buffer = '" + buffer + "'");

// test out JavaScript regular expressions using the buffer
if (buffer.match(/^hx/)) {
	println("yes hi");
} else {
	println("no hi");
}

// end of script



