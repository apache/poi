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

import java.util.Locale;

public class JvmBugs {
    private static final POILogger LOG = POILogFactory.getLogger(JvmBugs.class);
    
    /**
     * The LineBreakMeasurer is used for calculating text bounds.
     * The last official JDK 6 version (1.6.0_45) and also JDK 7 (1.7.0_21)
     * for Windows are affected. For JDK 7 - update to a more recent version.
     * For JDK 6 - replace the fontmanager.dll with the previous release (1.6.0_43).
     * 
     * For performance reasons, this method only checks for a windows jvm
     * with version 1.6.0_45 and 1.7.0_21.
     * 
     * Set system property "org.apache.poi.JvmBugs.LineBreakMeasurer.ignore" to "true"
     * to bypass this check and use the normal fonts.
     * 
     * @return true, if jvm is bugged, caller code should use Lucida Sans
     * instead of Calibri and Lucida Bright instead of Cambria
     *
     * @see <a href="http://svn.apache.org/viewvc/poi/trunk/src/ooxml/testcases/org/apache/poi/xslf/usermodel/TestPPTX2PNG.java?view=markup">Workaround for XSLF</a>
     * @see <a href="http://svn.apache.org/viewvc/poi/trunk/src/testcases/org/apache/poi/ss/usermodel/BaseTestSheetAutosizeColumn.java?view=markup">Workaround for XSSF and HSSF</a>
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=54904">POI Bug #54904</a>
     * @see <a href="http://bugs.java.com/view_bug.do?bug_id=6501991">JDK Bug #6501991</a>
     * @see <a href="http://bugs.java.com/view_bug.do?bug_id=8013716">JDK Bug #8013716</a>
     * @see <a href="https://bitbucket.org/fakraemer/line-break-measurer-test">LineBreakMeasurerTest</a>
     */
    public static boolean hasLineBreakMeasurerBug() {
        String version = System.getProperty("java.version");
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        boolean ignore = Boolean.getBoolean("org.apache.poi.JvmBugs.LineBreakMeasurer.ignore");
        boolean hasBug = (!ignore && (os.contains("win") && ("1.6.0_45".equals(version) || "1.7.0_21".equals(version))));
        if (hasBug) {
            LOG.log(POILogger.WARN, "JVM has LineBreakMeasurer bug - see POI bug #54904 - caller code might default to Lucida Sans");
        }
        return hasBug;
    }
}
