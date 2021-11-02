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
package org.apache.poi.xddf.usermodel.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.util.Units;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;

class TestXDDFTextBodyProperties {

    @Test
    void testProperties() throws IOException {
        XDDFTextBody text = new XDDFTextBody(null);
        text.initialize();
        XDDFBodyProperties body = text.getBodyProperties();
        CTTextBodyProperties props = body.getXmlObject();

        body.setBottomInset(null);
        assertFalse(props.isSetBIns());
        body.setBottomInset(3.6);
        assertTrue(props.isSetBIns());
        assertEquals(Units.toEMU(3.6), props.getBIns());

        body.setLeftInset(null);
        assertFalse(props.isSetLIns());
        body.setLeftInset(3.6);
        assertTrue(props.isSetLIns());
        assertEquals(Units.toEMU(3.6), props.getLIns());

        body.setRightInset(null);
        assertFalse(props.isSetRIns());
        body.setRightInset(3.6);
        assertTrue(props.isSetRIns());
        assertEquals(Units.toEMU(3.6), props.getRIns());

        body.setTopInset(null);
        assertFalse(props.isSetTIns());
        body.setTopInset(3.6);
        assertTrue(props.isSetTIns());
        assertEquals(Units.toEMU(3.6), props.getTIns());

        body.setAutoFit(null);
        assertFalse(props.isSetNoAutofit());
        assertFalse(props.isSetNormAutofit());
        assertFalse(props.isSetSpAutoFit());

        body.setAutoFit(new XDDFNoAutoFit());
        assertTrue(props.isSetNoAutofit());
        assertFalse(props.isSetNormAutofit());
        assertFalse(props.isSetSpAutoFit());

        body.setAutoFit(new XDDFNormalAutoFit());
        assertFalse(props.isSetNoAutofit());
        assertTrue(props.isSetNormAutofit());
        assertFalse(props.isSetSpAutoFit());

        body.setAutoFit(new XDDFShapeAutoFit());
        assertFalse(props.isSetNoAutofit());
        assertFalse(props.isSetNormAutofit());
        assertTrue(props.isSetSpAutoFit());

    }
}
