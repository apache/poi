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
package tools.util;

import org.apache.xmlbeans.*;

import java.util.ArrayList;
import java.util.Collection;
import java.io.File;
import java.io.IOException;


public class SchemaValidator
{

    /* Some tests
    public static void main(String args[])
    {
        //String schema = "cases/qatest/files/xbean/java2schema/daily/j2s/cases/inheritance/schema.xsd";
        String schema = "build/AntTests/META-INF/schemas/schema-0.xsd";
        Collection errors = new ArrayList();

        if (!isValidSchema(schema, errors))
        {
            System.out.println(schema + " is not valid:");
            Iterator i = errors.iterator();
            while (i.hasNext())
            {
                XmlError err = (XmlError) i.next();
                System.out.println(XmlError.severityAsString(err.getSeverity())
                                   + ": " + err.getMessage()
                                   + " at line " + err.getLine()
                                   + ", column " + err.getColumn());

            }
        }

    }
    */

    /**
     * Validates a schema file. Errors if any are returned in the collection
     * object that is passed in
     * @param schemaFile
     * @param errors
     * @return true if schema is valid, false otherwise.
     * @throws IllegalArgumentException
     */
    public static boolean isSchemaValid(String schemaFile, Collection errors)
        throws IllegalArgumentException
    {
        if (errors == null)
        {
            String msg = "Collection parameter cannot be null";
            throw new IllegalArgumentException(msg);
        }
        XmlOptions options = new XmlOptions();
        options.setErrorListener(errors);
        options.setLoadLineNumbers();

        Exception ex = null;
        Exception ioex = null;
        XmlObject[] schema = new XmlObject[1];
        SchemaTypeSystem system = null;
        try
        {
            schema[0] = XmlObject.Factory.parse(new File(schemaFile));
            system = XmlBeans.compileXsd(schema,
                                         XmlBeans.getBuiltinTypeSystem(),
                                         options);
        } catch (XmlException e)
        {
            // Parse Exception
            ex = e;
        } catch (IOException ioe)
        {
            // Error while trying to read file
            ioex = ioe;
        }

        if (ioex != null)
        {
            errors.add(XmlError.forMessage("EXCEPTION: " + ioex.toString(),
                                           XmlError.SEVERITY_ERROR));
            return false;
        }
        if (ex != null)
        {
            errors.add(XmlError.forMessage("EXCEPTION: " + ex.toString(),
                                           XmlError.SEVERITY_ERROR));
            return false;
        }

        return (system == null)? false : true;
    }

    /**
     * Validates the given schema file. Errors if any are not available
     *
     * @param schemaFile
     * @return true if schema is valid, false otherwise
     * @throws IllegalArgumentException
     */

    public static boolean isSchemaValid(String schemaFile)
        throws IllegalArgumentException
    {
        Collection errors = new ArrayList();
        return isSchemaValid(schemaFile, errors);
    }


}
