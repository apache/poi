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

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.impl.common.QNameHelper;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

public class TypeHierarchyPrinter
{
    public static void printUsage()
    {
        System.out.println("Prints the inheritance hierarchy of types defined in a schema.\n");
        System.out.println("Usage: xsdtree [-noanon] [-nopvr] [-noupa] [-partial] [-license] schemafile.xsd*");
        System.out.println("    -noanon - Don't include anonymous types in the tree.");
        System.out.println("    -noupa - do not enforce the unique particle attribution rule");
        System.out.println("    -nopvr - do not enforce the particle valid (restriction) rule");
        System.out.println("    -partial - Print only part of the hierarchy.");
        System.out.println("    -license - prints license information");
        System.out.println("    schemafile.xsd - File containing the schema for which to print a tree.");
        System.out.println();
    }

    public static void main(String[] args) throws Exception
    {
        Set flags = new HashSet();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("license");
        flags.add("version");
        flags.add("noanon");
        flags.add("noupr");
        flags.add("noupa");
        flags.add("partial");

        CommandLine cl = new CommandLine(args, flags, Collections.EMPTY_SET);
        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null)
        {
            printUsage();
            System.exit(0);
            return;
        }

        String[] badopts = cl.getBadOpts();
        if (badopts.length > 0)
        {
            for (int i = 0; i < badopts.length; i++)
                System.out.println("Unrecognized option: " + badopts[i]);
            printUsage();
            System.exit(0);
            return;
        }

        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }

        if (cl.getOpt("version") != null)
        {
            CommandLine.printVersion();
            System.exit(0);
            return;
        }

        if (cl.args().length == 0)
        {
            printUsage();
            return;
        }

        boolean noanon = (cl.getOpt("noanon") != null);
        boolean nopvr = (cl.getOpt("nopvr") != null);
        boolean noupa = (cl.getOpt("noupa") != null);
        boolean partial = (cl.getOpt("partial") != null);

        File[] schemaFiles = cl.filesEndingWith(".xsd");
        File[] jarFiles = cl.filesEndingWith(".jar");

        // step 1: load all the files
        List sdocs = new ArrayList();
        for (int i = 0; i < schemaFiles.length; i++)
        {
            try
            {
                sdocs.add(
                    SchemaDocument.Factory.parse(
                        schemaFiles[i], (new XmlOptions()).setLoadLineNumbers()));
            }
            catch (Exception e)
            {
                System.err.println( schemaFiles[i] + " not loadable: " + e );
            }
        }


        XmlObject[] schemas = (XmlObject[])sdocs.toArray(new XmlObject[0]);

        // step 2: compile all the schemas
        SchemaTypeLoader linkTo = null;
        SchemaTypeSystem typeSystem;
        Collection compErrors = new ArrayList();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        schemaOptions.setCompileDownloadUrls();
        if (nopvr)
            schemaOptions.setCompileNoPvrRule();
        if (noupa)
            schemaOptions.setCompileNoUpaRule();
        if (partial)
            schemaOptions.put("COMPILE_PARTIAL_TYPESYSTEM");

        if (jarFiles != null && jarFiles.length > 0)
            linkTo = XmlBeans.typeLoaderForResource(XmlBeans.resourceLoaderForPath(jarFiles));

        try
        {
            typeSystem = XmlBeans.compileXsd(schemas, linkTo, schemaOptions);
        }
        catch (XmlException e)
        {
            System.out.println("Schema invalid:" + (partial ? " couldn't recover from errors" : ""));
            if (compErrors.isEmpty())
                System.out.println(e.getMessage());
            else for (Iterator i = compErrors.iterator(); i.hasNext(); )
                System.out.println(i.next());
            return;
        }

        // step 2.5: recovered from errors, print out errors
        if (partial && !compErrors.isEmpty())
        {
            System.out.println("Schema invalid: partial schema type system recovered");
            for (Iterator i = compErrors.iterator(); i.hasNext(); )
                System.out.println(i.next());
        }

        // step 3: go through all the types, and note their base types and namespaces
        Map prefixes = new HashMap();
        prefixes.put("http://www.w3.org/XML/1998/namespace", "xml");
        prefixes.put("http://www.w3.org/2001/XMLSchema", "xs");
        System.out.println("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");

        // This will be a map of (base SchemaType -> Collection of directly dervied types)
        Map childTypes = new HashMap();

        // breadthfirst traversal of the type containment tree
        List allSeenTypes = new ArrayList();
        allSeenTypes.addAll(Arrays.asList(typeSystem.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(typeSystem.attributeTypes()));
        allSeenTypes.addAll(Arrays.asList(typeSystem.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType sType = (SchemaType)allSeenTypes.get(i);

            // recurse through nested anonymous types as well
            if (!noanon)
                allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));

            // we're not interested in document types, attribute types, or chasing the base type of anyType
            if (sType.isDocumentType() || sType.isAttributeType() || sType == XmlObject.type)
                continue;

            // assign a prefix to the namespace of this type if needed
            noteNamespace(prefixes, sType);

            // enter this type in the list of children of its base type
            Collection children = (Collection)childTypes.get(sType.getBaseType());
            if (children == null)
            {
                children = new ArrayList();
                childTypes.put(sType.getBaseType(), children);

                // the first time a builtin type is seen, add it too (to get a complete tree up to anyType)
                if (sType.getBaseType().isBuiltinType())
                    allSeenTypes.add(sType.getBaseType());
            }
            children.add(sType);
        }

        // step 4: print the tree, starting from xs:anyType (i.e., XmlObject.type)
        List typesToPrint = new ArrayList();
        typesToPrint.add(XmlObject.type);
        StringBuffer spaces = new StringBuffer();
        while (!typesToPrint.isEmpty())
        {
            SchemaType sType = (SchemaType)typesToPrint.remove(typesToPrint.size() - 1);
            if (sType == null)
                spaces.setLength(Math.max(0, spaces.length() - 2));
            else
            {
                System.out.println(spaces + "+-" + QNameHelper.readable(sType, prefixes) + notes(sType));
                Collection children = (Collection)childTypes.get(sType);
                if (children != null && children.size() > 0)
                {
                    spaces.append(typesToPrint.size() == 0 || typesToPrint.get(typesToPrint.size() - 1) == null ? "  " : "| ");
                    typesToPrint.add(null);
                    typesToPrint.addAll(children);
                }
            }
        }
    }

    private static String notes(SchemaType sType)
    {
        if (sType.isBuiltinType())
            return " (builtin)";

        if (sType.isSimpleType())
        {
            switch (sType.getSimpleVariety())
            {
                case SchemaType.LIST:
                    return " (list)";
                case SchemaType.UNION:
                    return " (union)";
                default:
                    if (sType.getEnumerationValues() != null)
                        return " (enumeration)";
                    return "";
            }
        }

        switch (sType.getContentType())
        {
            case SchemaType.MIXED_CONTENT:
                return " (mixed)";
            case SchemaType.SIMPLE_CONTENT:
                return " (complex)";
            default:
                return "";
        }
    }

    private static void noteNamespace(Map prefixes, SchemaType sType)
    {
        String namespace = QNameHelper.namespace(sType);
        if (namespace.equals("") || prefixes.containsKey(namespace))
            return;

        String base = QNameHelper.suggestPrefix(namespace);
        String result = base;
        for (int n = 0; prefixes.containsValue(result); n += 1)
        {
            result = base + n;
        }

        prefixes.put(namespace, result);
        System.out.println("xmlns:" + result + "=\"" + namespace + "\"");
    }
}
