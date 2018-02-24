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
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument;
import org.apache.xmlbeans.impl.xb.substwsdl.TImport;

import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;
import java.io.File;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.IncludeDocument;

public class SchemaCopy
{
    public static void printUsage()
    {
        System.out.println("Copies the XML schema at the specified URL to the specified file.");
        System.out.println("Usage: scopy sourceurl [targetfile]");
        System.out.println("    sourceurl - The URL at which the schema is located.");
        System.out.println("    targetfile - The file to which the schema should be copied.");
        System.out.println();

    }

    public static void main(String[] args)
    {
        if (args.length < 1 || args.length > 2)
        {
            printUsage();
            return;
        }

        URI source = null;
        URI target = null;

        try
        {
            if(args[0].compareToIgnoreCase("-usage") == 0){
                printUsage();
                return;
            }

            source = new URI(args[0]);
            source.toURL(); // to trigger exception
        }
        catch (Exception e)
        {
            System.err.println("Badly formed URL " + source);
            return;
        }

        if (args.length < 2)
        {
            try
            {
                URI dir = new File(".").getCanonicalFile().toURI();
                String lastPart = source.getPath();
                lastPart = lastPart.substring(lastPart.lastIndexOf('/') + 1);
                target = CodeGenUtil.resolve(dir, URI.create(lastPart));
            }
            catch (Exception e)
            {
                System.err.println("Cannot canonicalize current directory");
                return;
            }
        }
        else
        {
            try
            {
                target = new URI(args[1]);
                if (!target.isAbsolute())
                    target = null;
                else if (!target.getScheme().equals("file"))
                    target = null;
            }
            catch (Exception e)
            {
                target = null;
            }

            if (target == null)
            {
                try
                {
                    target = new File(target).getCanonicalFile().toURI();
                }
                catch (Exception e)
                {
                    System.err.println("Cannot canonicalize current directory");
                    return;
                }
            }
        }

        Map thingsToCopy = findAllRelative(source, target);
        copyAll(thingsToCopy, true);
    }

    private static void copyAll(Map uriMap, boolean stdout)
    {
        for (Iterator i = uriMap.keySet().iterator(); i.hasNext(); )
        {
            URI source = (URI)i.next();
            URI target = (URI)uriMap.get(source);
            try
            {
                IOUtil.copyCompletely(source, target);
            }
            catch (Exception e)
            {
                if (stdout)
                    System.out.println("Could not copy " + source + " -> " + target);
                continue;
            }
            if (stdout)
            System.out.println("Copied " + source + " -> " + target);
        }
    }


    /**
     * Copies the schema or wsdl at the source URI to the target URI, along
     * with any relative references.  The target URI should be a file URI.
     * If doCopy is false, the file copies are not actually done; the map
     * returned just describes the copies that would have been done.
     *
     * @param source an arbitrary URI describing a source Schema or WSDL
     * @param target a file URI describing a target filename
     * @return a map of all the source/target URIs needed to copy
     * the file along with all its relative referents.
     */
    public static Map findAllRelative(URI source, URI target)
    {
        Map result = new LinkedHashMap();
        result.put(source, target);

        LinkedList process = new LinkedList();
        process.add(source);

        while (!process.isEmpty())
        {
            URI nextSource = (URI)process.removeFirst();
            URI nextTarget = (URI)result.get(nextSource);
            Map nextResults = findRelativeInOne(nextSource, nextTarget);
            for (Iterator i = nextResults.keySet().iterator(); i.hasNext(); )
            {
                URI newSource = (URI)i.next();
                if (result.containsKey(newSource))
                    continue;
                result.put(newSource, nextResults.get(newSource));
                process.add(newSource);
            }
        }

        return result;
    }

    private static final XmlOptions loadOptions = new XmlOptions().
            setLoadSubstituteNamespaces(Collections.singletonMap(
                    "http://schemas.xmlsoap.org/wsdl/", "http://www.apache.org/internal/xmlbeans/wsdlsubst"
            ));

    private static Map findRelativeInOne(URI source, URI target)
    {
        try
        {
            URL sourceURL = source.toURL();
            XmlObject xobj = XmlObject.Factory.parse(sourceURL, loadOptions);
            XmlCursor xcur = xobj.newCursor();
            xcur.toFirstChild();

            Map result = new LinkedHashMap();

            if (xobj instanceof SchemaDocument)
                putMappingsFromSchema(result, source, target, ((SchemaDocument)xobj).getSchema());
            else if (xobj instanceof DefinitionsDocument)
                putMappingsFromWsdl(result, source, target, ((DefinitionsDocument)xobj).getDefinitions());
            return result;
        }
        catch (Exception e)
        {
            // any exceptions parsing the given URL?  Then skip this file silently
        }
        return Collections.EMPTY_MAP;
    }

    private static void putNewMapping(Map result, URI origSource, URI origTarget, String literalURI)
    {
        try
        {
            if (literalURI == null)
                return;
            URI newRelative = new URI(literalURI);
            if (newRelative.isAbsolute())
                return;
            URI newSource = CodeGenUtil.resolve(origSource, newRelative);
            URI newTarget = CodeGenUtil.resolve(origTarget, newRelative);
            result.put(newSource, newTarget);
        }
        catch (URISyntaxException e)
        {
            // uri syntax problem? do nothing silently.
        }
    }

    private static void putMappingsFromSchema(Map result, URI source, URI target, SchemaDocument.Schema schema)
    {
        ImportDocument.Import[] imports = schema.getImportArray();
        for (int i = 0; i < imports.length; i++)
            putNewMapping(result, source, target, imports[i].getSchemaLocation());

        IncludeDocument.Include[] includes = schema.getIncludeArray();
        for (int i = 0; i < includes.length; i++)
            putNewMapping(result, source, target, includes[i].getSchemaLocation());
    }

    private static void putMappingsFromWsdl(Map result, URI source, URI target, DefinitionsDocument.Definitions wdoc)
    {
        XmlObject[] types = wdoc.getTypesArray();
        for (int i = 0; i < types.length; i++)
        {
            SchemaDocument.Schema[] schemas = (SchemaDocument.Schema[])types[i].selectPath("declare namespace xs='http://www.w3.org/2001/XMLSchema' xs:schema");
            for (int j = 0; j < schemas.length; j++)
                putMappingsFromSchema(result, source, target, schemas[j]);
        }

        TImport[] imports = wdoc.getImportArray();
        for (int i = 0; i < imports.length; i++)
            putNewMapping(result, source, target, imports[i].getLocation());
    }
}
