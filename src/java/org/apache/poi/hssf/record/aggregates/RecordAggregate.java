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

package org.apache.poi.hssf.record.aggregates;

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordInputStream;

/**
 * <tt>RecordAggregate</tt>s are groups of of BIFF <tt>Record</tt>s that are typically stored 
 * together and/or updated together.  Workbook / Sheet records are typically stored in a sequential
 * list, which does not provide much structure to coordinate updates.
 * 
 * @author Josh Micich
 */
public abstract class RecordAggregate extends Record {
	// TODO - convert existing aggregate classes to proper subclasses of this one
	protected final void validateSid(short id) {
		// TODO - break class hierarchy and make separate from Record
		throw new RuntimeException("Should not be called");
	}
	protected final void fillFields(RecordInputStream in) {
		throw new RuntimeException("Should not be called");
	}
	// force subclassses to provide better implementation than default
	public abstract int getRecordSize();
}
