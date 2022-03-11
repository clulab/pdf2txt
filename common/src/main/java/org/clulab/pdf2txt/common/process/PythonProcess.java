/* ***************************************************************************
Package
*****************************************************************************/
package org.clulab.pdf2txt.common.process;
/* ***************************************************************************
Imports
*****************************************************************************/
import java.util.ArrayList;
/* ***************************************************************************
Class
*****************************************************************************/
public class PythonProcess extends ExternalProcess {
	
	public PythonProcess(String cd, String python, String program, ArrayList<String> arguments) {
		super();
		directory(cd);
		command.add(python);
		command.add(program);
		command.addAll(arguments);
	}
	
	public PythonProcess(String python, String program) {
		super();
		command.add(python);
		command.add("-c");
		command.add(program);
	}
}
/* **************************************************************************/
