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
package compile.scomp.common.mockobj;

import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.impl.util.FilerImpl;

import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;
import common.Common;

/**
 *
 *
 */
public class TestFiler implements Filer
{
    private String P = File.separator;
    private FilerImpl impl;
    private boolean isCreateBinaryFile;
    private boolean isCreateSourceFile;
    private Vector binFileVec;
    private Vector srcFileVec;

    public TestFiler()
    {
        String base = new File(Common.OUTPUTROOT).getAbsolutePath() +
                P + "filer" + P;
        String sClass = base + "classes";
        String sSrc = base + "src";
        File fClass = new File(sClass);
        File fSrc = new File(sSrc);
        impl = new FilerImpl(fClass, fSrc, null, true, false);
        binFileVec = new Vector();
        srcFileVec = new Vector();
    }

    public OutputStream createBinaryFile(String typename) throws IOException
    {
        System.out.println("BFS: TypeName: " + typename);
        isCreateBinaryFile = true;
        binFileVec.add(typename);
        return impl.createBinaryFile(typename);
    }

    public Writer createSourceFile(String typename) throws IOException
    {
        System.out.println("SF: TypeName: " + typename);
        srcFileVec.add(typename);
        isCreateSourceFile = true;
        return impl.createSourceFile(typename);
    }

    public boolean isCreateBinaryFile()
    {
        return isCreateBinaryFile;
    }

    public boolean isCreateSourceFile()
    {
        return isCreateSourceFile;
    }

    public Vector getBinFileVec()
    {
        return binFileVec;
    }

    public Vector getSrcFileVec()
    {
        return srcFileVec;
    }
}