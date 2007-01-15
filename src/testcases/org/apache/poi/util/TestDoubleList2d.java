/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.util;

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class TestDoubleList2d
        extends TestCase
{
    public void testAccess()
            throws Exception
    {
        DoubleList2d array = new DoubleList2d();
        assertEquals( 0, array.get( 0, 0 ), 0.00001 );
        assertEquals( 0, array.get( 1, 1 ), 0.00001 );
        assertEquals( 0, array.get( 100, 100 ), 0.00001 );
        array.set( 100, 100, 999 );
        assertEquals( 999, array.get( 100, 100 ), 0.00001 );
        assertEquals( 0, array.get( 0, 0 ), 0.00001 );
        array.set( 0, 0, 999 );
        assertEquals( 999, array.get( 0, 0 ), 0.00001 );

        try
        {
            array.get( -1, -1 );
            fail();
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {
            // pass
        }
    }
}
