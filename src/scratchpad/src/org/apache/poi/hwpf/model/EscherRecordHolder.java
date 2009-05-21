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

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ddf.DefaultEscherRecordFactory;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherRecordFactory;

/**
 * Based on AbstractEscherRecordHolder from HSSF.
 *
 * @author Squeeself
 */
public final class EscherRecordHolder {
	private final ArrayList<EscherRecord> escherRecords;

	public EscherRecordHolder() {
		escherRecords = new ArrayList<EscherRecord>();
	}

	public EscherRecordHolder(byte[] data, int offset, int size) {
		this();
		fillEscherRecords(data, offset, size);
	}

	private void fillEscherRecords(byte[] data, int offset, int size)
	{
		EscherRecordFactory recordFactory = new DefaultEscherRecordFactory();
		int pos = offset;
		while ( pos < offset + size)
		{
			EscherRecord r = recordFactory.createRecord(data, pos);
			escherRecords.add(r);
			int bytesRead = r.fillFields(data, pos, recordFactory);
			pos += bytesRead + 1; // There is an empty byte between each top-level record in a Word doc
		}
	}

	public List<EscherRecord> getEscherRecords() {
		return escherRecords;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		if (escherRecords.size() == 0) {
			buffer.append("No Escher Records Decoded").append("\n");
		}
		Iterator<EscherRecord> iterator = escherRecords.iterator();
		while (iterator.hasNext()) {
			EscherRecord r = iterator.next();
			buffer.append(r.toString());
		}
		return buffer.toString();
	}

	/**
	 * If we have a EscherContainerRecord as one of our
	 *  children (and most top level escher holders do),
	 *  then return that.
	 */
	public EscherContainerRecord getEscherContainer() {
		for(Iterator<EscherRecord> it = escherRecords.iterator(); it.hasNext();) {
			Object er = it.next();
			if(er instanceof EscherContainerRecord) {
				return (EscherContainerRecord)er;
			}
		}
		return null;
	}

	/**
	 * Descends into all our children, returning the
	 *  first EscherRecord with the given id, or null
	 *  if none found
	 */
	public EscherRecord findFirstWithId(short id) {
		return findFirstWithId(id, getEscherRecords());
	}
	private static EscherRecord findFirstWithId(short id, List<EscherRecord> records) {
		// Check at our level
		for(Iterator<EscherRecord> it = records.iterator(); it.hasNext();) {
			EscherRecord r = it.next();
			if(r.getRecordId() == id) {
				return r;
			}
		}

		// Then check our children in turn
		for(Iterator<EscherRecord> it = records.iterator(); it.hasNext();) {
			EscherRecord r = it.next();
			if(r.isContainerRecord()) {
				EscherRecord found = findFirstWithId(id, r.getChildRecords());
				if(found != null) {
					return found;
				}
			}
		}

		// Not found in this lot
		return null;
	}
}
