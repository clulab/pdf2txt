/* ***************************************************************************
Package
*****************************************************************************/
package org.clulab.pdf2txt.common.process;
/* ***************************************************************************
Imports
*****************************************************************************/
import java.lang.ProcessBuilder;
import java.io.IOException;
import java.lang.Process;
import java.util.ArrayList;
import java.util.List;
/* ***************************************************************************
Class
*****************************************************************************/
public class ExternalProcess {
	protected ProcessBuilder processBuilder;
	protected ArrayList<String> command;
	
	public ExternalProcess() {
		this(new ArrayList<String>());
	}
	
	public ExternalProcess(String command) {
		this();
		this.command.add(command);
	}
	
	public ExternalProcess(ArrayList<String> command) {
		this.command = command;
		processBuilder = new ProcessBuilder(command);
	}
	
	public ProcessBuilder getProcessBuilder() {
		return processBuilder;
	}
	
	public void execute() throws AppException, IOException, InterruptedException {
		Process process = processBuilder.start();
		
		int result = process.waitFor();
		if (result != 0)
			throw new AppException("Process " + (command.size() > 0 ? "\"" + command.get(0) + "\"" : "") + " terminated abnormally!");
	}
	
	// See https://stackoverflow.com/questions/16714127/how-to-redirect-process-builders-output-to-a-string
	public String executeToString() throws IOException {
		Process process = processBuilder.start();
		
		try (LineReader lineReader = new LineReader(process.getInputStream())) {
			StringBuffer stringBuffer = new StringBuffer();
			String line = null;
			
			while ((line = lineReader.readLine()) != null) {
				stringBuffer.append(line);
				stringBuffer.append('\n');
			}
			return stringBuffer.toString();
		}
	}
	
	public List<String> command() {
		return processBuilder.command();
	}
	
	public void directory(String name) {
		processBuilder.directory(new java.io.File(name));
	}
	
	public void environment(String name, String value) {
		processBuilder.environment().put(name, value);
	}
}
/* **************************************************************************/
