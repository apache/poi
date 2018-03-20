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
package tools.JUnit;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

import java.util.Vector;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * <p>This task runs tests from the JUnit testing framework. The latest version
 * of JUnit can be found at <a href="http://www.junit.org">http://www.junit.org</a>.
 * <p/>
 * <p><strong>Note:</strong> This task depends on external libraries not included
 * in the Ant distribution.  See <a href="http://ant.apache.org/manual/install.html#librarydependencies">
 * <p/>
 * Library Dependencies</a> for more information.
 * </p>
 * <p/>
 * <strong>Note</strong>:
 * You must have <code>junit.jar</code> and the class files for the
 * <code>&lt;junit&gt;</code> task in the same classpath.
 * You can do one of:
 * </p><ol>
 * <li>
 * Put both <code>junit.jar</code> and the optional tasks jar file in
 * <p/>
 * <code>ANT_HOME/lib</code>.
 * </li>
 * <li>
 * Do not put either in <code>ANT_HOME/lib</code>, and instead
 * include their locations in your <code>CLASSPATH</code> environment variable.
 * </li>
 * <li>
 * Do neither of the above, and instead, specify their locations using
 * a <code>&lt;classpath&gt;</code> element in the build file.
 * <p/>
 * See <a href="http://ant.apache.org/faq.html#delegating-classloader" target="_top">the
 * FAQ</a> for details.</p>
 * <p/>
 * <p/>
 * <pre>
 * Usage:
 * This task is derived from the Java task and supports all attributes and nested
 * elements of the Java task. Additionally it supports the following attributes
 * <p/>
 * formatter       =  Optional class that implements tools.JUnit.JUnitXResultFormatter
 *                    to be passed to the JUnit runner for formatting the results
 * reportFile      =  File for the formatter to write too.
 *                    (Required if formatter is specified. The formatter may choose
 *                     to ignore this)
 * showOutput      =  Tells the formatter to show the output of the tests.
 *                    The default formatter ignores this flag, since it anyway
 *                    displays everything to stdout/stderr.
 * classes        = Ignored unless the fileset is not valid. If the user has specified
 *                  valid classnames, this variable is used to run the classes. Hacky but
 *                  allows the user to use the same command line parameter as when specifying filesets.
 * <p/>
 * The tests to run are specified using nested <code>Fileset</code>s elements
 * The task picks up any .class or .java files from the filesets and tries to run
 * them as JUnit tests.
 * </pre>
 */
public class JUnitXTask extends org.apache.tools.ant.taskdefs.Java {
    // Task attributes vars
    private String reportFile;
    private boolean showOutput = false;
    private String formatterClass;
    private String[] classFiles = null;

    public final static String resultListener="listener";
    public final static String outFile="file";

    /**
     * the list of filesets containing the testcase filename rules
     */
    private Vector filesets = new Vector();

    /**
     * Add a new fileset instance to this batchtest. Whatever the fileset is,
     * only filename that are <tt>.java</tt> or <tt>.class</tt> will be
     * considered as 'candidates'.
     *
     * @param fs the new fileset containing the rules to get the testcases.
     */
    public void addFileSet(FileSet fs) {
        filesets.addElement(fs);
    }

    // Additional Attributes
    /**
     *
     */
    public void setClasses(String classes) {
        this.classFiles = classes.split(",");
    }

    /**
     * Set the file to write the test run report to
     *
     * @param reportFile
     */
    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    /**
     * Sets the class to be used as the Result formatter for the test results
     * This class should implement tools.JUnit.JUnitXResultFormatter
     *
     * @param fmtClass
     */
    public void setFormatter(String fmtClass) {
        this.formatterClass = fmtClass;
    }

    /**
     * Controls whether the output from the tests is sent to stdout/stderr.
     *
     * @param show
     */
    public void setShowOutput(boolean show) {
        this.showOutput = show;
    }

    public void execute() throws BuildException {
        validate();
        String[] files = getFilenames();
        File temp = null;
        String fName = Long.toString(System.currentTimeMillis());


        //issue w/ JDK 1.4 IOException
        // temp = File.createTempFile(,null);
        temp = new File(fName);

        try {
            temp.deleteOnExit();
            // System.out.println(temp.toString());
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));
            System.out.println(temp.toString());
            for (int i = 0; i < files.length; i++)
                out.write(javaToClass(files[i]) + "\n");
            out.close();
        }
        catch (java.io.IOException ioe) {
            throw new BuildException("Could not write out temporary file for " +
                    "passing arguments to the JUnit runner: " +
                    ioe.getMessage());
        }

        super.setClassname("tools.JUnit.JUnitXRunner");
        // Args: tempFile reportFormatter outFile <showOutput>
        super.createArg().setFile(temp);
        if (formatterClass != null) {
            super.createArg().setValue("-"+resultListener);
            super.createArg().setValue(this.formatterClass);
            super.createArg().setValue("-"+outFile);
            super.createArg().setValue(this.reportFile);
        }
        if (showOutput){
            super.createArg().setValue("-showoutput");
            super.createArg().setValue(new Boolean(this.showOutput).toString());
        }
        super.execute();
    }

    /**
     * Validates the attributes
     */
    private void validate() {
        // Check if any filesets have been specified
        if (filesets.size() == 0)
            throw new BuildException("You have to specify atleast one Fileset");

        // If Custom ResultFormatter is specified, then an outfile should be
        // specified. The default ResultFormatter write only to Stdout
        if ((formatterClass != null) && (reportFile == null))
            throw new BuildException(
                    "Using custom ReportFormatter: " +
                    "reportFile attribute should be specified");

    }

    /**
     * Iterate over all filesets and return the filename of all files
     * that end with <tt>.java</tt> or <tt>.class</tt>. This is to avoid
     * wrapping a <tt>JUnitTest</tt> over an xml file for example. A Testcase
     * is obviously a java file (compiled or not).
     *
     * @return an array of filenames without their extension. As they should
     *         normally be taken from their root, filenames should match their fully
     *         qualified class name (If it is not the case it will fail when running the test).
     *         For the class <tt>org/apache/Whatever.class</tt> it will return <tt>org/apache/Whatever</tt>.
     */
    private String[] getFilenames() {
        Vector v = new Vector();
        final int size = this.filesets.size();
        for (int j = 0; j < size; j++) {
            FileSet fs = (FileSet) filesets.elementAt(j);
            //    DirectoryScanner ds = fs.getDirectoryScanner(project);
            DirectoryScanner ds = fs.getDirectoryScanner(fs.getProject());
            ds.scan();
            String[] f = ds.getIncludedFiles();
            //check for an actual class here
            if (f.length == 0 && size == 1)
                f = classFiles;
            for (int k = 0; k < f.length; k++) {
                String pathname = f[k];
                if (pathname.endsWith(".java")) {
                    v.addElement(
                            pathname.substring(0,
                                    pathname.length() - ".java".length()));
                } else if (pathname.endsWith(".class")) {
                    // DOn't try to run inner classes
                    if (pathname.indexOf("$") == -1)
                        v.addElement(
                                pathname.substring(0,
                                        pathname.length() - ".class".length()));
                } else
                    try {
                        Class.forName(pathname);
                        v.addElement(pathname);
                    }
                    catch (ClassNotFoundException e) {
                        ;//filename is not a class
                    }
            }
        }
        String[] files = new String[v.size()];
        v.copyInto(files);
        return files;
    }

    /**
     * Convenient method to convert a pathname without extension to a
     * fully qualified classname. For example <tt>org/apache/Whatever</tt> will
     * be converted to <tt>org.apache.Whatever</tt>
     *
     * @param filename the filename to "convert" to a classname.
     * @return the classname matching the filename.
     */
    public static final String javaToClass(String filename) {
        return filename.replace(File.separatorChar, '.');
    }


}
