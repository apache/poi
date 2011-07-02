/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.formula.IEvaluationListener.ICacheEntry;
import org.apache.poi.ss.formula.PlainCellCache.Loc;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellValue;

import java.io.PrintStream;
import java.util.*;

/**
 * @author Yegor Kozlov
 */
public class TestPlainCellCache extends TestCase {

    /**
     *
     */
    public void testLoc(){
        PlainCellCache cache = new PlainCellCache();
        for (int bookIndex = 0; bookIndex < 0x1000; bookIndex += 0x100) {
            for (int sheetIndex = 0; sheetIndex < 0x1000; sheetIndex += 0x100) {
                for (int rowIndex = 0; rowIndex < 0x100000; rowIndex += 0x1000) {
                    for (int columnIndex = 0; columnIndex < 0x4000; columnIndex += 0x100) {
                        Loc loc = new Loc(bookIndex, sheetIndex, rowIndex, columnIndex);
                        assertEquals(bookIndex, loc.getBookIndex());
                        assertEquals(sheetIndex, loc.getSheetIndex());
                        assertEquals(rowIndex, loc.getRowIndex());
                        assertEquals(columnIndex, loc.getColumnIndex());

                        Loc sameLoc = new Loc(bookIndex, sheetIndex, rowIndex, columnIndex);
                        assertEquals(loc.hashCode(), sameLoc.hashCode());
                        assertTrue(loc.equals(sameLoc));

                        assertNull(cache.get(loc));
                        PlainValueCellCacheEntry entry = new PlainValueCellCacheEntry(new NumberEval(0));
                        cache.put(loc, entry);
                        assertSame(entry, cache.get(loc));
                        cache.remove(loc);
                        assertNull(cache.get(loc));

                        cache.put(loc, entry);
                    }
                    cache.clear();
                }
            }

        }
    }
}
