/* ***************************************************************************
Package
*****************************************************************************/
package org.clulab.pdf2txt.common.process;
/* ***************************************************************************
Imports
*****************************************************************************/

/* ***************************************************************************
Class
*****************************************************************************/
public class AppException extends Exception {
	private static final long serialVersionUID = 1L;

	public AppException() {
		super();
	}
	
	public AppException(String message) {
		super(message);
	}

	// This is Exception for a reason.  We want to catch unchecked exceptions
	// and roll back the database even for null pointers and out of range indexes.
	public AppException(String message, Exception cause) {
		super(message, cause);
	}
	
	public AppException(Exception cause) {
		super(cause);
	}
}
/* **************************************************************************/
