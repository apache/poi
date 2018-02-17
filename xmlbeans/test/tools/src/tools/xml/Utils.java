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
package tools.xml;

import org.apache.xmlbeans.XmlError;

import java.util.Collection;
import java.util.Iterator;

/**
 * Collection of Utility methods
 * e.g. printXMLErrors();
 */
public class Utils
{

    /**
     * Iterators over the Collection and prints out XmlErrors
     * @param errors Collection object containing XmlError objects
     */
    public static void printXMLErrors(Collection errors)
    {
        for (Iterator i = errors.iterator(); i.hasNext();)
        {
            Object obj = i.next();
            if (!(obj instanceof XmlError))
                continue;
            XmlError err = (XmlError) obj;
            String sev = (err.getSeverity() == XmlError.SEVERITY_WARNING ?
                            "WARNING" :
                            (err.getSeverity() == XmlError.SEVERITY_INFO ?
                            "INFO" :
                            "ERROR"));
            System.out.println(sev + " " + err.getLine() + ":" + err.getColumn()
                               + " " + err.getMessage() + " ");
        }
    }

}
