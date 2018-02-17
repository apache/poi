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

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelAttribute;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedAttributeGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.FormChoice;
import org.apache.xmlbeans.impl.xb.xsdschema.IncludeDocument;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import javax.xml.namespace.QName;

/**
 * This program takes a collection of .xsd files as input, finds all duplicate
 * name definitions, and factors out the first instance of each of those into
 * a common.xsd file, adding an appropriate <import> statement in the original
 * xsd file.
 */
public class FactorImports
{
    public static void printUsage()
    {
        System.out.println("Refactors a directory of XSD files to remove name conflicts.");
        System.out.println("Usage: sfactor [-import common.xsd] [-out outputdir] inputdir");
        System.out.println("    -import common.xsd - The XSD file to contain redundant ");
        System.out.println("                         definitions for importing.");
        System.out.println("    -out outputdir - The directory into which to place XSD ");
        System.out.println("                     files resulting from refactoring, ");
        System.out.println("                     plus a commonly imported common.xsd.");
        System.out.println("    inputdir - The directory containing the XSD files with");
        System.out.println("               redundant definitions.");
        System.out.println("    -license - Print license information.");
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

        CommandLine cl = new CommandLine(args, flags, Arrays.asList(new String[] {"import", "out"}));
        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null || args.length < 1)
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

        args = cl.args();
        if (args.length != 1)
        {
            System.exit(0);
            return;
        }

        String commonName = cl.getOpt("import");
        if (commonName == null)
            commonName = "common.xsd";

        String out = cl.getOpt("out");
        if (out == null)
        {
            System.out.println("Using output directory 'out'");
            out = "out";
        }
        File outdir = new File(out);
        File basedir = new File(args[0]);

        // first, parse all the schema files
        File[] files = cl.getFiles();
        Map schemaDocs = new HashMap();
        Set elementNames = new HashSet();
        Set attributeNames = new HashSet();
        Set typeNames = new HashSet();
        Set modelGroupNames = new HashSet();
        Set attrGroupNames = new HashSet();

        Set dupeElementNames = new HashSet();
        Set dupeAttributeNames = new HashSet();
        Set dupeTypeNames = new HashSet();
        Set dupeModelGroupNames = new HashSet();
        Set dupeAttrGroupNames = new HashSet();
        Set dupeNamespaces = new HashSet();

        for (int i = 0; i < files.length; i++)
        {
            try
            {
                // load schema
                SchemaDocument doc = SchemaDocument.Factory.parse(files[i]);
                schemaDocs.put(doc, files[i]);

                // warn about for imports, includes
                if (doc.getSchema().sizeOfImportArray() > 0 || doc.getSchema().sizeOfIncludeArray() > 0)
                    System.out.println("warning: " + files[i] + " contains imports or includes that are being ignored.");

                // collect together names
                String targetNamespace = doc.getSchema().getTargetNamespace();
                if (targetNamespace == null)
                    targetNamespace = "";

                TopLevelComplexType ct[] = doc.getSchema().getComplexTypeArray();
                for (int j = 0; j < ct.length; j++)
                    noteName(ct[j].getName(), targetNamespace, typeNames, dupeTypeNames, dupeNamespaces);

                TopLevelSimpleType st[] = doc.getSchema().getSimpleTypeArray();
                for (int j = 0; j < st.length; j++)
                    noteName(st[j].getName(), targetNamespace, typeNames, dupeTypeNames, dupeNamespaces);

                TopLevelElement el[] = doc.getSchema().getElementArray();
                for (int j = 0; j < el.length; j++)
                    noteName(el[j].getName(), targetNamespace, elementNames, dupeElementNames, dupeNamespaces);

                TopLevelAttribute at[] = doc.getSchema().getAttributeArray();
                for (int j = 0; j < at.length; j++)
                    noteName(at[j].getName(), targetNamespace, attributeNames, dupeAttributeNames, dupeNamespaces);

                NamedGroup gr[] = doc.getSchema().getGroupArray();
                for (int j = 0; j < gr.length; j++)
                    noteName(gr[j].getName(), targetNamespace, modelGroupNames, dupeModelGroupNames, dupeNamespaces);

                NamedAttributeGroup ag[] = doc.getSchema().getAttributeGroupArray();
                for (int j = 0; j < ag.length; j++)
                    noteName(ag[j].getName(), targetNamespace, attrGroupNames, dupeAttrGroupNames, dupeNamespaces);

            }
            catch (XmlException e)
            {
                System.out.println("warning: " + files[i] + " is not a schema file - " + e.getError().toString());
            }
            catch (IOException e)
            {
                System.err.println("Unable to load " + files[i] + " - " + e.getMessage());
                System.exit(1);
                return;
            }
        }

        if (schemaDocs.size() == 0)
        {
            System.out.println("No schema files found.");
            System.exit(0);
            return;
        }

        if (dupeTypeNames.size() + dupeElementNames.size() + dupeAttributeNames.size() +
                dupeModelGroupNames.size() + dupeAttrGroupNames.size() == 0)
        {
            System.out.println("No duplicate names found.");
            System.exit(0);
            return;
        }

        // create a schema doc for each namespace to be imported
        Map commonDocs = new HashMap();
        Map commonFiles = new HashMap();
        int count = dupeNamespaces.size() == 1 ? 0 : 1;
        for (Iterator i = dupeNamespaces.iterator(); i.hasNext(); )
        {
            String namespace = (String)i.next();
            SchemaDocument commonDoc = SchemaDocument.Factory.parse(
                    "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'/>"
            );
            if (namespace.length() > 0)
                commonDoc.getSchema().setTargetNamespace(namespace);
            commonDoc.getSchema().setElementFormDefault(FormChoice.QUALIFIED);
            commonDocs.put(namespace, commonDoc);
            commonFiles.put(commonDoc, commonFileFor(commonName, namespace, count++, outdir));
        }

        // pull out all the duplicate definitions and drop them into the file
        // we reuse the elementNames (etc) sets to keep track of which definitions
        // we have already inserted.
        for (Iterator i = schemaDocs.keySet().iterator(); i.hasNext(); )
        {
            SchemaDocument doc = (SchemaDocument)i.next();

            // collect together names
            String targetNamespace = doc.getSchema().getTargetNamespace();
            if (targetNamespace == null)
                targetNamespace = "";

            SchemaDocument commonDoc = (SchemaDocument)commonDocs.get(targetNamespace);

            boolean needImport = false;

            TopLevelComplexType ct[] = doc.getSchema().getComplexTypeArray();
            for (int j = ct.length - 1; j >= 0; j--)
            {
                if (!isDuplicate(ct[j].getName(), targetNamespace, dupeTypeNames))
                    continue;
                if (isFirstDuplicate(ct[j].getName(), targetNamespace, typeNames, dupeTypeNames))
                    commonDoc.getSchema().addNewComplexType().set(ct[j]);
                needImport = true;
                doc.getSchema().removeComplexType(j);
            }

            TopLevelSimpleType st[] = doc.getSchema().getSimpleTypeArray();
            for (int j = 0; j < st.length; j++)
            {
                if (!isDuplicate(st[j].getName(), targetNamespace, dupeTypeNames))
                    continue;
                if (isFirstDuplicate(st[j].getName(), targetNamespace, typeNames, dupeTypeNames))
                    commonDoc.getSchema().addNewSimpleType().set(st[j]);
                needImport = true;
                doc.getSchema().removeSimpleType(j);
            }

            TopLevelElement el[] = doc.getSchema().getElementArray();
            for (int j = 0; j < el.length; j++)
            {
                if (!isDuplicate(el[j].getName(), targetNamespace, dupeElementNames))
                    continue;
                if (isFirstDuplicate(el[j].getName(), targetNamespace, elementNames, dupeElementNames))
                    commonDoc.getSchema().addNewElement().set(el[j]);
                needImport = true;
                doc.getSchema().removeElement(j);
            }

            TopLevelAttribute at[] = doc.getSchema().getAttributeArray();
            for (int j = 0; j < at.length; j++)
            {
                if (!isDuplicate(at[j].getName(), targetNamespace, dupeAttributeNames))
                    continue;
                if (isFirstDuplicate(at[j].getName(), targetNamespace, attributeNames, dupeAttributeNames))
                    commonDoc.getSchema().addNewElement().set(at[j]);
                needImport = true;
                doc.getSchema().removeElement(j);
            }

            NamedGroup gr[] = doc.getSchema().getGroupArray();
            for (int j = 0; j < gr.length; j++)
            {
                if (!isDuplicate(gr[j].getName(), targetNamespace, dupeModelGroupNames))
                    continue;
                if (isFirstDuplicate(gr[j].getName(), targetNamespace, modelGroupNames, dupeModelGroupNames))
                    commonDoc.getSchema().addNewElement().set(gr[j]);
                needImport = true;
                doc.getSchema().removeElement(j);
            }

            NamedAttributeGroup ag[] = doc.getSchema().getAttributeGroupArray();
            for (int j = 0; j < ag.length; j++)
            {
                if (!isDuplicate(ag[j].getName(), targetNamespace, dupeAttrGroupNames))
                    continue;
                if (isFirstDuplicate(ag[j].getName(), targetNamespace, attrGroupNames, dupeAttrGroupNames))
                    commonDoc.getSchema().addNewElement().set(ag[j]);
                needImport = true;
                doc.getSchema().removeElement(j);
            }

            if (needImport)
            {
                IncludeDocument.Include newInclude = doc.getSchema().addNewInclude();
                File inputFile = (File)schemaDocs.get(doc);
                File outputFile = outputFileFor(inputFile, basedir, outdir);
                File commonFile = (File)commonFiles.get(commonDoc);
                if (targetNamespace != null)
                    newInclude.setSchemaLocation(relativeURIFor(outputFile, commonFile));
            }
        }

        // make the directory for output
        if (!outdir.isDirectory() && !outdir.mkdirs())
        {
            System.err.println("Unable to makedir " + outdir);
            System.exit(1);
            return;
        }

        // now write all those docs back out.
        for (Iterator i = schemaDocs.keySet().iterator(); i.hasNext(); )
        {
            SchemaDocument doc = (SchemaDocument)i.next();
            File inputFile = (File)schemaDocs.get(doc);
            File outputFile = outputFileFor(inputFile, basedir, outdir);
            if (outputFile == null)
                System.out.println("Cannot copy " + inputFile);
            else
                doc.save(outputFile, new XmlOptions().setSavePrettyPrint().setSaveAggresiveNamespaces());
        }

        for (Iterator i = commonFiles.keySet().iterator(); i.hasNext(); )
        {
            SchemaDocument doc = (SchemaDocument)i.next();
            File outputFile = (File)commonFiles.get(doc);
            doc.save(outputFile, new XmlOptions().setSavePrettyPrint().setSaveAggresiveNamespaces());
        }

    }

    private static File outputFileFor(File file, File baseDir, File outdir)
    {
        URI base = baseDir.getAbsoluteFile().toURI();
        URI abs = file.getAbsoluteFile().toURI();
        URI rel = base.relativize(abs);
        if (rel.isAbsolute())
        {
            System.out.println("Cannot relativize " + file);
            return null;
        }

        URI outbase = outdir.toURI();
        URI out = CodeGenUtil.resolve(outbase, rel);
        return new File(out);
    }

    private static URI commonAncestor(URI first, URI second)
    {
        String firstStr = first.toString();
        String secondStr = second.toString();
        int len = firstStr.length();
        if (secondStr.length() < len)
            len = secondStr.length();
        int i;
        for (i = 0; i < len; i++)
        {
            if (firstStr.charAt(i) != secondStr.charAt(i))
                break;
        }
        i -= 1;
        if (i >= 0)
            i = firstStr.lastIndexOf('/', i);
        if (i < 0)
            return null;
        try
        {
            return new URI(firstStr.substring(0, i));
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }


    private static String relativeURIFor(File source, File target)
    {
        URI base = source.getAbsoluteFile().toURI();
        URI abs = target.getAbsoluteFile().toURI();
        // find common substring...
        URI commonBase = commonAncestor(base, abs);
        if (commonBase == null)
            return abs.toString();

        URI baserel = commonBase.relativize(base);
        URI targetrel = commonBase.relativize(abs);
        if (baserel.isAbsolute() || targetrel.isAbsolute())
            return abs.toString();
        String prefix = "";
        String sourceRel = baserel.toString();
        for (int i = 0; i < sourceRel.length();)
        {
            i = sourceRel.indexOf('/', i);
            if (i < 0)
                break;
            prefix += "../";
            i += 1;
        }
        return prefix + targetrel.toString();
    }

    private static File commonFileFor(String commonName, String namespace, int i, File outdir)
    {
        String name = commonName;
        if (i > 0)
        {
            int index = commonName.lastIndexOf('.');
            if (index < 0)
                index = commonName.length();
            name = commonName.substring(0, index) + i + commonName.substring(index);
        }
        return new File(outdir, name);
    }


    private static void noteName(String name, String targetNamespace, Set seen, Set dupes, Set dupeNamespaces)
    {
        if (name == null)
            return;
        QName qName = new QName(targetNamespace, name);
        if (seen.contains(qName))
        {
            dupes.add(qName);
            dupeNamespaces.add(targetNamespace);
        }
        else
            seen.add(qName);

    }

    private static boolean isFirstDuplicate(String name, String targetNamespace, Set notseen, Set dupes)
    {
        if (name == null)
            return false;
        QName qName = new QName(targetNamespace, name);
        if (dupes.contains(qName) && notseen.contains(qName))
        {
            notseen.remove(qName);
            return true;
        }
        return false;
    }

    private static boolean isDuplicate(String name, String targetNamespace, Set dupes)
    {
        if (name == null)
            return false;
        QName qName = new QName(targetNamespace, name);
        return (dupes.contains(qName));
    }


}
