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
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * CFRecordsAggregate - aggregates Conditional Formatting records CFHeaderRecord 
 * and number of up to three CFRuleRecord records together to simplify
 * access to them.  
 * 
 * @author Dmitriy Kumshayev
 *
 */
public final class CFRecordsAggregate extends Record
{
	/** Excel allows up to 3 conditional formating rules */
	private static final int MAX_CONDTIONAL_FORMAT_RULES = 3;

	public final static short sid = -2008; // not a real BIFF record

	private static POILogger log = POILogFactory.getLogger(CFRecordsAggregate.class);

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
		header = pHeader;
		rules = new ArrayList(3);
		for (int i = 0; i < pRules.length; i++) {
			rules.add(pRules[i]);
		}
	}

	public CFRecordsAggregate(Region[] regions, CFRuleRecord[] rules) {
		this(new CFHeaderRecord(regions), rules);
	}

	/**
	 * Create CFRecordsAggregate from a list of CF Records
	 * @param recs - list of {@link Record} objects
	 * @param offset - position of {@link CFHeaderRecord} object in the list of Record objects
	 * @return CFRecordsAggregate object
	 */
	public static CFRecordsAggregate createCFAggregate(List recs, int pOffset)
	{
		Record rec = ( Record ) recs.get(pOffset);
		if (rec.getSid() != CFHeaderRecord.sid) {
			throw new IllegalStateException("next record sid was " + rec.getSid() 
					+ " instead of " + CFHeaderRecord.sid + " as expected");
		}

		CFHeaderRecord header = (CFHeaderRecord)rec;
		int nRules = header.getNumberOfConditionalFormats();

		CFRuleRecord[] rules = new CFRuleRecord[nRules];
		int offset = pOffset;
		int countFound = 0;
		while (countFound < rules.length) {
			offset++;
			if(offset>=recs.size()) {
				break;
			}
			rec = (Record)recs.get(offset);
			if(rec instanceof CFRuleRecord) {
				rules[countFound] = (CFRuleRecord) rec;
				countFound++;
			} else {
				break;
			}
		}

		if (countFound < nRules)
		{ // TODO -(MAR-2008) can this ever happen? write junit 
			
			if (log.check(POILogger.DEBUG))
			{
				log.log(POILogger.DEBUG, "Expected  " + nRules + " Conditional Formats, "
						+ "but found " + countFound + " rules");
			}
			header.setNumberOfConditionalFormats(nRules);
			CFRuleRecord[] lessRules = new CFRuleRecord[countFound];
			System.arraycopy(rules, 0, lessRules, 0, countFound);
			rules = lessRules;
		}
		return new CFRecordsAggregate(header, rules);
	}

	/**
	 * Create a deep clone of the record
	 * @return
	 */
	public CFRecordsAggregate cloneCFAggregate()
	{
	  
		CFRuleRecord[] newRecs = new CFRuleRecord[rules.size()];
		for (int i = 0; i < newRecs.length; i++) {
			newRecs[i] = (CFRuleRecord) getRule(i).clone();
		}
		return new CFRecordsAggregate((CFHeaderRecord) header.clone(), newRecs);
	}

	protected void fillFields(RecordInputStream in)
	{
	     // You never fill an aggregate record
	}

	public short getSid()
	{
		return sid;
	}

	/**
	 * called by the class that is responsible for writing this sucker.
	 * Subclasses should implement this so that their data is passed back in a
	 * byte array.
	 *
	 * @param offset to begin writing at
	 * @param data byte array containing instance data
	 * @return number of bytes written
	 */

	public int serialize(int offset, byte[] data)
	{
		int nRules = rules.size();
		header.setNumberOfConditionalFormats(nRules);

		int pos = offset;

		pos += header.serialize(pos, data);
		for(int i=0; i< nRules; i++) {
			pos += getRule(i).serialize(pos, data);
		}
		return pos - offset;
	}

	protected void validateSid(short id)
	{
		// do nothing here
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
		checkRuleIndex(idx);
		rules.set(idx, r);
	}
	public void addRule(CFRuleRecord r) {
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
	 *  @return sum of sizes of all aggregated records
	 */
	public int getRecordSize() 
	{
		int size = 0;
		if( header != null)
		{
			size += header.getRecordSize();
		}
		if( rules != null)
		{
			for(Iterator irecs = rules.iterator(); irecs.hasNext(); )
			{
				size += (( Record ) irecs.next()).getRecordSize();
			}
		}
		return size;
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
			if(cfRule!=null)
			{
				buffer.append(cfRule.toString());
			}
		}
		buffer.append("[/CF]\n");
		return buffer.toString();
	}
}
