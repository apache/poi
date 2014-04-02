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

package org.apache.poi.hssf.record;

/**
 * Common base class of {@link Record} and {@link org.apache.poi.hssf.record.aggregates.RecordAggregate}
 * 
 * @author Josh Micich
 */
public abstract class RecordBase {
	/**
	 * called by the class that is responsible for writing this sucker.
	 * Subclasses should implement this so that their data is passed back in a
	 * byte array.
	 * 
	 * @param offset to begin writing at
	 * @param data byte array containing instance data
	 * @return number of bytes written
	 */
	public abstract int serialize(int offset, byte[] data);

	/**
	 * gives the current serialized size of the record. Should include the sid
	 * and reclength (4 bytes).
	 */
	public abstract int getRecordSize();
}
