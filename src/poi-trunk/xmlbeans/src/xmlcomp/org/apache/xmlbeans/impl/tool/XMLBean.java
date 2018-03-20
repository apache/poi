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

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;
import java.net.URI;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.common.IOUtil;

/**
 * Modeled after Ant's javac and zip tasks.
 *
 * Schema files to process, or directories of schema files, are set with the 'schema'
 * attribute, and can be filtered with 'includes' and 'excludes'.
 * Alternatively, one or more nested &lt;fileset&gt; elements can specify the
 * files and directories to be used to generate this XMLBean.
 * The include set can also define .java files that should be built as well.
 * See the FileSet documentation at http://jakarta.apache.org/ant/manual/index.html
 * for instructions on FileSets if you are unfamiliar with their usage.
 */

public class XMLBean extends MatchingTask
{
    private ArrayList   schemas = new ArrayList();

    private Set         mdefnamespaces;

    private Path        classpath;

    private File        destfile,
                        schema,
                        srcgendir,
                        classgendir;

    private boolean     quiet,
                        verbose,
                        debug,
                        optimize,
                        download,
                        srconly,
                        noupa,
                        nopvr,
                        noann,
                        novdoc,
                        noext = false,
                        failonerror = true,
                        fork = true,
                        includeAntRuntime = true,
                        noSrcRegen,
                        includeJavaRuntime = false;

    private String      typesystemname,
                        forkedExecutable,
                        compiler,
                        debugLevel,
                        memoryInitialSize,
                        memoryMaximumSize,
                        catalog,
                        javasource;

    private List        extensions = new ArrayList();

    private HashMap     _extRouter = new HashMap(5);

    private static final String XSD = ".xsd",
                                WSDL = ".wsdl",
                                JAVA = ".java",
                                XSDCONFIG = ".xsdconfig";


    public void execute() throws BuildException
    {
        /* VALIDATION */
        //required
        if (schemas.size() == 0
            && schema == null
            && fileset.getDir(project) == null)
        {
            String msg = "The 'schema' or 'dir' attribute or a nested fileset is required.";
            if (failonerror)
                throw new BuildException(msg);
            else
            {
                log(msg, Project.MSG_ERR);
                return;
            }
        }

        _extRouter.put(XSD, new HashSet());
        _extRouter.put(WSDL, new HashSet());
        _extRouter.put(JAVA, new HashSet());
        _extRouter.put(XSDCONFIG, new HashSet());

        File theBasedir = schema;

        if (schema != null)
        {
            if (schema.isDirectory())
            {
                FileScanner scanner = getDirectoryScanner(schema);
                String[] paths = scanner.getIncludedFiles();
                processPaths(paths, scanner.getBasedir());
            }
            else
            {
                theBasedir = schema.getParentFile();
                processPaths(new String[] { schema.getName() }, theBasedir);
            }
        }

        if (fileset.getDir(project) != null)
            schemas.add(fileset);

        Iterator si = schemas.iterator();
        while (si.hasNext())
        {
            FileSet fs = (FileSet) si.next();
            FileScanner scanner = fs.getDirectoryScanner(project);
            File basedir = scanner.getBasedir();
            String[] paths = scanner.getIncludedFiles();

            processPaths(paths, basedir);
        }

        Set xsdList = (Set) _extRouter.get(XSD);
        Set wsdlList = (Set) _extRouter.get(WSDL);

        if (xsdList.size() + wsdlList.size() == 0)
        {
            log("Could not find any xsd or wsdl files to process.", Project.MSG_WARN);
            return;
        }

        //optional
        Set javaList = (Set) _extRouter.get(JAVA);
        Set xsdconfigList = (Set) _extRouter.get(XSDCONFIG);

        if (srcgendir == null && srconly)
            srcgendir = classgendir;

        if (destfile == null && classgendir == null && ! srconly)
            destfile = new File("xmltypes.jar");

        if (verbose)
            quiet = false;

        /* EXECUTION */

        File[] xsdArray = (File[]) xsdList.toArray(new File[xsdList.size()]);
        File[] wsdlArray = (File[]) wsdlList.toArray(new File[wsdlList.size()]);
        File[] javaArray = (File[]) javaList.toArray(new File[javaList.size()]);
        File[] xsdconfigArray = (File[]) xsdconfigList.toArray(new File[xsdconfigList.size()]);
        ErrorLogger err = new ErrorLogger(verbose);

        boolean success = false;

        try
        {
            // create a temp directory
            File tmpdir = null;
            if (srcgendir == null || classgendir == null)
            {
                tmpdir = SchemaCodeGenerator.createTempDir();
            }
            if (srcgendir == null)
                srcgendir = IOUtil.createDir(tmpdir, "src");
            if (classgendir == null)
                classgendir = IOUtil.createDir(tmpdir, "classes");

            // use the system classpath if user didn't provide any
            if (classpath == null)
            {
                classpath = new Path(project);
                classpath.concatSystemClasspath();
            }

            // prepend the output directory on the classpath
            Path.PathElement pathElement = classpath.createPathElement();
            pathElement.setLocation(classgendir);

            String[] paths = classpath.list();
            File[] cp = new File[paths.length];
            for (int i = 0; i < paths.length; i++)
                cp[i] = new File(paths[i]);

            // generate the source
            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setBaseDir(theBasedir);
            params.setXsdFiles(xsdArray);
            params.setWsdlFiles(wsdlArray);
            params.setJavaFiles(javaArray);
            params.setConfigFiles(xsdconfigArray);
            params.setClasspath(cp);
            params.setName(typesystemname);
            params.setSrcDir(srcgendir);
            params.setClassesDir(classgendir);
            params.setNojavac(true); // always compile using javac task below
            params.setDebug(debug);
            params.setVerbose(verbose);
            params.setQuiet(quiet);
            params.setDownload(download);
            params.setExtensions(extensions);
            params.setErrorListener(err);
            params.setCatalogFile(catalog);
            params.setIncrementalSrcGen(noSrcRegen);
            params.setMdefNamespaces(mdefnamespaces);
            params.setNoUpa(noupa);
            params.setNoPvr(nopvr);
            params.setNoAnn(noann);
            params.setNoVDoc(novdoc);
            params.setNoExt(noext);
            params.setJavaSource(javasource);
            success = SchemaCompiler.compile(params);

            if (success && !srconly) {
                long start = System.currentTimeMillis();

                // compile the source
                Javac javac = new Javac();
                javac.setProject(project);
                javac.setTaskName(getTaskName());
                javac.setClasspath(classpath);
                if (compiler != null) javac.setCompiler(compiler);
                javac.setDebug(debug);
                if (debugLevel != null) javac.setDebugLevel(debugLevel);
                javac.setDestdir(classgendir);
                javac.setExecutable(forkedExecutable);
                javac.setFailonerror(failonerror);
                javac.setFork(fork);
                if (javasource != null)
                {
                    javac.setSource(javasource);
                    javac.setTarget(javasource);
                }
                else
                {
                    javac.setSource("1.4");
                    javac.setTarget("1.4");
                }
                javac.setIncludeantruntime(includeAntRuntime);
                javac.setIncludejavaruntime(includeJavaRuntime);
                javac.setSrcdir(new Path(project, srcgendir.getAbsolutePath()));
                if (memoryInitialSize != null) javac.setMemoryInitialSize(memoryInitialSize);
                if (memoryMaximumSize != null) javac.setMemoryMaximumSize(memoryMaximumSize);
                javac.setOptimize(optimize);
                javac.setVerbose(verbose);
                javac.execute();

                long finish = System.currentTimeMillis();
                if (!quiet)
                    log("Time to compile code: " + ((double)(finish - start) / 1000.0) + " seconds");

                if (destfile != null)
                {
                    // jar the compiled classes
                    Jar jar = new Jar();
                    jar.setProject(project);
                    jar.setTaskName(getTaskName());
                    jar.setBasedir(classgendir);
                    jar.setDestFile(destfile);
                    jar.execute();
                }
            }

            if (tmpdir != null) {
                SchemaCodeGenerator.tryHardToDelete(tmpdir);
            }
        }
        catch (BuildException e)
        {
            // re-throw anything thrown from javac or jar task
            throw e;
        }
        catch (Throwable e)
        {
            //interrupted means cancel
            if (e instanceof InterruptedException || failonerror)
                throw new BuildException(e);
            
            log("Exception while building schemas: " + e.getMessage(), Project.MSG_ERR);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log(sw.toString(), Project.MSG_VERBOSE);
        }

        if (!success && failonerror)
            throw new BuildException(); //stop the build
    }

    private void processPaths(String[] paths, File baseDir)
    {
        for (int i = 0; i < paths.length; i++)
        {
            int dot = paths[i].lastIndexOf('.');
            if (dot > -1)
            {
                String path = paths[i];
                String possExt = path.substring(dot).toLowerCase();
                Set set = (Set) _extRouter.get(possExt);

                if (set != null)
                    set.add(new File(baseDir, path));
            }
        }
    }

    public void addFileset(FileSet fileset)
    {
        schemas.add(fileset);
    }

    /////////////////////////////
    //Getter/Setters
    public File getSchema()
    {
        return schema;
    }

    /**
     * A file that points to either an individual schema file or a directory of files.
     * It is optional only if one or more &lt;fileset&gt; elements are nested in this
     * task.
     * @param schema Required, unless a fileset element is nested.
     */
    public void setSchema(File schema)
    {
        this.schema = schema;
    }

    /**
     * The classpath to use if schemas in the fileset import definitions that are
     * supplied by other compiled xml beans JAR files, or if .java files are in the
     * schema fileset.
     * @param classpath Optional.
     */
    public void setClasspath(Path classpath)
    {
        if (this.classpath != null)
            this.classpath.append(classpath);
        else
            this.classpath = classpath;
    }

    /**
     * Adds a path to the classpath.
     */
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(project);
        }
        return classpath.createPath();
    }

    /**
     * Adds a reference to a classpath defined elsewhere.
     * @param classpathref Optional.
     */
    public void setClasspathRef(Reference classpathref)
    {
        if (classpath == null)
            classpath = new Path(project);

        classpath.createPath().setRefid(classpathref);
    }

    public Path getClasspath()
    {
        return classpath;
    }

    public File getDestfile()
    {
        return destfile;
    }

    /**
     * Define the name of the jar file created.  For instance, "myXMLBean.jar"
     * will output the results of this task into a jar with the same name.
     * Optional, defaults to "xmltypes.jar".
     * @param destfile Optional.
     */
    public void setDestfile(File destfile)
    {
        this.destfile = destfile;
    }

    public File getSrcgendir()
    {
        return srcgendir;
    }

    /**
     * Set a location to generate .java files into.  Optional, defaults to
     * a temp dir.
     * @param srcgendir Optional.
     */
    public void setSrcgendir(File srcgendir)
    {
        this.srcgendir = srcgendir;
    }

    public File getClassgendir()
    {
        return classgendir;
    }

    /**
     * Set a location to generate .class files into.  Optional, defaults to
     * a temp dir.
     * @param classgendir Optional.
     */
    public void setClassgendir(File classgendir)
    {
        this.classgendir = classgendir;
    }

    /**
     * Choose the implementation for this particular task.
     *
     * @since Ant 1.5
     */
    public void setCompiler(String compiler)
    {
        this.compiler = compiler;
    }

    public boolean isDownload()
    {
        return download;
    }

    /**
     * Set to true to permit the compiler to download URLs for imports
     * and includes.  Defaults to false, meaning all imports and includes
     * must be copied locally.
     * @param download Optional.
     */
    public void setDownload(boolean download)
    {
        this.download = download;
    }

    /**
     * If true, compiles with optimization enabled.
     */
    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    /** Gets the optimize flag. */
    public boolean getOptimize() {
        return optimize;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    /**
     * Controls the amount of output.  Defaults to true.
     * @param verbose Optional.
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    public boolean isQuiet()
    {
        return quiet;
    }

    /**
     * Controls the amount of output.  Defaults to false.
     * @param quiet Optional.
     */
    public void setQuiet(boolean quiet)
    {
        this.quiet = quiet;
    }

    public boolean isDebug()
    {
        return debug;
    }

    /**
     * Get the value of debugLevel.
     * @return value of debugLevel.
     */
    public String getDebugLevel() {
        return debugLevel;
    }

    /**
     * Keyword list to be appended to the -g command-line switch.
     *
     * This will be ignored by all implementations except modern
     * and classic(ver >= 1.2). Legal values are none or a
     * comma-separated list of the following keywords: lines, vars,
     * and source. If debuglevel is not specified, by default, :none
     * will be appended to -g. If debug is not turned on, this attribute
     * will be ignored.
     *
     * @param v  Value to assign to debugLevel.
     */
    public void setDebugLevel(String  v) {
        this.debugLevel = v;
    }

    /**
     * Generate debugging symbols.
     * @param debug Optional.
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    /**
     * If true, forks the javac compiler.
     *
     * @param f "true|false|on|off|yes|no"
     */
    public void setFork(boolean f) {
        fork = f;
    }

    /**
     * Sets the the name of the javac executable.
     *
     * <p>Ignored unless fork is true or extJavac has been specified
     * as the compiler.</p>
     */
    public void setExecutable(String forkExec) {
        forkedExecutable = forkExec;
    }

    public String getExecutable() {
        return forkedExecutable;
    }

    public boolean isSrconly()
    {
        return srconly;
    }

    /**
     * A value of true means that only source will be generated.  Optional,
     * default is false.
     * @param srconly Optional.
     */
    public void setSrconly(boolean srconly)
    {
        this.srconly = srconly;
    }

    public String getTypesystemname()
    {
        return typesystemname;
    }

    /**
     * One or more SchemaCompiler extensions can be passed in via the &lt;extension> subelement.
     *  Schema Compiler extensions must implement the interface com.xbean.too.SchemaCompilerExtension
     */
    public Extension createExtension() {
        Extension e = new Extension();
        extensions.add(e);
        return e;
    }

    /**
     * One or more namespaces in which duplicate definitions are to be ignored
     * can be passed in via the &lt;ignoreDuplicatesInNamespaces> subelement.
     */
    public void setIgnoreDuplicatesInNamespaces(String namespaces) {
        mdefnamespaces = new HashSet();
        StringTokenizer st = new StringTokenizer(namespaces, ",");
        while (st.hasMoreTokens())
        {
          String namespace = st.nextToken().trim();
          mdefnamespaces.add(namespace);
        }
    }

    public String getIgnoreDuplicatesInNamespaces() {
        if (mdefnamespaces == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        Iterator i = mdefnamespaces.iterator();
        while (i.hasNext()) {
            buf.append((String)i.next());
            if (i.hasNext()) {
                buf.append(",");
            }
        }
        return buf.toString();
    }

    /**
     * The name of the package that the TypeSystemHolder class should be
     * generated in. Normally this should be left unspecified.  None of
     * the xml beans are generated in this package.
     * <BR><BR>Use .xsdconfig files to modify xml bean package or class names.
     * @param typesystemname Optional.
     */
    public void setTypesystemname(String typesystemname)
    {
        this.typesystemname = typesystemname;
    }

    public boolean isFailonerror()
    {
        return failonerror;
    }

    /**
     * Determines whether or not the ant target will continue if the XMLBean
     * creation encounters a build error.  Defaults to true.  Optional.
     * @param failonerror Optional.
     */
    public void setFailonerror(boolean failonerror)
    {
        this.failonerror = failonerror;
    }

    public boolean isIncludeAntRuntime()
    {
        return includeAntRuntime;
    }

    public void setIncludeAntRuntime(boolean includeAntRuntime)
    {
        this.includeAntRuntime = includeAntRuntime;
    }

    public boolean isIncludeJavaRuntime()
    {
        return includeJavaRuntime;
    }

    public void setIncludeJavaRuntime(boolean includeJavaRuntime)
    {
        this.includeJavaRuntime = includeJavaRuntime;
    }

    public boolean isNoSrcRegen()
    {
        return noSrcRegen;
    }

    public void setNoSrcRegen(boolean noSrcRegen)
    {
        this.noSrcRegen = noSrcRegen;
    }

    /**
     * Set the initial memory size of the underlying javac process.
     */
    public String getMemoryInitialSize()
    {
        return memoryInitialSize;
    }

    public void setMemoryInitialSize(String memoryInitialSize)
    {
        this.memoryInitialSize = memoryInitialSize;
    }

    /**
     * Set the maximum memory size of the underlying javac process.
     */
    public String getMemoryMaximumSize()
    {
        return memoryMaximumSize;
    }

    public void setMemoryMaximumSize(String memoryMaximumSize)
    {
        this.memoryMaximumSize = memoryMaximumSize;
    }

    /**
     * Do not enforce the unique particle attribution rule.
     */
    public void setNoUpa(boolean noupa)
    {
        this.noupa = noupa;
    }

    public boolean isNoUpa()
    {
        return noupa;
    }

    /**
     * Do not enforce the particle valid (restriction) rule.
     */
    public void setNoPvr(boolean nopvr)
    {
        this.nopvr = nopvr;
    }

    public boolean isNoPvr()
    {
        return nopvr;
    }

    /**
     * Skip over schema &lt;annotation%gt; elements.
     */
    public void setNoAnnotations(boolean noann)
    {
        this.noann = noann;
    }

    public boolean isNoAnnotations()
    {
        return noann;
    }

    /**
     * Do not validate the contents of schema &lt;documentation&gt; elements.
     */
    public void setNoValidateDoc(boolean novdoc)
    {
        this.novdoc = novdoc;
    }

    public boolean isNoValidateDoc()
    {
        return novdoc;
    }

    /**
     * Ignore extensions found in .xsdconfig files
     * @param novdoc
     */
    public void setNoExt(boolean noext)
    {
        this.noext = noext;
    }

    public boolean isNoExt()
    {
        return noext;
    }

    /**
     * Generate java source compatible with the given version.  Currently,
     * only "1.4" or "1.5" are supported and "1.4" is the default.
     */
    public void setJavaSource(String javasource)
    {
        this.javasource = javasource;
    }

    public String getJavaSource()
    {
        return javasource;
    }

  //REVIEW this allows people to deal with the case where they drag in
  //more files for compilation than they should.  not sure if this is
  //a good thing or not
  private String source = null;
  public void setSource(String s) { source = s; }

    /**
     * Gets the XML Catalog file for org.apache.xml.resolver.tools.CatalogResolver. (Note: needs resolver.jar from http://xml.apache.org/commons/components/resolver/index.html)
     */
    public String getCatalog()
    {
        return catalog;
    }

    /**
     * Sets the XML Catalog file for org.apache.xml.resolver.tools.CatalogResolver. (Note: needs resolver.jar from http://xml.apache.org/commons/components/resolver/index.html)
     */
    public void setCatalog(String catalog)
    {
        this.catalog = catalog;
    }

    private static URI uriFromFile(File f)
    {
        if (f == null)
            return null;

        try
        {
            return f.getCanonicalFile().toURI();
        }
        catch(java.io.IOException e)
        {
            // Don't spit out an exception here because on Windows you'll get one
            // if the filename is "aux", "lpt1", etc. It's the caller's responsibility
            // to deal with those cases correctly, usually by calling FileSvc.invalidPathCheck()
            // MessageSvc.get().logException(e);
            return f.getAbsoluteFile().toURI();
        }
    }

    public class ErrorLogger extends AbstractCollection
    {
        private boolean _noisy;
        private URI _baseURI;

        public ErrorLogger(boolean noisy)
        {
            _noisy = noisy;
            _baseURI = uriFromFile(project.getBaseDir());
        }

        public boolean add(Object o)
        {
            if (o instanceof XmlError)
            {
                XmlError err = (XmlError)o;
                if (err.getSeverity() == XmlError.SEVERITY_ERROR)
                    log(err.toString(_baseURI), Project.MSG_ERR);
                else if (err.getSeverity() == XmlError.SEVERITY_WARNING)
                    log(err.toString(_baseURI), Project.MSG_WARN);
                else if (_noisy)
                    log(err.toString(_baseURI), Project.MSG_INFO);
            }
            return false;
        }

        public Iterator iterator()
        {
            return Collections.EMPTY_LIST.iterator();
        }

        public int size()
        {
            return 0;
        }
    }
}
