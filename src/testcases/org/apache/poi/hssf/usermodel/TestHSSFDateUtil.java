
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class TestHSSFDateUtil
 *
 *
 * @author
 * @author  Dan Sherman (dsherman at isisph.com)
 * @version %I%, %G%
 */

public class TestHSSFDateUtil
    extends TestCase
{
    public TestHSSFDateUtil(String s)
    {
        super(s);
    }

    /**
     * Checks the date conversion functions in the HSSFDateUtil class.
     */

    public void testDateConversion()
        throws Exception
    {

        // Iteratating over the hours exposes any rounding issues.
        for (int hour = 0; hour < 23; hour++)
        {
            GregorianCalendar date      = new GregorianCalendar(2002, 0, 1,
                                              hour, 1, 1);
            double            excelDate =
                HSSFDateUtil.getExcelDate(date.getTime());

            assertEquals("Checking hour = " + hour, date.getTime().getTime(),
                         HSSFDateUtil.getJavaDate(excelDate).getTime());
        }
        
        // check 1900 and 1904 date windowing conversions
        double excelDate = 36526.0;
                 // with 1900 windowing, excelDate is Jan. 1, 2000
                 // with 1904 windowing, excelDate is Jan. 2, 2004
        GregorianCalendar cal = new GregorianCalendar(2000,0,1); // Jan. 1, 2000
        Date dateIf1900 = cal.getTime();
        cal.add(GregorianCalendar.YEAR,4); // now Jan. 1, 2004
        cal.add(GregorianCalendar.DATE,1); // now Jan. 2, 2004
        Date dateIf1904 = cal.getTime();
        // 1900 windowing
        assertEquals("Checking 1900 Date Windowing",
                        dateIf1900.getTime(),
                           HSSFDateUtil.getJavaDate(excelDate,false).getTime());
        // 1904 windowing
        assertEquals("Checking 1904 Date Windowing",
                        dateIf1904.getTime(),
                           HSSFDateUtil.getJavaDate(excelDate,true).getTime());
    }
}
