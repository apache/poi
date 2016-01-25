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
package org.apache.poi.hssf.eventusermodel.old;

import org.apache.poi.hssf.record.*;

import java.io.InputStream;


/**
 * Interface for use with the {@link OldHSSFEventFactory#process(InputStream, OldHSSFListener)}.
 *
 * @see OldHSSFEventFactory
 */
public interface OldHSSFListener {

    /**
     * Process an {@link BOFRecord}. Called when a record occurs in an HSSF file.
     */
    void onBOFRecord(BOFRecord record, int biffVersion);

    /**
     * Process an {@link OldSheetRecord}. Called when a record occurs in an HSSF file.
     */
    void onOldSheetRecord(OldSheetRecord shr);

    /**
     * Process an {@link OldLabelRecord}. Called when a record occurs in an HSSF file.
     */
    void onOldLabelRecord(OldLabelRecord lr);

    /**
     * Process an {@link OldStringRecord}. Called when a record occurs in an HSSF file.
     */
    void onOldStringRecord(OldStringRecord sr);

    /**
     * Process an {@link NumberRecord}. Called when a record occurs in an HSSF file.
     */
    void onNumberRecord(NumberRecord nr);

    /**
     * Process an {@link FormulaRecord}. Called when a record occurs in an HSSF file.
     */
    void onFormulaRecord(FormulaRecord fr);

    /**
     * Process an {@link OldFormulaRecord}. Called when a record occurs in an HSSF file.
     */
    void onOldFormulaRecord(OldFormulaRecord fr);

    /**
     * Process an {@link RKRecord}. Called when a record occurs in an HSSF file.
     */
    void onRKRecord(RKRecord rr);

    /**
     * Process an {@link MulRKRecord}. Called when a record occurs in an HSSF file.
     */
    void onMulRKRecord(MulRKRecord mrr);

    /**
     * Called when no records left in in an HSSF file.
     */
    void onBookEnd();
}
