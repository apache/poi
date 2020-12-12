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

package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXmlColumnPr;

/**
 *
 * This class is a wrapper around the CTXmlColumnPr (Open Office XML Part 4:
 * chapter 3.5.1.7)
 *
 *
 * @author Roberto Manicardi
 */
public class XSSFXmlColumnPr {

    private XSSFTable table;
    private XSSFTableColumn tableColumn;
    private CTXmlColumnPr ctXmlColumnPr;

    /**
     * Create a new XSSFXmlColumnPr (XML column properties) wrapper around a
     * CTXmlColumnPr.
     *
     * @param tableColumn
     *            table column for which the XML column properties are set
     * @param ctXmlColumnPr
     *            the XML column properties xmlbean to wrap
     */
    @Internal
    public XSSFXmlColumnPr(XSSFTableColumn tableColumn, CTXmlColumnPr ctXmlColumnPr) {
        this.table = tableColumn.getTable();
        this.tableColumn = tableColumn;
        this.ctXmlColumnPr = ctXmlColumnPr;
    }

    /**
     * Get the column for which these XML column properties are set.
     *
     * @return the table column
     * @since 4.0.0
     */
    public XSSFTableColumn getTableColumn() {
        return tableColumn;
    }

    public long getMapId() {
        return ctXmlColumnPr.getMapId();
    }

    public String getXPath() {
        return ctXmlColumnPr.getXpath();
    }

    /**
     * If the XPath is, for example, /Node1/Node2/Node3 and /Node1/Node2 is the common XPath for the table, the local XPath is /Node3
     *
     * @return the local XPath
     */
    public String getLocalXPath() {
        StringBuilder localXPath = new StringBuilder();
        int numberOfCommonXPathAxis = table.getCommonXpath().split("/").length-1;

        String[] xPathTokens = ctXmlColumnPr.getXpath().split("/");
        for (int i = numberOfCommonXPathAxis; i < xPathTokens.length; i++) {
            localXPath.append("/" + xPathTokens[i]);
        }
        return localXPath.toString();
    }

    public String getXmlDataType() {

        return ctXmlColumnPr.getXmlDataType();
    }

}
