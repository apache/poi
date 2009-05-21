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

package org.apache.poi.hpbf.model;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.poifs.filesystem.DirectoryNode;

/**
 * Parent class of all Escher parts
 */
public abstract class EscherPart extends HPBFPart {
	private EscherRecord[] records;

	/**
	 * Creates the Escher Part, and finds our child
	 *  escher records
	 */
	public EscherPart(DirectoryNode baseDir, String[] parts) throws IOException {
		super(baseDir, parts);

		// Now create our Escher children
		DefaultEscherRecordFactory erf =
			new DefaultEscherRecordFactory();

		ArrayList ec = new ArrayList();
		int left = data.length;
		while(left > 0) {
			EscherRecord er = erf.createRecord(data, 0);
			er.fillFields(data, 0, erf);
			left -= er.getRecordSize();

			ec.add(er);
		}

		records = (EscherRecord[])
			ec.toArray(new EscherRecord[ec.size()]);
	}

	public EscherRecord[] getEscherRecords() {
		return records;
	}

	/**
	 * Serialises our Escher children back
	 *  into bytes.
	 */
	protected void generateData() {
		int size = 0;
		for(int i=0; i<records.length; i++) {
			size += records[i].getRecordSize();
		}

		data = new byte[size];
		size = 0;
		for(int i=0; i<records.length; i++) {
			int thisSize =
				records[i].serialize(size, data);
			size += thisSize;
		}
	}
}
