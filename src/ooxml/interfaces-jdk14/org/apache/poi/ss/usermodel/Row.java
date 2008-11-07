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

package org.apache.poi.ss.usermodel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import java.util.Iterator;

/**
 * This is a JDK 1.4 compatible interface for HSSFRow.
 * If you are using JDK 1.5 or later, use the other set of interfaces,
 *  which work properly for both HSSFRow and XSSFRow
 */
public interface Row {
    int getRowNum();
    short getFirstCellNum();
    short getLastCellNum();
    int getPhysicalNumberOfCells();
    HSSFCell getCell(int cellnum);

    Iterator cellIterator();

    /**
     * Used to specify the different possible policies
     *  if for the case of null and blank cells
     */
    public static class MissingCellPolicy {
    	private static int NEXT_ID = 1;
    	public final int id;
    	private MissingCellPolicy() {
    		this.id = NEXT_ID++;
    	}
    }
    /** Missing cells are returned as null, Blank cells are returned as normal */
    public static final MissingCellPolicy RETURN_NULL_AND_BLANK = new MissingCellPolicy();
    /** Missing cells are returned as null, as are blank cells */
    public static final MissingCellPolicy RETURN_BLANK_AS_NULL = new MissingCellPolicy();
    /** A new, blank cell is created for missing cells. Blank cells are returned as normal */
    public static final MissingCellPolicy CREATE_NULL_AS_BLANK = new MissingCellPolicy();
}
