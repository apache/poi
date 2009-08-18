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

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.ss.usermodel.BaseTestFont;
import org.apache.poi.ss.usermodel.Font;

/**
 * Tests various functionality having to do with {@link org.apache.poi.ss.usermodel.Name}.
 *
 * @author Yegor Kozlov
 */
public final class TestHSSFFont extends BaseTestFont {

    @Override
    protected HSSFITestDataProvider getTestDataProvider(){
        return HSSFITestDataProvider.getInstance();
    }

    public void testDefaultFont() {
        baseTestDefaultFont(HSSFFont.FONT_ARIAL, (short)200, Font.COLOR_NORMAL);
    }
}
