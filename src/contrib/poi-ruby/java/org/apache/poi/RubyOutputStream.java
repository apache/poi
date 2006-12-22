/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.apache.poi;


import java.io.OutputStream;
import java.io.IOException;

/**
 * @author aviks
 * Wrap a java.io.OutputStream around a Ruby IO object
 */

public class RubyOutputStream extends OutputStream {

	//pointer to native ruby VALUE 
    protected long rubyIO;

    public RubyOutputStream (long rubyIO)
    {
        this.rubyIO = rubyIO;
//        incRef();
    }

    protected void finalize()
        throws Throwable
    {
//        decRef();
    }

//    protected native void incRef();
//    protected native void decRef();

    public native void close()
        throws IOException;


	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public native void write(int arg0) throws IOException;
}

