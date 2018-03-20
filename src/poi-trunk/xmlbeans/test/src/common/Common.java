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
package common;

import org.apache.xmlbeans.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.xml.namespace.QName;

/**
 *
 */
public class Common extends TestCase
{
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String P = File.separator;

    public static String FWROOT = getRootFile();
    public static String CASEROOT = getCaseLocation();
    public static String XBEAN_CASE_ROOT = getCaseLocation() + P + "xbean";

    //location of files under "cases folder"
    public static String OUTPUTROOT = FWROOT+P+"build" + P + "test" + P + "output";


    public LinkedList errorList;
    public XmlOptions xmOpts;

    public Common(String name)
    {
        super(name);
        errorList = new LinkedList();
        xmOpts = new XmlOptions();
        xmOpts.setErrorListener(errorList);
    }

    /**
     * If System.property for 'xbean.rootdir' == null
     * use '.' as basePath
     * '.' should be where the build.xml file lives
     *
     * @return
     * @throws IllegalStateException
     */
    public static String getRootFile() throws IllegalStateException
    {
        String baseDir = System.getProperty("xbean.rootdir");
        if (baseDir == null)
            return new File(".").getAbsolutePath();
        else
            return new File(baseDir).getAbsolutePath();
    }

    /**
     * If System.property for 'cases.location' == null
     * use '.' as basePath and add test/cases/xbea'.'
     * should be where the build.xml file lives
     *
     * @return
     * @throws IllegalStateException
     */
    public static String getCaseLocation() throws IllegalStateException
    {
        String baseDir = System.getProperty("cases.location");
        if (baseDir == null) {
            return new File("." + P + "test" + P + "cases").getAbsolutePath();
        } else {
            return new File(baseDir).getAbsolutePath();
        }
    }

    /**
     * Gets a case file from under CASEROOT with location passed in as strPath
     *
     * @param strPath
     * @return file Object for references location
     */
    public static File xbeanCase(String strPath)
    {
        return (new File(CASEROOT, strPath));
    }

    /**
     * Creates directory under output directory as noted by strPath
     *
     * @param strPath
     * @return File Object specified by strPath
     */
    public static File xbeanOutput(String strPath)
    {
        File result = (new File(OUTPUTROOT, strPath));
        File parentdir = result.getParentFile();
        parentdir.mkdirs();
        return result;
    }

    /**
     * Recursively deletes files under specified directory
     *
     * @param dir
     */
    public static void deltree(File dir)
    {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                String[] list = dir.list();
                for (int i = 0; i < list.length; i++)
                    deltree(new File(dir, list[i]));
            }
            if (!dir.delete())
                System.out.println("Could not delete " + dir);
            //throw new IllegalStateException("Could not delete " + dir);
        }
    }

    /**
     * Convenience method for displaying errorListener contents after validation
     *
     * @param errors
     */
    public static void listErrors(List errors)
    {
        for (int i = 0; i < errors.size(); i++) {
            XmlError error = (XmlError) errors.get(i);
            if (error.getSeverity() == XmlError.SEVERITY_ERROR)
                System.out.println(error.toString());
        }
    }

    /**
     * check list of errors/warnings/msgs and print them. Return true if errors found
     * @param errors
     * @return
     */
    public static boolean printOptionErrMsgs(Collection errors)
    {
        boolean errFound = false;
        if (!errors.isEmpty()) {
            for (Iterator i = errors.iterator(); i.hasNext();) {
                XmlError eacherr = (XmlError) i.next();
                int errSeverity = eacherr.getSeverity();
                if (errSeverity == XmlError.SEVERITY_ERROR) {
                    System.out.println("Err Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                    errFound = true;
                } else if (errSeverity == XmlError.SEVERITY_WARNING) {
                    System.out.println("Warning Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                } else if (errSeverity == XmlError.SEVERITY_INFO) {
                    System.out.println("Info Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                }
            }
            errors.clear();
        }
        return errFound;
    }

    /**
     * Validate schemas to instance based on the docType
     * @param schemas
     * @param instances
     * @param docType
     * @throws Exception
     */
    public static void validateInstance(String[] schemas, String[] instances, QName docType) throws Exception
    {
        SchemaTypeLoader stl = makeSchemaTypeLoader(schemas);
        XmlOptions options = new XmlOptions();

        if (docType != null) {
            SchemaType docSchema = stl.findDocumentType(docType);
            Assert.assertTrue(docSchema != null);
            options.put(XmlOptions.DOCUMENT_TYPE, docSchema);
        }

        for (int i = 0; i < instances.length; i++) {
            XmlObject x =
                    stl.parse((String) instances[i], null, options);

            //if (!startOnDocument) {
            //    XmlCursor c = x.newCursor();
            //    c.toFirstChild();
            //    x = c.getObject();
            //    c.dispose();
            //}

            List xel = new ArrayList();

            options.put(XmlOptions.ERROR_LISTENER, xel);

            boolean isValid = x.validate(options);

            if (!isValid) {
                StringBuffer errorTxt = new StringBuffer("Invalid doc, expected a valid doc: ");
                errorTxt.append("Instance(" + i + "): ");
                errorTxt.append(x.xmlText());
                errorTxt.append("Errors: ");
                for (int j = 0; j < xel.size(); j++)
                    errorTxt.append(xel.get(j) + "\n");
                System.err.println(errorTxt.toString());
                throw new Exception("Instance not valid\n" + errorTxt.toString());
            }
        }
    }


    /**
     * Convenience method for creating an XmlObject from a String
     *
     * @param XsdAsString
     * @return
     */
    public static XmlObject compileXsdString(String XsdAsString)
    {
        XmlObject xobj = null;
        try {
            xobj = XmlObject.Factory.parse(XsdAsString);
        } catch (XmlException xme) {
            if (!xme.getErrors().isEmpty()) {
                for (Iterator itr = xme.getErrors().iterator(); itr.hasNext();) {
                    System.out.println("Parse Errors :" + itr.next());
                }
            }
        } finally {
            Assert.assertNotNull(xobj);
            return xobj;
        }
    }


    /**
     * Convenience method for creating an XmlObject from a File referenced as a String of the path to the file
     *
     * @param XsdFilePath
     * @return
     */
    public static XmlObject compileXsdFile(String XsdFilePath)
    {
        XmlObject xobj = null;
        try {
            xobj = XmlObject.Factory.parse(new File(XsdFilePath));
        } catch (XmlException xme) {
            if (!xme.getErrors().isEmpty()) {
                for (Iterator itr = xme.getErrors().iterator(); itr.hasNext();) {
                    System.out.println("Parse Errors :" + itr.next());
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            ioe.getMessage();
        } finally {
            Assert.assertNotNull(xobj);
            return xobj;
        }
    }

    /**
     * Convenience method to create a SchemaTypeLoader from a set of xsds
     *
     * @param schemas
     * @return
     * @throws Exception
     */
    public static SchemaTypeLoader makeSchemaTypeLoader(String[] schemas)
            throws Exception
    {
        XmlObject[] schemaDocs = new XmlObject[schemas.length];

        for (int i = 0; i < schemas.length; i++) {
            schemaDocs[i] =
                    XmlObject.Factory.parse(schemas[i]);
        }

        return XmlBeans.loadXsd(schemaDocs);
    }

    /**
     * Is the JVM being used a 1.4 version?
     * Used for tests involving the javasource=1.5 compilation setting
     *
     * @return true if java.version starts with 1.4
     */
    public static boolean isJDK14()
    {
        return System.getProperty("java.version").startsWith("1.4");
    }

    /**
     * Convenience class for creating tests in a multithreaded env
     */
    public static abstract class TestThread extends Thread
    {
        protected Throwable _throwable;
        protected boolean _result;
        protected XmlOptions xm;
        protected ArrayList errors;

        public TestThread()
        {
            xm = new XmlOptions();
            ArrayList errors = new ArrayList();
            xm.setErrorListener(errors);
            xm.setValidateOnSet();
        }

        public Throwable getException()
        {
            return _throwable;
        }

        public boolean getResult()
        {
            return _result;
        }


    }


}
