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

package org.apache.poi.xwpf.usermodel;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;

/**
 * Raw picture data, normally attached to a WordprocessingML Drawing.
 * As a rule, embedded xlsx are stored in the /word/embedded/ part of a WordprocessingML package.
 */
@Beta
public class XWPFChartData extends POIXMLDocumentPart {
    
    /**
     * Create a new XWPFChartData node
     * @since POI 4.0.0
     */
    protected XWPFChartData() {
        super();
    }
    
    /**
     * Construct XWPFChartData from a package part
     *
     * @param part the package part holding the drawing data,
     * 
     * @since POI 4.0.0
     */
    public XWPFChartData(PackagePart part) {
        super(part);
    }
    
    /**
     * Gets the WorkBook data as a byte array.
     * <p>
     * Note, that this call might be expensive since excel data is copied into a temporary byte array.
     * You can grab the chart data directly from the underlying package part as follows:
     * <br>
     * <code>
     * InputStream is = getPackagePart().getInputStream();
     * </code>
     * </p>
     *
     * @return the embedded excel data.
     * @since POI 4.0.0
     */
    public InputStream getData() {
        try {
            return getPackagePart().getInputStream();
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
    }
    
    /**
     * chartData objects store the actual content in the part directly without keeping a
     * copy like all others therefore we need to handle them differently.
     */
    @Override
    protected void prepareForCommit() {
        // do not clear the part here
    }
}
