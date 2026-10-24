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

package org.apache.poi.ss.formula;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link org.apache.poi.ss.formula.CellCacheEntry}.
 */
class TestCellCacheEntry {

    @Test
    void testBasic() {
        CellCacheEntry pcce = new PlainValueCellCacheEntry(new NumberEval(42.0));
        ValueEval ve = pcce.getValue();
        assertEquals(42, ((NumberEval)ve).getNumberValue(), 0.0);

        FormulaCellCacheEntry fcce = new FormulaCellCacheEntry();
        fcce.updateFormulaResult(new NumberEval(10.0), CellCacheEntry.EMPTY_ARRAY, null);

        ve = fcce.getValue();
        assertEquals(10, ((NumberEval)ve).getNumberValue(), 0.0);
    }

    @Test
    void consumingCellsAreRegisteredOnDemand() {
        CellCacheEntry input = new PlainValueCellCacheEntry(new NumberEval(1));
        assertThat(input.getConsumingCells(), emptyArray());
        FormulaCellCacheEntry consumer = new FormulaCellCacheEntry();
        assertThrows(IllegalStateException.class, () -> input.clearConsumingCell(consumer));

        assertTrue(input.addConsumingCell(consumer));
        assertFalse(input.addConsumingCell(consumer), "second registration of the same consumer");
        assertThat(input.getConsumingCells(), arrayContaining(consumer));

        input.clearConsumingCell(consumer);
        assertThat(input.getConsumingCells(), emptyArray());
        assertThrows(IllegalStateException.class, () -> input.clearConsumingCell(consumer));
    }

    @Test
    void sensitiveInputCellsListedTwiceAreKeptOnce() {
        // a formula like A1*A1 reads the same cell twice; the entry must register itself once
        // and unregister itself once, or the second unregistration would fail
        CellCacheEntry a1 = new PlainValueCellCacheEntry(new NumberEval(3));
        CellCacheEntry b1 = new PlainValueCellCacheEntry(new NumberEval(4));
        FormulaCellCacheEntry fcce = new FormulaCellCacheEntry();
        fcce.updateFormulaResult(new NumberEval(13.0), new CellCacheEntry[] { a1, a1, b1, a1 }, null);
        assertThat(a1.getConsumingCells(), arrayContaining(fcce));
        assertThat(b1.getConsumingCells(), arrayContaining(fcce));
        assertTrue(fcce.isInputSensitive());

        // re-evaluating with a different set of inputs unregisters from the dropped ones
        CellCacheEntry c1 = new PlainValueCellCacheEntry(new NumberEval(5));
        fcce.updateFormulaResult(new NumberEval(8.0), new CellCacheEntry[] { a1, c1 }, null);
        assertThat(a1.getConsumingCells(), arrayContaining(fcce));
        assertThat(b1.getConsumingCells(), emptyArray());
        assertThat(c1.getConsumingCells(), arrayContaining(fcce));

        fcce.clearFormulaEntry();
        assertThat(a1.getConsumingCells(), emptyArray());
        assertThat(c1.getConsumingCells(), emptyArray());
        assertFalse(fcce.isInputSensitive());
        // clearing again is harmless
        fcce.clearFormulaEntry();
    }

    @Test
    void invalidationWalkClearsEveryDependentAndEveryRegistration() {
        // plain cell P is read by formulas F1 and F2; F1 is read by F2 and F3 (a diamond, so F2
        // is reachable twice); F3 also reads itself (a circular reference caches its own error)
        CellCacheEntry p = new PlainValueCellCacheEntry(new NumberEval(1));
        FormulaCellCacheEntry f1 = new FormulaCellCacheEntry();
        FormulaCellCacheEntry f2 = new FormulaCellCacheEntry();
        FormulaCellCacheEntry f3 = new FormulaCellCacheEntry();
        f1.updateFormulaResult(new NumberEval(2), new CellCacheEntry[] { p }, null);
        f2.updateFormulaResult(new NumberEval(3), new CellCacheEntry[] { p, f1 }, null);
        f3.updateFormulaResult(new NumberEval(4), new CellCacheEntry[] { f1, f3 }, null);
        assertThat(p.getConsumingCells(), arrayContainingInAnyOrder(f1, f2));
        assertThat(f1.getConsumingCells(), arrayContainingInAnyOrder(f2, f3));
        assertThat(f3.getConsumingCells(), arrayContainingInAnyOrder(f3));

        p.recurseClearCachedFormulaResults(null);

        for (FormulaCellCacheEntry f : new FormulaCellCacheEntry[] { f1, f2, f3 }) {
            assertNull(f.getValue());
            assertFalse(f.isInputSensitive());
            assertThat(f.getConsumingCells(), emptyArray());
        }
        assertThat(p.getConsumingCells(), emptyArray());
        // the plain cell keeps its own value: only the formulas depending on it are stale
        assertEquals(1, ((NumberEval) p.getValue()).getNumberValue(), 0);
    }

    @Test
    void invalidationWalkOverManyConsumers() {
        CellCacheEntry p = new PlainValueCellCacheEntry(new NumberEval(1));
        FormulaCellCacheEntry[] consumers = new FormulaCellCacheEntry[500];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new FormulaCellCacheEntry();
            consumers[i].updateFormulaResult(new NumberEval(i), new CellCacheEntry[] { p }, null);
        }
        p.recurseClearCachedFormulaResults(null);
        assertThat(p.getConsumingCells(), emptyArray());
        for (FormulaCellCacheEntry f : consumers) {
            assertNull(f.getValue());
        }
    }
}
