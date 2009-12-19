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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.*;

/**
 * Manages the all the records associated with a 'Custom View Settings' sub-stream.<br/>
 * Includes the initial USERSVIEWBEGIN(0x01AA) and final USERSVIEWEND(0x01AB).
 * 
 * @author Josh Micich
 */
public final class CustomViewSettingsRecordAggregate extends RecordAggregate {

	private final Record _begin;
	private final Record _end;
	/**
	 * All the records between BOF and EOF
	 */
	private final List<RecordBase> _recs;
	private PageSettingsBlock _psBlock;

	public CustomViewSettingsRecordAggregate(RecordStream rs) {
		_begin = rs.getNext();
		if (_begin.getSid() != UserSViewBegin.sid) {
			throw new IllegalStateException("Bad begin record");
		}
		List<RecordBase> temp = new ArrayList<RecordBase>();
		while (rs.peekNextSid() != UserSViewEnd.sid) {
			if (PageSettingsBlock.isComponentRecord(rs.peekNextSid())) {
				if (_psBlock != null) {
					throw new IllegalStateException(
							"Found more than one PageSettingsBlock in custom view settings sub-stream");
				}
				_psBlock = new PageSettingsBlock(rs);
				temp.add(_psBlock);
				continue;
			}
			temp.add(rs.getNext());
		}
		_recs = temp;
		_end = rs.getNext(); // no need to save EOF in field
		if (_end.getSid() != UserSViewEnd.sid) {
			throw new IllegalStateException("Bad custom view settings end record");
		}
	}

	public void visitContainedRecords(RecordVisitor rv) {
		if (_recs.isEmpty()) {
			return;
		}
		rv.visitRecord(_begin);
		for (int i = 0; i < _recs.size(); i++) {
			RecordBase rb = _recs.get(i);
			if (rb instanceof RecordAggregate) {
				((RecordAggregate) rb).visitContainedRecords(rv);
			} else {
				rv.visitRecord((Record) rb);
			}
		}
		rv.visitRecord(_end);
	}

	public static boolean isBeginRecord(int sid) {
		return sid == UserSViewBegin.sid;
	}

    public void append(RecordBase r){
        _recs.add(r);    
    }
}
