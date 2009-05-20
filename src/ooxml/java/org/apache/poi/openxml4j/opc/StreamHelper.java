/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.openxml4j.opc;

import java.io.InputStream;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public final class StreamHelper {

	private StreamHelper() {
		// Do nothing
	}

	/**
	 * Turning the DOM4j object in the specified output stream.
	 *
	 * @param xmlContent
	 *            The XML document.
	 * @param outStream
	 *            The OutputStream in which the XML document will be written.
	 * @return <b>true</b> if the xml is successfully written in the stream,
	 *         else <b>false</b>.
	 */
	public static boolean saveXmlInStream(Document xmlContent,
			OutputStream outStream) {
		try {
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(outStream, outformat);
			writer.write(xmlContent);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Copy the input stream into the output stream.
	 *
	 * @param inStream
	 *            The source stream.
	 * @param outStream
	 *            The destination stream.
	 * @return <b>true</b> if the operation succeed, else return <b>false</b>.
	 */
	public static boolean copyStream(InputStream inStream, OutputStream outStream) {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) >= 0) {
				outStream.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
