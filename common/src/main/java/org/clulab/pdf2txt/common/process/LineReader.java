/* ***************************************************************************
Package
*****************************************************************************/
package org.clulab.pdf2txt.common.process;
/* ***************************************************************************
Imports
*****************************************************************************/
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
/* ***************************************************************************
Class
*****************************************************************************/
public class LineReader implements AutoCloseable {
	protected BufferedReader bufferedReader;
	protected int lineNo;
	
	public LineReader(InputStream inputStream) {
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		lineNo = 0;
	}
	
	public LineReader(String path) throws FileNotFoundException {
		this(new FileInputStream(path));
	}
	
	@Override
	public void close() {
		try {
			bufferedReader.close();
		}
		catch (IOException exception) {
			// I don't care
		}
	}
	
	public String readLine() throws IOException {
		String line = bufferedReader.readLine();
		
		lineNo++;
		return line;
	}
	
	public int getLineNo() {
		return lineNo;
	}
}
/* **************************************************************************/
