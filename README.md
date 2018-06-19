# Brainfuck interpreter and Eclipse editing/debug support.

Brainfuck interpreter written in Java. The interpreter supports in process
debugging, i.e. the debugger must run in the same Java process.

Eclipse feature comprising of an editor and a debug plugin.  

## Brainfuck interpreter

Characteristics:

* **Size of internal memory**  
 The size of the internal memory (navigable with &lt; and &gt;) will increase dynamically when the memory pointer exceeds the current size of the memory. This will not be boundary checked and stops when the JVM heap memory is exhausted. Starting the JVM with larger memory boundaries can increase the available memory.
 When running in Eclipse, the interpreter is run within Eclipses JVM therefore the constraints apply with which Eclipse is run. 
 
* **Wrapping**    
 The memory bytes will wrap on + and -. 

* **Running the standalone Interpreter**  
java org.birenheide.bf.Main \[in=&lt;filename&gt;\] \[out=&lt;filename&gt;\] \[dbg=&lt;filename&gt;\] &lt;filename&gt;  
  * in: name of a file to read from in the brainfuck program; will read from command line if omitted  
  * out: name of a file to write to in the brainfuck program; will write to command line if omitted  
  * dbg: name of a file containing debug information; no debugging, if omitted. See debug.properties for usage  
  * filename: mandatory file containing the brainfuck program  


See also: [Brainfuck on Esolang Wiki](http://esolangs.org/wiki/Brainfuck)

## Eclipse editing and run/debug support
* **Editing**  
Editor support with syntax highlighting and template support. Templates are triggered from code completion.
Templates can operate on number tuples separated by semicolon to enable easy template customization.

* **Debugging**  
Supports breakpoints, watchpoints and stepping. 

* [Eclipse Update Site](https://richardbirenheide.github.io/brainfuck/updatesite)  
Add the update site link under `Help->Install New Software...` in the `Work with:` field.  

## Building the code
The project can be built with Maven. The reactor `pom.xml` is in project `org.birenheide.bf.parent`.

The reactor pom builds only the interpreter and the Eclipse plugins. The target platform for the build is Eclipse Luna.
There are two more profiles defined:
* `latest-target` which builds against the latest version of Eclipse
* `update-site-assembly` which will also build the Eclipse Feature and the Update Site