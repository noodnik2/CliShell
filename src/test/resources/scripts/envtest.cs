#
# Prove out concept of "setting up the custom scripting environment"
# by loading custom functions and data required by other script(s).
# 
# Dependencies:
#   "myenv.js" - sets up scripting environment
#   "myjs.js" - uses environment feature(s) setup by "myenv.js"
#

jscript -r src/test/resources/scripts/myenv.js
jscript    src/test/resources/scripts/myjs.js 

# end of script
