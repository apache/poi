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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;
import java.util.Calendar;

//import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class PerfResultUtil
{	
	/*
	 * Process flat file logs into perf result xmlbean instance.
	 * A result consists is a line in the file starting with the
	 * given delimiter, followed by space delimitted perf data. 
	 * Useful as a postprocessor for simple perf tests where 
	 * you just want to use stdout loging during the test.
	 */
	public ResultSetDocument processFlatFile(String p_filename, String p_delim) throws FileNotFoundException, IOException
	{
		// names for known space-delimited perf data
		// labels plucked out of the given file
		final String TIME = "time";
		final String MEMORY = "memory";
		final String HASH = "hash";
		
		FileReader reader = new FileReader(p_filename);
		StringBuffer buff = new StringBuffer();
		int c;
		while( (c=reader.read()) != -1)
		{
			buff.append((char)c);
		}
		//System.out.println("file contents: \n"+buff.toString());
		
		ResultSetDocument doc = ResultSetDocument.Factory.newInstance();
		ResultSetDocument.ResultSet resultSet = doc.addNewResultSet();
		
		// add default environment info
		Environment env = resultSet.addNewEnvironment();
		env.setOs(System.getProperty("os.name")+System.getProperty("os.version"));
		env.setJvm(System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.version"));
		env.setDate(Calendar.getInstance());
		
		StringTokenizer st = new StringTokenizer( buff.toString(),System.getProperty("line.separator"));
		int k = 1;
		while(st.hasMoreTokens())
		{
			String currentLine = st.nextToken().toString();
			if(currentLine.startsWith(p_delim))
			{
				StringTokenizer stResult = new StringTokenizer(currentLine," ");
				// advance past the perf rusult line delimeter
				stResult.nextToken();
				
				Result result = resultSet.addNewResult();
				// id is a unique integer within this result set
				result.setId(k);
				k += 1;
				// name must be the first token in a flat perf result
				result.setName(stResult.nextToken());
				
				// scan the resulting tokens for known perf data or notes
				while(stResult.hasMoreTokens())
				{
					String data = stResult.nextToken();
					if(data.indexOf(TIME) != -1)
						result.setTime(Long.parseLong(stResult.nextToken()));
					else if(data.indexOf(HASH) != -1)
						result.setHash(Integer.parseInt(stResult.nextToken()));
					else if(data.indexOf(MEMORY) != -1)
						result.setMemory(Long.parseLong(stResult.nextToken()));
					else if(data.indexOf("=") != -1)
					{
						// add custom element/value if this is a 'foo=bar' type token
						String[] pair = data.split("=");
						Custom custom = result.addNewCustom();
						custom.setName(pair[0]);
						custom.setValue(pair[1]);
					}
					else
					{
						if(null != result.getNote())
							result.setNote(result.getNote()+" "+data);
						else
							result.setNote(data);
					}
					
				}
			}
		}
		

		// TODO: retreive all system information programmatically
		//env.setSysmem();
		//env.setHostname();
		//env.setCpuinfo();
		// TODO: add change list support
		
		return doc;
	}
	
	public static void saveXmlToFile(ResultSetDocument p_doc, String p_dirname, String p_origname) throws IOException
	{
		// save the xml result
		File fGendir = new File(p_dirname);
		fGendir.mkdirs();
		FileWriter writer = new FileWriter(p_dirname+System.getProperty("file.separator")+p_origname+".xml");
		XmlOptions opts = new XmlOptions();
		opts.setSavePrettyPrint();
		p_doc.save(writer,opts);
		writer.flush();
		writer.close();
	}
	
}