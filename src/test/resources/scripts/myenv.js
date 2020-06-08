/*
 * "My Environment" test script for CliShell "ScriptingPlugin"
 * 
 * Prove out concept of "setting up the custom scripting environment"
 * by loading custom functions and data required by other script(s).
 * 
 */

function message(text) {
	println("message: " + text);
}

message('message function defined in "myenv.js"');

// end of script

