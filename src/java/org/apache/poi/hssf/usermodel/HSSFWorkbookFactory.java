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

import java.io.IOException;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.Internal;

/**
 * Helper class which is instantiated by reflection from
 * {@link WorkbookFactory#create(java.io.File)} and similar
 */
@SuppressWarnings("unused")
@Internal
public class HSSFWorkbookFactory extends WorkbookFactory {
    /**
     * Create a new empty Workbook
     *
     * @return The created workbook
     */
    public static HSSFWorkbook createWorkbook() {
        return new HSSFWorkbook();
    }

    /**
     * Creates a HSSFWorkbook from the given {@link POIFSFileSystem}<p>
     * Note that in order to properly release resources the
     * Workbook should be closed after use.
     */
    public static HSSFWorkbook createWorkbook(final POIFSFileSystem fs) throws IOException {
        return new HSSFWorkbook(fs);
    }

    /**
     * Creates a HSSFWorkbook from the given DirectoryNode<p>
     * Note that in order to properly release resources the
     * Workbook should be closed after use.
     */
    public static HSSFWorkbook createWorkbook(final DirectoryNode root) throws IOException {
        return new HSSFWorkbook(root, true);
    }
}
