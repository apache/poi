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

package drt.drtcases;

import java.io.File;
import java.io.IOException;

public class TestEnv extends common.Common
{
    private static File fwroot = new File(FWROOT);
    private static File caseroot = new File(XBEAN_CASE_ROOT);
    private static File outputroot = new File(OUTPUTROOT);

    public TestEnv(){
        super("Test Env");
    }
    public TestEnv(String name){
        super(name);
    }
    /*public static File getRootFile() throws IllegalStateException
    {
        try
        {
            return new File( System.getProperty( "xbean.rootdir" ) ).getCanonicalFile();
        }
        catch( IOException e )
        {
            throw new IllegalStateException(e.toString());
        }
    }

    public static File xbeanCase(String str)
    {
        return (new File(caseroot, str));
    }

    public static File xbeanOutput(String str)
    {
        File result = (new File(outputroot, str));
        File parentdir = result.getParentFile();
        parentdir.mkdirs();
        return result;
    }

    public static void deltree(File dir)
    {
        if (dir.exists())
        {
            if (dir.isDirectory())
            {
                String[] list = dir.list();
                for (int i = 0; i < list.length; i++)
                    deltree(new File(dir, list[i]));
            }
            if (!dir.delete())
                throw new IllegalStateException("Could not delete " + dir);
        }
    } */
}
