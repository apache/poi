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
package org.apache.poi.xssf.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of XSSFModel for binary parts of
 *  the file, eg images or vba macros
 */
public class BinaryPart implements XSSFModel {
	private byte[] data;
	
	public BinaryPart(InputStream in) throws IOException {
		readFrom(in);
	}
	
	/**
	 * Fetch the contents of the binary part
	 */
	public byte[] getContents() {
		return data;
	}
	/**
	 * Changes the contents of the binary part
	 */
	public void setContents(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Reads the contents of the binary part in.
	 */
	public void readFrom(InputStream is) throws IOException {
		int read = 0;
		byte[] buffer = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		while( (read = is.read(buffer)) != -1 ) {
			if(read > 0) {
				baos.write(buffer, 0, read);
			}
		}
		data = baos.toByteArray();
	}
	
	public void writeTo(OutputStream out) throws IOException {
		out.write(data);
	}
}
