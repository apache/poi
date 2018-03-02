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
package org.apache.xmlbeans.samples.abstracttypes;

import abstractFigures.*;
import figures.*;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * Test class that builds a document using type substitution
 */
public class AbstractTypes
{
    public static void main(String[] args)
    {
        buildDocument(true);
    }

    public static XmlObject buildDocument(boolean enableOutput)
    {
        XmlOptions opt = (new XmlOptions()).setSavePrettyPrint();

        // Build a new document
        RootDocument doc = RootDocument.Factory.newInstance();
        RootDocument.Root figures = doc.addNewRoot();
        if (enableOutput)
            System.out.println("Empty document:\n" + doc.xmlText(opt) + "\n");

        // Add abstract figures
        Shape s1 = figures.addNewFigure();
        s1.setId("001");
        Shape s2 = figures.addNewFigure();
        s2.setId("002");
        // Document contains two shapes now
        // Because the shape is abstract, the document will not yet be valid
        if (enableOutput)
        {
            System.out.println("Document containing the abstract types:\n" + doc.xmlText(opt));
            System.out.println("Valid = " + doc.validate() + "\n");
        }

        // Change the abstract figures to concrete ones
        Circle circle = (Circle) s1.changeType(Circle.type);
        circle.setRadius(10.0);
        Square square = (Square) s2.changeType(Square.type);
        square.setSide(20.0);
        // Document contains two concrete shapes and is valid
        if (enableOutput)
        {
            System.out.println("Final document:\n" + doc.xmlText(opt));
            System.out.println("Vald = " + doc.validate());
        }

        return doc;
    }
}
