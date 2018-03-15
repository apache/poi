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
package misc.detailed;

import junit.framework.TestCase;

import tools.ant.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Date: Feb 8, 2005
 * Time: 11:30:24 AM
 */
public class SampleRunner
    extends TestCase
{


    protected void setUp()
        throws Exception
    {
        proj = new Project();
        proj.setName("Samples Task Tests");
        XMLBEANS_HOME = proj.getBaseDir().getAbsolutePath();
        samples = new ArrayList();
        runSampleTest = new SamplesBuildFileTest("Sample Ant Task Wrapper");
    }

    public void testSamples()
        throws Exception
    {
        loadSampleDirs(new File("./samples"));
        ArrayList exceptions = new ArrayList();
        for (int i = 0; i < samples.size(); i++)
        {

            runSampleTest.call_samples_task(
                ((File) samples.get(i)).getAbsolutePath()
                , "test");
            BuildException e;
            if ((e = runSampleTest.getAnyExceptions()) != null)
            {
                exceptions.add(((File) samples.get(i)).getAbsolutePath());
                exceptions.add(e.getException());
            }
        }
        if (exceptions.size() != 0)
            throw new RuntimeException(getMessageFromExceptions(exceptions));

    }

    private String getMessageFromExceptions(ArrayList ex)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ex.size(); i += 2)
        {
            sb.append("\n\nFILE:" + (String) ex.get(i));
            sb.append(
                "\n **Error: " + ((BuildException) ex.get(i + 1)).getMessage());
        }
        return sb.toString();
    }

    private void loadSampleDirs(File dir)
    {
        assert dir != null && dir.exists();
        File[] files = dir.listFiles(new BuildFilter());
        assert files.length == 1;
        samples.add(files[0]);

    }

    private class BuildFilter
        implements FilenameFilter
    {
        public boolean accept(File file, String name)
        {
            return name.equals("build.xml");
        }
    }

    private class SamplesBuildFileTest
        extends BuildFileTest
    {
        public SamplesBuildFileTest(String name)
        {
            super(name);
        }

        public void call_samples_task(String projectPath, String taskName)
        {
            configureProject(projectPath);
            Project proj = getProject();
            proj.setProperty("xmlbeans.home", XMLBEANS_HOME);
            executeTarget(proj.getDefaultTarget());
            return;
        }

        public BuildException getAnyExceptions()
            throws Exception
        {
            return this.getBuildException();
        }
    }

    ArrayList samples;
    Project proj;
    Target target;
    String XMLBEANS_HOME;
    SamplesBuildFileTest runSampleTest;

}
