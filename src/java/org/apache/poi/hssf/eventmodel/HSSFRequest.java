
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

package org.apache.poi.hssf.eventmodel;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFactory;

/**
 * An HSSFRequest object should be constructed registering an instance or multiple
 * instances of HSSFListener with each Record.sid you wish to listen for.
 *
 * @see org.apache.poi.hssf.eventmodel.HSSFEventFactory
 * @see org.apache.poi.hssf.eventmodel.HSSFListener
 * @see org.apache.poi.hssf.dev.EFHSSF
 * @author  andy
 */

public class HSSFRequest
{
    private HashMap records;

    /** Creates a new instance of HSSFRequest */

    public HSSFRequest()
    {
        records =
            new HashMap(50);   // most folks won't listen for too many of these
    }

    /**
     * add an event listener for a particular record type.  The trick is you have to know
     * what the records are for or just start with our examples and build on them.  Alternatively,
     * you CAN call addListenerForAllRecords and you'll recieve ALL record events in one listener,
     * but if you like to squeeze every last byte of efficiency out of life you my not like this.
     * (its sure as heck what I plan to do)
     *
     * @see #addListenerForAllRecords(HSSFListener)
     *
     * @param lsnr      for the event
     * @param sid       identifier for the record type this is the .sid static member on the individual records
     *        for example req.addListener(myListener, BOFRecord.sid)
     */

    public void addListener(HSSFListener lsnr, short sid)
    {
        List   list = null;
        Object obj  = records.get(new Short(sid));

        if (obj != null)
        {
            list = ( List ) obj;
        }
        else
        {
            list = new ArrayList(
                1);   // probably most people will use one listener
            list.add(lsnr);
            records.put(new Short(sid), list);
        }
    }

    /**
     * This is the equivilent of calling addListener(myListener, sid) for EVERY
     * record in the org.apache.poi.hssf.record package. This is for lazy
     * people like me. You can call this more than once with more than one listener, but
     * that seems like a bad thing to do from a practice-perspective unless you have a
     * compelling reason to do so (like maybe you send the event two places or log it or
     * something?).
     *
     * @param lsnr      a single listener to associate with ALL records
     */

    public void addListenerForAllRecords(HSSFListener lsnr)
    {
        short[] rectypes = RecordFactory.getAllKnownRecordSIDs();

        for (int k = 0; k < rectypes.length; k++)
        {
            addListener(lsnr, rectypes[ k ]);
        }
    }

    /**
     * called by HSSFEventFactory, passes the Record to each listener associated with
     * a record.sid.
     */

    protected void processRecord(Record rec)
    {
        Object obj = records.get(new Short(rec.getSid()));

        if (obj != null)
        {
            List listeners = ( List ) obj;

            for (int k = 0; k < listeners.size(); k++)
            {
                HSSFListener listener = ( HSSFListener ) listeners.get(k);

                listener.processRecord(rec);
            }
        }
    }
}
