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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.BindingConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.net.URI;

import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;

import java.util.Collection;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class SchemaTypeSystemCompiler
{
    public static class Parameters
    {
        private SchemaTypeSystem existingSystem;
        private String name;
        private Schema[] schemas;
        private BindingConfig config;
        private SchemaTypeLoader linkTo;
        private XmlOptions options;
        private Collection errorListener;
        private boolean javaize;
        private URI baseURI;
        private Map sourcesToCopyMap;
        private File schemasDir;

        public SchemaTypeSystem getExistingTypeSystem()
        {
            return existingSystem;
        }

        public void setExistingTypeSystem(SchemaTypeSystem system)
        {
            this.existingSystem = system;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public SchemaDocument.Schema[] getSchemas()
        {
            return schemas;
        }

        public void setSchemas(SchemaDocument.Schema[] schemas)
        {
            this.schemas = schemas;
        }

        public BindingConfig getConfig()
        {
            return config;
        }

        public void setConfig(BindingConfig config)
        {
            this.config = config;
        }

        public SchemaTypeLoader getLinkTo()
        {
            return linkTo;
        }

        public void setLinkTo(SchemaTypeLoader linkTo)
        {
            this.linkTo = linkTo;
        }

        public XmlOptions getOptions()
        {
            return options;
        }

        public void setOptions(XmlOptions options)
        {
            this.options = options;
        }

        public Collection getErrorListener()
        {
            return errorListener;
        }

        public void setErrorListener(Collection errorListener)
        {
            this.errorListener = errorListener;
        }

        public boolean isJavaize()
        {
            return javaize;
        }

        public void setJavaize(boolean javaize)
        {
            this.javaize = javaize;
        }

        public URI getBaseURI()
        {
            return baseURI;
        }

        public void setBaseURI(URI baseURI)
        {
            this.baseURI = baseURI;
        }

        public Map getSourcesToCopyMap()
        {
            return sourcesToCopyMap;
        }

        public void setSourcesToCopyMap(Map sourcesToCopyMap)
        {
            this.sourcesToCopyMap = sourcesToCopyMap;
        }

        public File getSchemasDir()
        {
            return schemasDir;
        }

        public void setSchemasDir(File schemasDir)
        {
            this.schemasDir = schemasDir;
        }

    }

    /**
     * Compiles a SchemaTypeSystem.  Use XmlBeans.compileXmlBeans() if you can.
     */
    public static SchemaTypeSystem compile(Parameters params)
    {
        return compileImpl(params.getExistingTypeSystem(), params.getName(),
            params.getSchemas(), params.getConfig(), params.getLinkTo(),
            params.getOptions(), params.getErrorListener(), params.isJavaize(),
            params.getBaseURI(), params.getSourcesToCopyMap(), params.getSchemasDir());
    }

    /**
     * Please do not invoke this method directly as the signature could change unexpectedly.
     * Use one of
     * {@link XmlBeans#loadXsd(XmlObject[])},
     * {@link XmlBeans#compileXsd(XmlObject[], SchemaTypeLoader, XmlOptions)},
     * or
     * {@link XmlBeans#compileXmlBeans(String, SchemaTypeSystem, XmlObject[], BindingConfig, SchemaTypeLoader, Filer, XmlOptions)}
     */
    public static SchemaTypeSystemImpl compile(String name, SchemaTypeSystem existingSTS,
        XmlObject[] input, BindingConfig config, SchemaTypeLoader linkTo, Filer filer, XmlOptions options)
        throws XmlException
    {
        options = XmlOptions.maskNull(options);
        ArrayList schemas = new ArrayList();

        if (input != null)
        {
            for (int i = 0; i < input.length; i++)
            {
                if (input[i] instanceof Schema)
                    schemas.add(input[i]);
                else if (input[i] instanceof SchemaDocument && ((SchemaDocument)input[i]).getSchema() != null)
                    schemas.add(((SchemaDocument)input[i]).getSchema());
                else
                    throw new XmlException("Thread " + Thread.currentThread().getName() +  ": The " + i + "th supplied input is not a schema document: its type is " + input[i].schemaType());
            }
        }

        Collection userErrors = (Collection)options.get(XmlOptions.ERROR_LISTENER);
        XmlErrorWatcher errorWatcher = new XmlErrorWatcher(userErrors);

        SchemaTypeSystemImpl stsi = compileImpl(existingSTS, name,
            (Schema[])schemas.toArray(new Schema[schemas.size()]),
            config, linkTo, options, errorWatcher, filer!=null, (URI) options.get(XmlOptions.BASE_URI),
            null, null);

        // if there is an error and compile didn't recover (stsi==null), throw exception
        if (errorWatcher.hasError() && stsi == null)
        {
            throw new XmlException(errorWatcher.firstError());
        }

        if (stsi != null && !stsi.isIncomplete() && filer != null)
        {
            stsi.save(filer);
            generateTypes(stsi, filer, options);
        }

        return stsi;
    }

    //
    // Compiles a SchemaTypeSystem
    //
    /* package */ static SchemaTypeSystemImpl compileImpl( SchemaTypeSystem system, String name,
        Schema[] schemas, BindingConfig config, SchemaTypeLoader linkTo,
        XmlOptions options, Collection outsideErrors, boolean javaize,
        URI baseURI, Map sourcesToCopyMap, File schemasDir)
    {
        if (linkTo == null)
            throw new IllegalArgumentException("Must supply linkTo");

        XmlErrorWatcher errorWatcher = new XmlErrorWatcher(outsideErrors);
        boolean incremental = system != null;

        // construct the state
        StscState state = StscState.start();
        boolean validate = (options == null || !options.hasOption(XmlOptions.COMPILE_NO_VALIDATION));
        try
        {
            state.setErrorListener(errorWatcher);
            state.setBindingConfig(config);
            state.setOptions(options);
            state.setGivenTypeSystemName(name);
            state.setSchemasDir(schemasDir);
            if (baseURI != null)
                state.setBaseUri(baseURI);

            // construct the classpath (you always get the builtin types)
            linkTo = SchemaTypeLoaderImpl.build(new SchemaTypeLoader[] { BuiltinSchemaTypeSystem.get(), linkTo }, null, null);
            state.setImportingTypeLoader(linkTo);

            List validSchemas = new ArrayList(schemas.length);

            // load all the xsd files into it
            if (validate)
            {
                XmlOptions validateOptions = new XmlOptions().setErrorListener(errorWatcher);
                if (options.hasOption(XmlOptions.VALIDATE_TREAT_LAX_AS_SKIP))
                    validateOptions.setValidateTreatLaxAsSkip();
                for (int i = 0; i < schemas.length; i++)
                {
                    if (schemas[i].validate(validateOptions))
                        validSchemas.add(schemas[i]);
                }
            }
            else
            {
                validSchemas.addAll(Arrays.asList(schemas));
            }

            Schema[] startWith = (Schema[])validSchemas.toArray(new Schema[validSchemas.size()]);

            if (incremental)
            {
                Set namespaces = new HashSet();
                startWith = getSchemasToRecompile((SchemaTypeSystemImpl)system, startWith, namespaces);
                state.initFromTypeSystem((SchemaTypeSystemImpl)system, namespaces);
            }
            else
            {
                state.setDependencies(new SchemaDependencies());
            }

            // deal with imports and includes
            StscImporter.SchemaToProcess[] schemasAndChameleons = StscImporter.resolveImportsAndIncludes(startWith, incremental);

            // call the translator so that it may also perform magic
            StscTranslator.addAllDefinitions(schemasAndChameleons);

            // call the resolver to do its magic
            StscResolver.resolveAll();
            
            // call the checker to check both restrictions and defaults
            StscChecker.checkAll();

            // call the javaizer to do its magic
            StscJavaizer.javaizeAllTypes(javaize);

            // construct the loader out of the state
            state.get().sts().loadFromStscState(state);

            // fill in the source-copy map
            if (sourcesToCopyMap != null)
                sourcesToCopyMap.putAll(state.sourceCopyMap());

            if (errorWatcher.hasError())
            {
                // EXPERIMENTAL: recovery from compilation errors and partial type system
                if (state.allowPartial() && state.getRecovered() == errorWatcher.size())
                {
                    // if partial type system allowed and all errors were recovered
                    state.get().sts().setIncomplete(true);
                }
                else
                {
                    // if any non-recoverable errors, return null
                    return null;
                }
            }

            if (system != null)
                ((SchemaTypeSystemImpl) system).setIncomplete(true);

            return state.get().sts();
        }
        finally
        {
            StscState.end();
        }
    }

    /**
     * Get the list of Schemas to be recompiled, based on the list of Schemas that
     * were modified.
     * We make use of the depencency information that we stored in the typesystem
     * and of the entity resolvers that have been set up
     */
    private static Schema[] getSchemasToRecompile(SchemaTypeSystemImpl system,
        Schema[] modified, Set namespaces)
    {
        Set modifiedFiles = new HashSet();
        Map haveFile = new HashMap();
        List result = new ArrayList();
        for (int i = 0; i < modified.length; i++)
        {
            String fileURL = modified[i].documentProperties().getSourceName();
            if (fileURL == null)
                throw new IllegalArgumentException("One of the Schema files passed in" +
                    " doesn't have the source set, which prevents it to be incrementally" +
                    " compiled");
            modifiedFiles.add(fileURL);
            haveFile.put(fileURL, modified[i]);
            result.add(modified[i]);
        }
        SchemaDependencies dep = system.getDependencies();
        List nss = dep.getNamespacesTouched(modifiedFiles);
        namespaces.addAll(dep.computeTransitiveClosure(nss));
        List needRecompilation = dep.getFilesTouched(namespaces);
        StscState.get().setDependencies(new SchemaDependencies(dep, namespaces));
        for (int i = 0; i < needRecompilation.size(); i++)
        {
            String url = (String) needRecompilation.get(i);
            Schema have = (Schema) haveFile.get(url);
            if (have == null)
            {
                // We have to load the file from the entity resolver
                try
                {
                    XmlObject xdoc = StscImporter.DownloadTable.
                        downloadDocument(StscState.get().getS4SLoader(), null, url);
                    XmlOptions voptions = new XmlOptions();
                    voptions.setErrorListener(StscState.get().getErrorListener());
                    if (!(xdoc instanceof SchemaDocument) || !xdoc.validate(voptions))
                    {
                        StscState.get().error("Referenced document is not a valid schema, URL = " + url, XmlErrorCodes.CANNOT_FIND_RESOURCE, null);
                        continue;
                    }

                    SchemaDocument sDoc = (SchemaDocument)xdoc;

                    result.add(sDoc.getSchema());
                }
                catch (java.net.MalformedURLException mfe)
                {
                    StscState.get().error(XmlErrorCodes.EXCEPTION_LOADING_URL, new Object[] { "MalformedURLException", url, mfe.getMessage() }, null);
                    continue;
                }
                catch (java.io.IOException ioe)
                {
                    StscState.get().error(XmlErrorCodes.EXCEPTION_LOADING_URL, new Object[] { "IOException", url, ioe.getMessage() }, null);
                    continue;
                }
                catch (XmlException xmle)
                {
                    StscState.get().error(XmlErrorCodes.EXCEPTION_LOADING_URL, new Object[] { "XmlException", url, xmle.getMessage() }, null);
                    continue;
                }
            }
        }
        return (Schema[]) result.toArray(new Schema[result.size()]);
    }


    /**
     * Generate java source files for a SchemaTypeSystem.
     * Please do not invoke this method directly as the signature could change unexpectedly.
     * Use {@link org.apache.xmlbeans.XmlBeans#compileXmlBeans}
     *
     * @param system the SchemaTypeSystem to generated java source for
     * @param filer to create the java source files
     * @param options See {@link XmlOptions#setSchemaCodePrinter(org.apache.xmlbeans.SchemaCodePrinter)}
     * @return true if saving the generated source succeeded.
     */
    public static boolean generateTypes(SchemaTypeSystem system, Filer filer, XmlOptions options)
    {
        // partial type systems not allowed to be saved
        if (system instanceof SchemaTypeSystemImpl && ((SchemaTypeSystemImpl)system).isIncomplete())
            return false;
        
        boolean success = true;

        List types = new ArrayList();
        types.addAll(Arrays.asList(system.globalTypes()));
        types.addAll(Arrays.asList(system.documentTypes()));
        types.addAll(Arrays.asList(system.attributeTypes()));

        for (Iterator i = types.iterator(); i.hasNext(); )
        {
            SchemaType type = (SchemaType)i.next();
            if (type.isBuiltinType())
                continue;
            if (type.getFullJavaName() == null)
                continue;

            String fjn = type.getFullJavaName();

            Writer writer = null;

            try
            {
                // Generate interface class
                writer = filer.createSourceFile(fjn);
                SchemaTypeCodePrinter.printType(writer, type, options);
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
                success = false;
            }
            finally {
                try { if (writer != null) writer.close(); } catch (IOException e) {}
            }

            try
            {
                // Generate Implementation class
                fjn = type.getFullJavaImplName();
                writer = filer.createSourceFile(fjn);

                SchemaTypeCodePrinter.printTypeImpl(writer, type, options);
            }
            catch (IOException e)
            {
                System.err.println("IO Error " + e);
                success = false;
            }
            finally {
                try { if (writer != null) writer.close(); } catch (IOException e) {}
            }
        }

        return success;
    }
}
