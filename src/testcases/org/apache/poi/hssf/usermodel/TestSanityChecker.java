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
package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.hssf.record.*;

/**
 * Okay, this may seem strange but I need to test my test logic.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestSanityChecker
        extends TestCase
{
    public TestSanityChecker( String s )
    {
        super( s );
    }

    public void testCheckRecordOrder()
            throws Exception
    {
        final SanityChecker c = new SanityChecker();
        List records = new ArrayList();
        records.add(new BOFRecord());
        records.add(new InterfaceHdrRecord());
        records.add(new BoundSheetRecord());
        records.add(new EOFRecord());
        final SanityChecker.CheckRecord[] check = {
            new SanityChecker.CheckRecord(BOFRecord.class, '1'),
            new SanityChecker.CheckRecord(InterfaceHdrRecord.class, '0'),
            new SanityChecker.CheckRecord(BoundSheetRecord.class, 'M'),
            new SanityChecker.CheckRecord(NameRecord.class, '*'),
            new SanityChecker.CheckRecord(EOFRecord.class, '1'),
        };
        // check pass
        c.checkRecordOrder(records, check);
        records.add(2, new BoundSheetRecord());
        c.checkRecordOrder(records, check);
        records.remove(1);      // optional record missing
        c.checkRecordOrder(records, check);
        records.add(3, new NameRecord());
        records.add(3, new NameRecord()); // optional multiple record occurs more than one time
        c.checkRecordOrder(records, check);

        // check fail
        expectFail( new Runnable() {
            public void run()
            {
                // check optional in wrong spot
                List records = new ArrayList();
                records.add(new BOFRecord());
                records.add(new BoundSheetRecord());
                records.add(new InterfaceHdrRecord());
                records.add(new EOFRecord());
                c.checkRecordOrder(records, check);
            }
        });

        expectFail( new Runnable() {
            public void run()
            {
                // check optional one off occurs more than once
                List records = new ArrayList();
                records.add(new BOFRecord());
                records.add(new InterfaceHdrRecord());
                records.add(new BoundSheetRecord());
                records.add(new InterfaceHdrRecord());
                records.add(new EOFRecord());
                c.checkRecordOrder(records, check);
            }
        });

        expectFail( new Runnable() {
            public void run()
            {
                // check many scattered
                List records = new ArrayList();
                records.add(new BOFRecord());
                records.add(new BoundSheetRecord());
                records.add(new NameRecord());
                records.add(new EOFRecord());
                records.add(new NameRecord());
                c.checkRecordOrder(records, check);
            }
        });

        expectFail( new Runnable() {
            public void run()
            {
                // check missing manditory
                List records = new ArrayList();
                records.add(new InterfaceHdrRecord());
                records.add(new BoundSheetRecord());
                records.add(new EOFRecord());
                c.checkRecordOrder(records, check);
            }
        });

        expectFail( new Runnable() {
            public void run()
            {
                // check missing 1..many
                List records = new ArrayList();
                records.add(new BOFRecord());
                records.add(new InterfaceHdrRecord());
                records.add(new EOFRecord());
                c.checkRecordOrder(records, check);
            }
        });

        expectFail( new Runnable() {
            public void run()
            {
                // check wrong order
                List records = new ArrayList();
                records.add(new InterfaceHdrRecord());
                records.add(new BoundSheetRecord());
                records.add(new BOFRecord());
                records.add(new EOFRecord());
                c.checkRecordOrder(records, check);
            }
        });

        expectFail( new Runnable() {
            public void run()
            {
                // check optional record in wrong order
                List records = new ArrayList();
                records.add(new BOFRecord());
                records.add(new BoundSheetRecord());
                records.add(new InterfaceHdrRecord());
                records.add(new EOFRecord());
                c.checkRecordOrder(records, check);
            }
        });

    }

    private void expectFail( Runnable runnable )
    {
        boolean fail = false;
        try
        {
            runnable.run();
            fail = true;
        }
        catch (AssertionFailedError pass)
        {
        }
        assertTrue(!fail);
    }

}

