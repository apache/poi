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

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

/**
 * An object that handles instantiating concrete
 *  classes of the various instances one needs for
 *  HSSF and XSSF.
 * Works around a limitation in Java where we
 *  cannot have static methods on interfaces or abstract
 *  classes.
 * This allows you to get the appropriate class for
 *  a given interface, without you having to worry
 *  about if you're dealing with HSSF or XSSF.
 */
public interface CreationHelper {
    /**
     * Creates a new RichTextString instance
     * @param text The text to initialise the RichTextString with
     */
    RichTextString createRichTextString(String text);

    /**
     * Creates a new DataFormat instance
     */
    DataFormat createDataFormat();
    
    /**
     * Creates a new Hyperlink, of the given type
     */
    Hyperlink createHyperlink(HyperlinkType type);

    /**
     * Creates FormulaEvaluator - an object that evaluates formula cells.
     *
     * @return a FormulaEvaluator instance
     */
    FormulaEvaluator createFormulaEvaluator();
    
    /**
     * Creates a XSSF-style Color object, used for extended sheet
     *  formattings and conditional formattings
     */
    ExtendedColor createExtendedColor();

    /**
     * Creates a ClientAnchor. Use this object to position drawing object in a sheet
     *
     * @return a ClientAnchor instance
     * @see org.apache.poi.ss.usermodel.Drawing
     */
    ClientAnchor createClientAnchor();

    /**
     * Creates an AreaReference.
     *
     * @param reference cell reference
     * @return an AreaReference instance
     */
    AreaReference createAreaReference(String reference);

    /**
     * Creates an area ref from a pair of Cell References..
     *
     * @param topLeft cell reference
     * @param bottomRight cell reference
     * @return an AreaReference instance
     */
    AreaReference createAreaReference(CellReference topLeft, CellReference bottomRight);

}
