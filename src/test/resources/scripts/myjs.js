/*
 * "My Environment" test script for CliShell "ScriptingPlugin"
 * 
 * Prove out concept of "setting up the custom scripting environment"
 * by loading custom functions and data required by other script(s).
 * 
 */

// use the "message" function which was already "setup" for the environment (see "myenv.js")
message('message function being called from "myjs.js"');

//// original
//function mergeHBArrays(array1, array2) {
//
//	// traverse through the properties of array2
//	for(var idx in array2){
//
//		// if the current property is already in result
//		if(contains(array1, idx)){
//
//			// skip it and have the function call itself to move to the next property
//			array1[idx] = mergeHBArrays(array1[idx], array2[idx]);
//
//		// otherwise, merge the property into the base
//	    } else{
//	    	array1[idx] = array2[idx];
//		}
//	}
//
//	// return array2 merged with base
//	return array1;
//}

// new
function merge(array1, array2) {

	var merged = {};
	
	message('array1');
	for (var idx in array1) {
		var obj = array1[idx];
		message(typeof obj);
		if (typeof obj == "object") {
			merged[idx] = obj;		// clone??
		} else {
			merged[idx] = obj;
		}
	}

	message('array2');
	for (var idx in array2) {
		var obj = array2[idx];
		message(typeof obj);
		if (typeof obj == "object") {
			merged[idx] = obj;		// clone??
		} else {
			merged[idx] = obj;
		}
	}

	// return array2 merged with base
	return merged;
}

function testit() {
	
	var array1 = { '1.one': 'this is 1.one', '1.two': 'this is 1.two' };
	var array2 = { '2.one': 'this is 2.one', '2.two': 'this is 2.two' };
	var array3 = {};
	var array4 = { '4.one': { '4.one.one': '4.1.1' }, '4.two': 'this is 4.two' };
	
	for (var state in ['MA', 'ME', 'NH']) {
		array3[state] = {};
		for (var instype in ['Fully Insured', 'Self Insured', 'Not Insured']) {
			array3[state][instype] = {};
			for (var filename in ['A','B','C']) {
				array3[state][instype][filename + ".pdf"] = state + " " + instype + " filename";
			}
		}
	}
	
//	array3["MA"]["Fully Insured"]["A.pdf"] = "MA-Fully Insured-A.pdf";
//	array3["ME"]["Self Insured"]["B.pdf"] = "ME-Self Insured-B.pdf";
//	array3["NH"]["Not Insured"]["C.pdf"] = "NH-Not Insured-C.pdf";
	
	var merge12 = merge(array1, array4);
	for (var idx in merge12) {
		message('idx:' + idx + ", '" + merge12[idx] + "'");
	}
	
}

testit();



// end of script