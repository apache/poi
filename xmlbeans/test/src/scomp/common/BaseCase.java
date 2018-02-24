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
package scomp.common;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlErrorCodes;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 *
 *
 */
public class BaseCase extends TestCase {
    protected boolean bVerbose = true;

    protected XmlOptions validateOptions;
    protected ArrayList errorList;

    public void setUp() {
        validateOptions = new XmlOptions();
        errorList = new ArrayList();
        validateOptions.setErrorListener(errorList);
    }

    protected void clearErrors() {
        //reset error list for next time
        errorList = new ArrayList();
        validateOptions.setErrorListener(errorList);
    }

    public void showErrors() {
        if (bVerbose)
            for (int i = 0; i < errorList.size(); i++) {
                XmlError error = (XmlError) errorList.get(i);
                System.out.println("\n");
                System.out.println("Message: " + error.getMessage() + "\n");
                if (error.getCursorLocation() != null)
                    System.out.println("Location of invalid XML: " +
                            error.getCursorLocation().xmlText() + "\n");
            }

    }

    //TODO: compare regardless of order
    public boolean compareErrorCodes(String[] expected) {
        if ( errorList.size() != expected.length){
            System.err.println(stringOfCodes(expected,errorList));
                           
           return false;
        }
        StringBuffer errMessage=
                new StringBuffer();
        for (int i = 0; i < errorList.size(); i++) {
            XmlError error = (XmlError) errorList.get(i);
            if ( error.getErrorCode() == null )
              errMessage.append("Kevin needs a code here "+error.getMessage()+"\n");

            if (!expected[i].equals(error.getErrorCode())){
                if (errMessage.length() >0)
                    System.err.println(errMessage);
                ArrayList exp=new ArrayList();

                System.err.println(stringOfCodes(expected,errorList));
                return false;
            }
        }
        return true;
    }

    public String stringOfCodes(String[] errorCodes, ArrayList actual_errorCodes)
    {
        StringBuffer res=new StringBuffer();
        res.append("\n Expected codes:\n");
        int i=0;
        for (;i < errorCodes.length; i++ )
            res.append( errorCodes[i]+"\n" );
        res.append("\nBut Got: \n");
        for ( i=0 ;i < actual_errorCodes.size(); i++ )
            res.append( actual_errorCodes.get(i)+"\n" );

        return res.toString();
    }

}
