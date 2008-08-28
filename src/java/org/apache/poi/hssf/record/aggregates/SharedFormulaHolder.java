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

import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SharedFormulaRecord;

/**
 * Temporarily holds SharedFormulaRecords while constructing a <tt>RowRecordsAggregate</tt>
 * 
 * @author Josh Micich
 */
final class SharedFormulaHolder {

	private static final SharedFormulaHolder EMPTY = new SharedFormulaHolder(new SharedFormulaRecord[0]);
	private final SharedFormulaRecord[] _sfrs;

	/**
	 * @param recs list of sheet records (possibly contains records for other parts of the Excel file)
	 * @param startIx index of first row/cell record for current sheet
	 * @param endIx one past index of last row/cell record for current sheet.  It is important 
	 * that this code does not inadvertently collect <tt>SharedFormulaRecord</tt>s from any other
	 * sheet (which could happen if endIx is chosen poorly).  (see bug 44449) 
	 */
	public static SharedFormulaHolder create(List recs, int startIx, int endIx) {
		List temp = new ArrayList();
        for (int k = startIx; k < endIx; k++)
        {
            Record rec = ( Record ) recs.get(k);
            if (rec instanceof SharedFormulaRecord) {
                temp.add(rec);
            }
        }
        if (temp.size() < 1) {
        	return EMPTY;
        }
        SharedFormulaRecord[] sfrs = new SharedFormulaRecord[temp.size()];
        temp.toArray(sfrs);
        return new SharedFormulaHolder(sfrs);
        
	}
	private SharedFormulaHolder(SharedFormulaRecord[] sfrs) {
		_sfrs = sfrs;
	}
	public void convertSharedFormulaRecord(FormulaRecord formula) {
        // Traverse the list of shared formulas in
        //  reverse order, and try to find the correct one
        //  for us
        for (int i=0; i<_sfrs.length; i++) {
            SharedFormulaRecord shrd = _sfrs[i];
            if (shrd.isFormulaInShared(formula)) {
                shrd.convertSharedFormulaRecord(formula);
                return;
            }
        }
        // not found
        handleMissingSharedFormulaRecord(formula);
	}

    /**
     * Sometimes the shared formula flag "seems" to be erroneously set, in which case there is no 
     * call to <tt>SharedFormulaRecord.convertSharedFormulaRecord</tt> and hence the 
     * <tt>parsedExpression</tt> field of this <tt>FormulaRecord</tt> will not get updated.<br/>
     * As it turns out, this is not a problem, because in these circumstances, the existing value
     * for <tt>parsedExpression</tt> is perfectly OK.<p/>
     * 
     * This method may also be used for setting breakpoints to help diagnose issues regarding the
     * abnormally-set 'shared formula' flags. 
     * (see TestValueRecordsAggregate.testSpuriousSharedFormulaFlag()).<p/>
     * 
     * The method currently does nothing but do not delete it without finding a nice home for this 
     * comment.
     */
    private static void handleMissingSharedFormulaRecord(FormulaRecord formula) {
        // could log an info message here since this is a fairly unusual occurrence.
    }
}
