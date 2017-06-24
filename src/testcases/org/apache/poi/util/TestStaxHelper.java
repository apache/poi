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

package org.apache.poi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

/**
 * Unit test for StaxHelper
 */
public class TestStaxHelper {

    /**
     * test that newXMLInputFactory returns a factory with sensible defaults
     */
    @Test
    public void testNewXMLInputFactory() throws XMLStreamException {
        XMLInputFactory factory = StaxHelper.newXMLInputFactory();
        assertEquals(true, factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_VALIDATING));
        assertEquals(false, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(false, factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    /**
     * test that newXMLOutputFactory returns a factory with sensible defaults
     */
    @Test
    public void testNewXMLOutputFactory() {
        XMLOutputFactory factory = StaxHelper.newXMLOutputFactory();
        assertEquals(true, factory.getProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES));
    }

    /**
     * test that newXMLEventFactory returns a factory
     */
    @Test
    public void testNewXMLEventFactory() {
        assertNotNull(StaxHelper.newXMLEventFactory());
    }

}

