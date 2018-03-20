/*   Copyright 2004 The Apache Software Foundation
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.apache.xmlbeans.test.tools;

import org.openuri.perf.*;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.BuildException;

public class PerfResultTask extends MatchingTask
{	
	private final String P = System.getProperty("file.separator");
	
	// attributes
	private String _srcdir = null;
	private String _gendir = null;
	private String _delim = null;
	private String _hostname = null;
	
	// setters for attributes
	public void setSrcdir(String p_srcdir)
	{
		_srcdir = p_srcdir;
	}
	
	public void setGendir(String p_gendir)
	{
		_gendir = p_gendir;
	}
	
	public void setDelimiter(String p_delimiter)
	{
		_delim = p_delimiter;
	}
	
	public void setHostname(String p_hostname)
	{
		_hostname = p_hostname;
	}
	
	
	public void execute() throws BuildException
	{
		PerfResultUtil util = new PerfResultUtil();
		try
		{
			// process files from includes
			fileset.setDir(new File(_srcdir));
			String[] files = fileset.getDirectoryScanner(project).getIncludedFiles();
			
			for(int i=0; i<files.length; i++)
			{
				System.out.println("processing file: "+_srcdir+P+files[i]);
				ResultSetDocument doc = util.processFlatFile(_srcdir+P+files[i],_delim);
				// TODO: add ant prop overrides for other env sesttings
				doc.getResultSet().getEnvironment().setHostname(_hostname);
				util.saveXmlToFile(doc,_gendir,files[i]);
			}
			
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.out.println("ERROR: IOException");
			return;
		}
	}


}