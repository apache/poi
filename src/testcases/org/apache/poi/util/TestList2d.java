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
public class TestList2d
        extends TestCase
{
    public void testAccess()
            throws Exception
    {
        Object objectA = new Object();
        Object objectB = new Object();

        List2d array = new List2d();
        assertNull( array.get( 0, 0 ) );
        assertNull( array.get( 1, 1 ) );
        assertNull( array.get( 100, 100 ) );
        array.set( 100, 100, objectA );
        assertSame( objectA, array.get( 100, 100 ) );
        assertNull( array.get( 0, 0 ) );
        array.set( 0, 0, objectB );
        assertSame( objectB, array.get( 0, 0 ) );

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
