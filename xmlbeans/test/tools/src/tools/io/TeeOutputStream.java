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
package tools.io;

import java.io.OutputStream;

public class TeeOutputStream extends OutputStream
{
    OutputStream out1;
    OutputStream out2;

    public TeeOutputStream(OutputStream out1, OutputStream out2)
    {
        this.out1 = out1;
        this.out2 = out2;
    }

    // Override methods of OutputStream
    public void close()
        throws java.io.IOException
    {
        out1.close();
        out2.close();
    }

    public void flush()
            throws java.io.IOException
    {
        out1.flush();
        out2.flush();
    }

    // Implementation of Outputstream's abstract method
    public void write(int b)
            throws java.io.IOException
    {
        out1.write(b);
        out2.write(b);
    }

}
