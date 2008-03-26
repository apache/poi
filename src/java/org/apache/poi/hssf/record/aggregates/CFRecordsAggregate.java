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
	public final static short sid = -2008; // not a real BIFF record

	private static POILogger  log = POILogFactory.getLogger(CFRecordsAggregate.class);

	private CFHeaderRecord header;

	// List of CFRuleRecord objects
	private final List rules;

	public CFRecordsAggregate()
	{
		header = null;
		rules  = new ArrayList(3);
	}

	/**
	 * Create CFRecordsAggregate from a list of CF Records
	 * @param recs - list of {@link Record} objects
	 * @param offset - position of {@link CFHeaderRecord} object in the list of Record objects
	 * @return CFRecordsAggregate object
	 */
	public static CFRecordsAggregate createCFAggregate(List recs, int pOffset)
	{

		int offset = pOffset;
		CFRecordsAggregate 	cfRecords  = new CFRecordsAggregate();
		ArrayList 		records = new ArrayList(4);

		Record rec = ( Record ) recs.get(offset++);

		if (rec.getSid() == CFHeaderRecord.sid)
		{
			records.add(rec);
			cfRecords.header = (CFHeaderRecord)rec;

			int nRules = cfRecords.header.getNumberOfConditionalFormats();
			int rulesCount = 0;
			while(  offset<recs.size() &&
					(rec = (Record)recs.get(offset++)).getSid() == CFRuleRecord.sid && 
					rec instanceof CFRuleRecord &&
					rulesCount++ < nRules
				 )
			{
				records.add(rec);
				cfRecords.rules.add(rec);
			}

			if (nRules != cfRecords.rules.size())
			{
				if (log.check(POILogger.DEBUG))
				{
					log.log(POILogger.DEBUG, "Expected  " + nRules + " Conditional Formats, "
							+ "but found " + cfRecords.rules.size() + " rules");
				}
				cfRecords.header.setNumberOfConditionalFormats(nRules);
			}

		}
		return cfRecords;
	}

	/**
	 * Create a deep clone of the record
	 * @return
	 */
	public CFRecordsAggregate cloneCFAggregate()
	{

	  ArrayList records = new ArrayList(this.rules.size()+1);
	  
	  records.add(this.header.clone());
	  
	  for (int i=0; i<this.rules.size();i++) 
	  {
		Record rec = (Record)((Record)this.rules.get(i)).clone();
		records.add(rec);
	  }
	  return createCFAggregate(records, 0);
	}

	/** You never fill an aggregate */
	protected void fillFields(RecordInputStream in)
	{
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
		int pos = offset;
		if( header != null && rules.size()>0 )
		{
			header.setNumberOfConditionalFormats(rules.size());

			pos += (( Record ) header).serialize(pos, data);

			for(Iterator itr = rules.iterator(); itr.hasNext();)
			{
				pos += (( Record ) itr.next()).serialize(pos, data);
			}
		}
		return pos - offset;
	}

	protected void validateSid(short id)
	{
		// do nothing here
	}

	/**
	 * @return the header
	 */
	public CFHeaderRecord getHeader()
	{
		return header;
	}

	/**
	 * @return the rules
	 */
	public List getRules()
	{
		return rules;
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
