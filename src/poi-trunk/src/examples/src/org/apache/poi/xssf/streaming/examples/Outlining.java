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

package org.apache.poi.xssf.streaming.examples;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class Outlining {

	public static void main(String[] args) throws IOException {
		Outlining o = new Outlining();
		o.collapseRow();
	}

	private void collapseRow() throws IOException {
		try (SXSSFWorkbook wb2 = new SXSSFWorkbook(100)) {
			SXSSFSheet sheet2 = wb2.createSheet("new sheet");

			int rowCount = 20;
			for (int i = 0; i < rowCount; i++) {
				sheet2.createRow(i);
			}

			sheet2.groupRow(4, 9);
			sheet2.groupRow(11, 19);

			sheet2.setRowGroupCollapsed(4, true);

			try (FileOutputStream fileOut = new FileOutputStream("outlining_collapsed.xlsx")) {
				wb2.write(fileOut);
			} finally {
				wb2.dispose();
			}
		}
	}
}
