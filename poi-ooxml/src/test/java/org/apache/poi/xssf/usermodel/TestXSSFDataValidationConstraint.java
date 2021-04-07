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
package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.poi.ss.formula.DataValidationEvaluator;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.IntStream;

class TestXSSFDataValidationConstraint {
    static final int listType = ValidationType.LIST;
    static final int ignoredType = OperatorType.IGNORED;

    // See bug 59719
    @Test
    void listLiteralsQuotesAreStripped_formulaConstructor() {
        // literal list, using formula constructor
        String literal = "\"one, two, three\"";
        String[] expected = new String[] { "one", "two", "three" };
        DataValidationConstraint constraint = new XSSFDataValidationConstraint(listType, ignoredType, literal, null);
        assertArrayEquals(expected, constraint.getExplicitListValues());
        // Excel and DataValidationConstraint parser ignore (strip) whitespace; quotes should still be intact
        // FIXME: whitespace wasn't stripped
        assertEquals(literal, constraint.getFormula1());
    }

    @Test
    void listLiteralsQuotesAreStripped_arrayConstructor() {
        // literal list, using array constructor
        String literal = "\"one, two, three\"";
        String[] expected = new String[] { "one", "two", "three" };
        DataValidationConstraint constraint = new XSSFDataValidationConstraint(expected);
        assertArrayEquals(expected, constraint.getExplicitListValues());
        // Excel and DataValidationConstraint parser ignore (strip) whitespace; quotes should still be intact
        assertEquals(literal.replace(" ", ""), constraint.getFormula1());
    }

    @Test
    void listLiteralsGreaterThan255CharactersThrows() {
        String[] literal = IntStream.range(0, 129).mapToObj(i -> "a").toArray(String[]::new);
        assertThrows(IllegalArgumentException.class, () -> new XSSFDataValidationConstraint(literal));
    }

    @Test
    void dataValidationListLiteralTooLongFromFile() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("DataValidationListTooLong.xlsx")) {
            XSSFFormulaEvaluator fEval = wb.getCreationHelper().createFormulaEvaluator();
            DataValidationEvaluator dvEval = new DataValidationEvaluator(wb, fEval);
            assertThrows(IllegalArgumentException.class, () -> dvEval.getValidationValuesForCell(new CellReference("Sheet0!A1")));
        }
    }

    @Test
    void rangeReference() {
        // (unnamed range) reference list
        String reference = "A1:A5";
        DataValidationConstraint constraint = new XSSFDataValidationConstraint(listType, ignoredType, reference, null);
        assertNull(constraint.getExplicitListValues());
        assertEquals("A1:A5", constraint.getFormula1());
    }

    @Test
    void namedRangeReference() {
        // named range list
        String namedRange = "MyNamedRange";
        DataValidationConstraint constraint = new XSSFDataValidationConstraint(listType, ignoredType, namedRange, null);
        assertNull(constraint.getExplicitListValues());
        assertEquals("MyNamedRange", constraint.getFormula1());
    }

}
