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
import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.formula.AreaErrPtg;
import org.apache.poi.hssf.record.formula.AreaPtg;
import org.apache.poi.hssf.record.formula.FormulaShifter;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * CFRecordsAggregate - aggregates Conditional Formatting records CFHeaderRecord 
 * and number of up to three CFRuleRecord records together to simplify
 * access to them.  
 * 
 * @author Dmitriy Kumshayev
 *
 */
public final class CFRecordsAggregate extends RecordAggregate {
	/** Excel allows up to 3 conditional formating rules */
	private static final int MAX_CONDTIONAL_FORMAT_RULES = 3;

	private final CFHeaderRecord header;

	/** List of CFRuleRecord objects */
	private final List rules;

	private CFRecordsAggregate(CFHeaderRecord pHeader, CFRuleRecord[] pRules) {
		if(pHeader == null) {
			throw new IllegalArgumentException("header must not be null");
		}
		if(pRules == null) {
			throw new IllegalArgumentException("rules must not be null");
		}
		if(pRules.length > MAX_CONDTIONAL_FORMAT_RULES) {
			throw new IllegalArgumentException("No more than " 
					+ MAX_CONDTIONAL_FORMAT_RULES + " rules may be specified");
		}
		if (pRules.length != pHeader.getNumberOfConditionalFormats()) {
			throw new RuntimeException("Mismatch number of rules");
		}
		header = pHeader;
		rules = new ArrayList(3);
		for (int i = 0; i < pRules.length; i++) {
			rules.add(pRules[i]);
		}
	}

	public CFRecordsAggregate(CellRangeAddress[] regions, CFRuleRecord[] rules) {
		this(new CFHeaderRecord(regions, rules.length), rules);
	}

	/**
	 * Create CFRecordsAggregate from a list of CF Records
	 * @param rs - the stream to read from
	 * @return CFRecordsAggregate object
	 */
	public static CFRecordsAggregate createCFAggregate(RecordStream rs) {
		Record rec = rs.getNext();
		if (rec.getSid() != CFHeaderRecord.sid) {
			throw new IllegalStateException("next record sid was " + rec.getSid() 
					+ " instead of " + CFHeaderRecord.sid + " as expected");
		}

		CFHeaderRecord header = (CFHeaderRecord)rec;
		int nRules = header.getNumberOfConditionalFormats();

		CFRuleRecord[] rules = new CFRuleRecord[nRules];
		for (int i = 0; i < rules.length; i++) {
			rules[i] = (CFRuleRecord) rs.getNext();
		}
		
		return new CFRecordsAggregate(header, rules);
	}

	/**
	 * Create a deep clone of the record
	 */
	public CFRecordsAggregate cloneCFAggregate()
	{
	  
		CFRuleRecord[] newRecs = new CFRuleRecord[rules.size()];
		for (int i = 0; i < newRecs.length; i++) {
			newRecs[i] = (CFRuleRecord) getRule(i).clone();
		}
		return new CFRecordsAggregate((CFHeaderRecord) header.clone(), newRecs);
	}

	/**
	 * @return the header. Never <code>null</code>.
	 */
	public CFHeaderRecord getHeader()
	{
		return header;
	}
	
	private void checkRuleIndex(int idx) {
		if(idx < 0 || idx >= rules.size()) {
			throw new IllegalArgumentException("Bad rule record index (" + idx 
					+ ") nRules=" + rules.size());
		}
	}
	public CFRuleRecord getRule(int idx) {
		checkRuleIndex(idx);
		return (CFRuleRecord) rules.get(idx);
	}
	public void setRule(int idx, CFRuleRecord r) {
		if (r == null) {
			throw new IllegalArgumentException("r must not be null");
		}
		checkRuleIndex(idx);
		rules.set(idx, r);
	}
	public void addRule(CFRuleRecord r) {
		if (r == null) {
			throw new IllegalArgumentException("r must not be null");
		}
		if(rules.size() >= MAX_CONDTIONAL_FORMAT_RULES) {
			throw new IllegalStateException("Cannot have more than " 
					+ MAX_CONDTIONAL_FORMAT_RULES + " conditional format rules");
		}
		rules.add(r);
		header.setNumberOfConditionalFormats(rules.size());
	}
	public int getNumberOfRules() {
		return rules.size();
	}

	/**
	 * String representation of CFRecordsAggregate
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("[CF]\n");
		if( header != null )
		{
			buffer.append(header.toString());
		}
		for(int i=0; i<rules.size(); i++)
		{
			CFRuleRecord cfRule = (CFRuleRecord)rules.get(i);
			buffer.append(cfRule.toString());
		}
		buffer.append("[/CF]\n");
		return buffer.toString();
	}

	public void visitContainedRecords(RecordVisitor rv) {
		rv.visitRecord(header);
		for(int i=0; i<rules.size(); i++) {
			CFRuleRecord rule = (CFRuleRecord)rules.get(i);
			rv.visitRecord(rule);
		}
	}

	/**
	 * @return <code>false</code> if this whole {@link CFHeaderRecord} / {@link CFRuleRecord}s should be deleted
	 */
	public boolean updateFormulasAfterCellShift(FormulaShifter shifter, int currentExternSheetIx) {
		CellRangeAddress[] cellRanges = header.getCellRanges();
		boolean changed = false;
		List temp = new ArrayList();
		for (int i = 0; i < cellRanges.length; i++) {
			CellRangeAddress craOld = cellRanges[i];
			CellRangeAddress craNew = shiftRange(shifter, craOld, currentExternSheetIx);
			if (craNew == null) {
				changed = true;
				continue;
			}
			temp.add(craNew);
			if (craNew != craOld) {
				changed = true;
			}
		}

		if (changed) {
			int nRanges = temp.size();
			if (nRanges == 0) {
				return false;
			}
			CellRangeAddress[] newRanges = new CellRangeAddress[nRanges];
			temp.toArray(newRanges);
			header.setCellRanges(newRanges);
		}
		
		for(int i=0; i<rules.size(); i++) {
			CFRuleRecord rule = (CFRuleRecord)rules.get(i);
			Ptg[] ptgs;
			ptgs = rule.getParsedExpression1();
			if (ptgs != null && shifter.adjustFormula(ptgs, currentExternSheetIx)) {
				rule.setParsedExpression1(ptgs);
			}
			ptgs = rule.getParsedExpression2();
			if (ptgs != null && shifter.adjustFormula(ptgs, currentExternSheetIx)) {
				rule.setParsedExpression2(ptgs);
			}
		}
		return true;
	}

	private static CellRangeAddress shiftRange(FormulaShifter shifter, CellRangeAddress cra, int currentExternSheetIx) {
		// FormulaShifter works well in terms of Ptgs - so convert CellRangeAddress to AreaPtg (and back) here
		AreaPtg aptg = new AreaPtg(cra.getFirstRow(), cra.getLastRow(), cra.getFirstColumn(), cra.getLastColumn(), false, false, false, false);
		Ptg[] ptgs = { aptg, };
		
		if (!shifter.adjustFormula(ptgs, currentExternSheetIx)) {
			return cra;
		}
		Ptg ptg0 = ptgs[0];
		if (ptg0 instanceof AreaPtg) {
			AreaPtg bptg = (AreaPtg) ptg0;
			return new CellRangeAddress(bptg.getFirstRow(), bptg.getLastRow(), bptg.getFirstColumn(), bptg.getLastColumn());
		}
		if (ptg0 instanceof AreaErrPtg) {
			return null;
		}
		throw new IllegalStateException("Unexpected shifted ptg class (" + ptg0.getClass().getName() + ")");
	}
}
