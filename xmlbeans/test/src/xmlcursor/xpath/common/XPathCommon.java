/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package xmlcursor.xpath.common;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import junit.framework.Assert;
import tools.xml.XmlComparator;

/**
 */
public class XPathCommon {

    public static String display(XmlObject[] rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < rObj.length; i++) {
            sb.append("[" + i + "] -- " + rObj[i].xmlText(xm)+"\n");
        }
        return sb.toString();
    }


    public static String getPrint(XmlObject[] rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();

        StringBuffer st = new StringBuffer();
        for (int i = 0; i < rObj.length; i++) {
            st.append("[" + i + "] -- " + rObj[i].xmlText(xm));
        }
        return st.toString();
    }

    public static String getPrint(XmlCursor rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();

        StringBuffer st = new StringBuffer();
        int i = 0;
        while (rObj.toNextSelection()) {
            st.append("[cursor-" + i + "] -- " + rObj.xmlText(xm));
            i++;
        }

        return st.toString();
    }

    public static String display(XmlCursor rObj) {
        XmlOptions xm = new XmlOptions();
        xm.setSavePrettyPrint();
        xm.setLoadStripWhitespace();
        StringBuffer sb=new StringBuffer();
        int i = 0;
        while (rObj.toNextSelection()) {
            sb.append("[cursor-" + i + "] -- " + rObj.xmlText(xm)+"\n");
            i++;
        }
        return sb.toString();
    }

    private static void check(XmlCursor actual, XmlCursor expected) {
        try {
            XmlComparator.Diagnostic diag = new XmlComparator.Diagnostic();
            boolean match = XmlComparator.lenientlyCompareTwoXmlStrings(
                actual.xmlText(),
                expected.xmlText(), diag);

            Assert.assertTrue("***********************\nFound difference: \nactual=\n'" +
                actual.xmlText() +
                              "'\nexpected=\n'" + expected.xmlText()
                + "'\ndiagnostic=" + diag, match);
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }
    }

    public static void compare(XmlObject rObj, XmlObject rSet) throws Exception{
        check(rObj.newCursor(),rSet.newCursor());
    }
    public static void compare(XmlObject[] rObj, XmlObject[] rSet) throws Exception {

        if (rObj.length != rSet.length)
            throw new Exception("Comparison Failed\n " +
                    "Actual Count: "+rObj.length +" Expected Count: "+rSet.length+"\n" +
                    "Actual:"+getPrint(rObj)+"\nExpected:"+getPrint(rSet));

        for (int i = 0; i < rObj.length; i++){
            check(rObj[i].newCursor(), rSet[i].newCursor());
        }
        // This should be done in the test if no exception occurs...don't print here
        // System.out.println("Test Passed");
    }

    public static void compare(XmlCursor rObj, XmlObject[] rSet) throws Exception {
        if (rObj.getSelectionCount() != rSet.length) {
            StringBuffer message = new StringBuffer();

            message.append("EXPECTED ==\n");
            display(rSet);
            message.append("ACTUAL ==\n");
            display(rObj);

            throw new Exception(message.toString()+
                "\nCompare failure == Result Count was not equal to actual count\n" +
                    "Actual Count: "+rObj.getSelectionCount() +" Expected Count: "+rSet.length+"\n" +
                    "Actual:" + getPrint(rObj) + "\nExpected:" + getPrint(rSet));
        }
        int i = 0;
        while (rObj.toNextSelection()) {
            //System.out.println("[cursor-" + i + "] -- " + rObj.xmlText(xm));
            //System.out.println("[Expected-" + i + "] -- " + rSet[i].xmlText(xm));

            check(rObj, rSet[i].newCursor());
            i++;
        }
        // This should be done in the test if no exception occurs...don't print here
       // System.out.println("Test Passed");
    }

    public static void checkLength(XmlCursor rObj, int count) throws Exception{
        if(rObj.getSelectionCount() != count){
            throw new Exception("Length == Return Count was not equal\n"+
                    "Cursor-Count: "+ rObj.getSelectionCount()+" Expected: "+count+"\n"+
                    getPrint(rObj));
        }
    }

}
