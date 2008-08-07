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

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.hssf.record.*;

/**
 * A Test case for a test utility class.<br/>
 * Okay, this may seem strange but I need to test my test logic.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSanityChecker extends TestCase {

    public void testCheckRecordOrder() {
        final SanityChecker c = new SanityChecker();
        List records = new ArrayList();
        records.add(new BOFRecord());
        records.add(new InterfaceHdrRecord());
        records.add(new BoundSheetRecord());
        records.add(EOFRecord.instance);
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
                records.add(EOFRecord.instance);
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
                records.add(EOFRecord.instance);
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
                records.add(EOFRecord.instance);
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
                records.add(EOFRecord.instance);
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
                records.add(EOFRecord.instance);
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
                records.add(EOFRecord.instance);
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
                records.add(EOFRecord.instance);
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

