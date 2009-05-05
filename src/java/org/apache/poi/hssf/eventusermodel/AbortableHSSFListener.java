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


package org.apache.poi.hssf.eventusermodel;

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.eventusermodel.HSSFUserException;

/**
 * Abstract class for use with the HSSFRequest and HSSFEventFactory, which
 *  allows for the halting of processing. 
 * Users should create subclass of this (which implements the usual
 *  HSSFListener), and then override the #abortableProcessRecord(Record)
 *  method to do their processing.
 * This should then be registered with the HSSFRequest (associating
 * it with Record SID's) as usual.
 *
 * @see org.apache.poi.hssf.eventusermodel.HSSFEventFactory
 * @see org.apache.poi.hssf.eventusermodel.HSSFRequest
 * @see org.apache.poi.hssf.eventusermodel.HSSFUserException
 *
 * @author Carey Sublette (careysub@earthling.net)
 */

public abstract class AbortableHSSFListener implements HSSFListener
{
    /**
     * This method, inherited from HSSFListener is implemented as a stub.
     * It is never called by HSSFEventFactory or HSSFRequest.
     * You should implement #abortableProcessRecord instead
     */
	public void processRecord(Record record)
	{
	}

   /**
	 * Process an HSSF Record. Called when a record occurs in an HSSF file. 
	 * Provides two options for halting the processing of the HSSF file.
	 *
	 * The return value provides a means of non-error termination with a 
	 * user-defined result code. A value of zero must be returned to 
	 * continue processing, any other value will halt processing by
	 * <code>HSSFEventFactory</code> with the code being passed back by 
	 * its abortable process events methods.
	 * 
	 * Error termination can be done by throwing the HSSFUserException.
	 *
	 * Note that HSSFEventFactory will not call the inherited process 
	 *
     * @return result code of zero for continued processing.
     *
	 * @throws HSSFUserException User code can throw this to abort 
	 * file processing by HSSFEventFactory and return diagnostic information.
     */
    public abstract short abortableProcessRecord(Record record) throws HSSFUserException;
}
