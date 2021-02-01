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

package org.apache.poi.util;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;


/**
 * Program elements annotated &#64;Internal are intended for
 * POI internal use only.
 *
 * Such elements are not public by design and likely to be removed, have their
 * signature change, or have their access level decreased from public to
 * protected, package, or private in future versions of POI without notice.
 *
 * &#64;Internal elements are eligible for immediate modification or removal and are
 * not subject to the POI project policy of deprecating an element for 2 major
 * releases before removing.
 *
 * @author Yegor Kozlov
 * @since POI-3.6
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {
    String value() default "";
    /**
     * The POI version when an element was declared internal.
     * This is not the same as an &#64;since javadoc annotation
     * which specifies when the feature itself was added.
     * A feature that was made internal after it was added may
     * have a different since and Internal-since version numbers.
     * */
    String since() default "";
}
