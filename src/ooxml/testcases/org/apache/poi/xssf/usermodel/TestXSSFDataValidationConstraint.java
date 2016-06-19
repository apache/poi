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

import static org.junit.Assert.*;

import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.junit.Test;

public class TestXSSFDataValidationConstraint {

    // See bug 59719
    @Test
    public void listLiteralsQuotesAreStripped() {
        int listType = ValidationType.LIST;
        int ignoredType = OperatorType.IGNORED;
        
        String literal = "\"one, two, three\"";
        String[] expected = new String[] { "one", "two", "three" };
        DataValidationConstraint constraint = new XSSFDataValidationConstraint(listType, ignoredType, literal, null);
        assertArrayEquals(expected, constraint.getExplicitListValues());
        
        String reference = "A1:A5";
        constraint = new XSSFDataValidationConstraint(listType, ignoredType, reference, null);
        assertNull(constraint.getExplicitListValues());
        
        String namedRange = "MyNamedRange";
        constraint = new XSSFDataValidationConstraint(listType, ignoredType, namedRange, null);
        assertNull(constraint.getExplicitListValues());
    }

}        
