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

import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.hpbf.model.QuillContents;
import org.apache.poi.hpbf.model.qcbits.QCBit;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;

/**
 * For dumping out the PLC contents of QC Bits of a
 *  HPBF (Publisher) file, while we try to figure out
 *  what the format of them is.
 */
public final class PLCDumper {
	private HPBFDocument doc;
	private QuillContents qc;

	public PLCDumper(HPBFDocument hpbfDoc) {
		doc = hpbfDoc;
		qc = doc.getQuillContents();
	}
	public PLCDumper(POIFSFileSystem fs) throws IOException {
		this(new HPBFDocument(fs));
	}
	public PLCDumper(InputStream inp) throws IOException {
		this(new POIFSFileSystem(inp));
	}

	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  PLCDumper <filename>");
			System.exit(1);
		}
		PLCDumper dump = new PLCDumper(
				new FileInputStream(args[0])
		);

		System.out.println("Dumping " + args[0]);
		dump.dumpPLC();
	}

	private void dumpPLC() {
		QCBit[] bits = qc.getBits();

		for(int i=0; i<bits.length; i++) {
			if(bits[i] == null) continue;
			if(bits[i].getBitType().equals("PLC ")) {
				dumpBit(bits[i], i);
			}
		}
	}

	private void dumpBit(QCBit bit, int index) {
		System.out.println("");
		System.out.println("Dumping " + bit.getBitType() + " bit at " + index);
		System.out.println("  Is a " + bit.getThingType() + ", number is " + bit.getOptA());
		System.out.println("  Starts at " + bit.getDataOffset() + " (0x" + Integer.toHexString(bit.getDataOffset()) + ")");
		System.out.println("  Runs for  " + bit.getLength() + " (0x" + Integer.toHexString(bit.getLength()) + ")");

		System.out.println(HexDump.dump(bit.getData(), 0, 0));
	}
}
