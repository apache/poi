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
package org.apache.poi.hpbf.dev;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * For dumping out the contents of HPBF (Publisher)
 *  files, while we try to figure out how they're
 *  constructed.
 */
public class HPBFDumper {
	private POIFSFileSystem fs;
	public HPBFDumper(POIFSFileSystem fs) {
		this.fs = fs;
	}
	public HPBFDumper(InputStream inp) throws IOException {
		this(new POIFSFileSystem(inp));
	}
	
	private static byte[] getData(DirectoryNode dir, String name) throws IOException {
		DocumentEntry docProps =
			(DocumentEntry)dir.getEntry(name);

		// Grab the document stream
		byte[] d = new byte[docProps.getSize()];
		dir.createDocumentInputStream(name).read(d);
		
		// All done
		return d;
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HPBFDumper <filename>");
			System.exit(1);
		}
		HPBFDumper dump = new HPBFDumper(
				new FileInputStream(args[0])
		);
		
		System.out.println("Dumping " + args[0]);
		dump.dumpContents();
		dump.dumpEnvelope();
		dump.dumpEscher();
		dump.dump001CompObj(dump.fs.getRoot());
		dump.dumpQuill();
		
		// Still to go:
		//  (0x03)Internal
		//  Objects
	}
	
	/**
	 * Dump out the escher parts of the file.
	 * Escher -> EscherStm and EscherDelayStm
	 */
	public void dumpEscher() throws IOException {
		DirectoryNode escherDir = (DirectoryNode) 
			fs.getRoot().getEntry("Escher");
		
		dumpEscherStm(escherDir);
		dumpEscherDelayStm(escherDir);
	}
	private void dumpEscherStream(byte[] data) {
		DefaultEscherRecordFactory erf = 
			new DefaultEscherRecordFactory();
		
		// Dump
		int left = data.length;
		while(left > 0) {
			EscherRecord er = erf.createRecord(data, 0);
			er.fillFields(data, 0, erf);
			left -= er.getRecordSize();
			
			System.out.println(er.toString());
		}
	}
	protected void dumpEscherStm(DirectoryNode escherDir) throws IOException {
		byte[] data = getData(escherDir, "EscherStm");
		System.out.println("");
		System.out.println("EscherStm - " + data.length + " bytes long:");
		if(data.length > 0)
			dumpEscherStream(data);
	}
	protected void dumpEscherDelayStm(DirectoryNode escherDir) throws IOException {
		byte[] data = getData(escherDir, "EscherDelayStm");
		System.out.println("");
		System.out.println("EscherDelayStm - " + data.length + " bytes long:");
		if(data.length > 0)
			dumpEscherStream(data);
	}
	
	public void dumpEnvelope() throws IOException {
		byte[] data = getData(fs.getRoot(), "Envelope");
		
		System.out.println("");
		System.out.println("Envelope - " + data.length + " bytes long:");
	}
	
	public void dumpContents() throws IOException {
		byte[] data = getData(fs.getRoot(), "Contents");
		
		System.out.println("");
		System.out.println("Contents - " + data.length + " bytes long:");
		
		// 8 bytes, always seems to be
		// E8 AC 2C 00 E8 03 05 01 
		// E8 AC 2C 00 E8 03 05 01  
		
		// 4 bytes - size of contents
		// 13/15 00 00 01
		
		// ....
		
	    // E8 03 08 08 0C 20 03 00 00 00 00 88 16 00 00 00 ..... ..........
		
	    // 01 18 27 00 03 20 00 00 E8 03 08 08 0C 20 03 00 ..'.. ....... ..
		
		// 01 18 30 00 03 20 00 00 
		// E8 03 06 08 07 08 08 08 09 10 01 00 0C 20 04 00
		// 00 00 00 88 1E 00 00 00
		
		// 01 18 31 00 03 20 00 00
		// E8 03 06 08 07 08 08 08 09 10 01 00 0C 20 04 00
		// 00 00 00 88 1E 00 00 00
		
		// 01 18 32 00 03 20 00 00
		// E8 03 06 08 07 08 08 08 09 10 01 00 0C 20 04 00
		// 00 00 00 88 1E 00 00 00
	}
	
	public void dumpCONTENTS(DirectoryNode dir) throws IOException {
		byte[] data = getData(dir, "CONTENTS");
		
		System.out.println("");
		System.out.println("CONTENTS - " + data.length + " bytes long:");
		
		// Dump out up to 0x200
		
		// Text from 0x200 onwards for a bit
	}
	
	protected void dump001CompObj(DirectoryNode dir) {
		// TODO
	}
	
	public void dumpQuill() throws IOException {
		DirectoryNode quillDir = (DirectoryNode) 
			fs.getRoot().getEntry("Quill");
		DirectoryNode quillSubDir = (DirectoryNode) 
			quillDir.getEntry("QuillSub");

		dump001CompObj(quillSubDir);
		dumpCONTENTS(quillSubDir);
	}
}
