/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.util;

import java.io.InputStream;
import java.io.IOException;

/**
 * Implementation of a BlockingInputStream to provide data to 
 * RawDataBlock that expects data in 512 byte chunks.  Useful to read
 * data from slow (ie, non FileInputStream) sources, for example when 
 * reading an OLE2 Document over a network. 
 *
 * Possible extentions: add a timeout. Curently a call to read(byte[]) on this
 *    class is blocking, so use at your own peril if your underlying stream blocks. 
 *
 * @author Jens Gerhard
 * @author aviks - documentation cleanups. 
 */
public class BlockingInputStream
      extends InputStream
{
      protected InputStream is;

      public BlockingInputStream(InputStream is)
      {
          this.is = is;
      }

      public int available()
        throws IOException
      {
          return is.available();
      }

      public void close()
        throws IOException
      {
          is.close();
      }

      public void mark(int readLimit)
      {
          is.mark(readLimit);
      }

      public boolean markSupported()
      {
          return is.markSupported();
      }

      public int read()
        throws IOException
      {
          return is.read();
      }
      
      /**
       * We had to revert to byte per byte reading to keep
       * with slow network connections on one hand, without
       * missing the end-of-file. 
       * This is the only method that does its own thing in this class
       *    everything else is delegated to aggregated stream. 
       * THIS IS A BLOCKING BLOCK READ!!!
       */
      public int read(byte[] bf)
        throws IOException
      {
          
          int i = 0;
          int b = 4611;
          while ( i < bf.length )
          {
              b = is.read();
              if ( b == -1 )
                  break;
              bf[i++] = (byte) b;
          }
          if ( i == 0 && b == -1 )
              return -1;
          return i;
      }

      public int read(byte[] bf, int s, int l)
        throws IOException
      {
          return is.read(bf, s, l);
      }

      public void reset()
        throws IOException
      {
          is.reset();
      }

      public long skip(long n)
        throws IOException
      {
          return is.skip(n);
      }
}

