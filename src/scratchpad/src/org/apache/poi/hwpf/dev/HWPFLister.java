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

package org.apache.poi.hwpf.dev;

import java.io.FileInputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.FileInformationBlock;

/**
 * Used by developers to list out key information on a
 *  HWPF file. End users will probably never need to
 *  use this program.
 */
public final class HWPFLister {
	private final HWPFDocument _doc;
	public HWPFLister(HWPFDocument doc) {
		_doc = doc;
	}

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.err.println("Use:");
			System.err.println("   HWPFLister <filename>");
			System.exit(1);
		}

		HWPFLister l = new HWPFLister(
				new HWPFDocument(new FileInputStream(args[0]))
		);
		l.dumpFIB();
	}

	public void dumpFIB() {
		FileInformationBlock fib = _doc.getFileInformationBlock();
		System.out.println(fib.toString());
	}
}
