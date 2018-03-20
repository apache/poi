/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.sprm;

import org.apache.poi.hwpf.usermodel.TableProperties;

import junit.framework.TestCase;

public class TableSprmUncompressorTest extends TestCase
{
    public void testSprmTDefTable()
    {
        final byte[] example = { (byte) 0x08, (byte) 0xD6, (byte) 0x2F,
                (byte) 0x00, (byte) 0x02, (byte) 0x94, (byte) 0xFF,
                (byte) 0x53, (byte) 0x03, (byte) 0x60, (byte) 0x13,
                (byte) 0x00, (byte) 0x06, (byte) 0xBF, (byte) 0x03,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x06, (byte) 0x0D, (byte) 0x10,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

        SprmOperation sprmOperation = new SprmOperation( example, 0 );
        assertEquals( SprmOperation.TYPE_TAP, sprmOperation.getType() );
        assertEquals( (short) 0x08, sprmOperation.getOperation() );

        TableProperties tableProperties = new TableProperties();
        TableSprmUncompressor.unCompressTAPOperation( tableProperties,
                sprmOperation );

        assertEquals( 2, tableProperties.getItcMac() );
        assertEquals( 3, tableProperties.getRgdxaCenter().length );
        assertEquals( (short) 0xff94, tableProperties.getRgdxaCenter()[0] );
        assertEquals( (short) 0x0353, tableProperties.getRgdxaCenter()[1] );
        assertEquals( (short) 0x1360, tableProperties.getRgdxaCenter()[2] );

        assertEquals( 2, tableProperties.getRgtc().length );
        assertEquals( (short) 0x03bf, tableProperties.getRgtc()[0].getWWidth() );
        assertEquals( (short) 0x100d, tableProperties.getRgtc()[1].getWWidth() );
    }
}