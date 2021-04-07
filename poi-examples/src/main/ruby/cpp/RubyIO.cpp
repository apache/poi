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
#include <gcj/cni.h>

#include "ruby.h"
#include "org/apache/poi/RubyOutputStream.h"


/**
 * The native functions declared in org.apache.poi.RubyoutputStream
 *
 * @author aviks
 */

 namespace org {
    namespace apache {
        namespace poi {

            void RubyOutputStream::close(void)
            {
            	rb_funcall3((VALUE ) rubyIO, rb_intern("close"), 0, NULL);
            }
            
            void RubyOutputStream::write(jint toWrite)
            {
               rb_funcall((VALUE ) rubyIO, rb_intern("putc"),1,INT2FIX(toWrite));
            }
        }
    }
}
