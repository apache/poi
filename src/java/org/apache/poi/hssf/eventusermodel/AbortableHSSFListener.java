
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.eventusermodel;

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.eventusermodel.HSSFUserException;

/**
 * Interface for use with the HSSFRequest and HSSFEventFactory.  Users should create
 * a listener supporting this interface and register it with the HSSFRequest (associating
 * it with Record SID's).
 *
 * @see org.apache.poi.hssf.eventmodel.HSSFEventFactory
 * @see org.apache.poi.hssf.eventmodel.HSSFRequest
 * @see org.apache.poi.hssf.HSSFUserException
 *
 * @author Carey Sublette (careysub@earthling.net)
 *
 */

public abstract class AbortableHSSFListener implements HSSFListener
{
    /**
     * This method, inherited from HSSFListener is implemented as a stub.
     * It is never called by HSSFEventFActory or HSSFRequest.
     *
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
