/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hpsf;

/**
 * <p>This exception is thrown if one of the {@link PropertySet}'s
 * convenience methods that require a single {@link Section} is called
 * and the {@link PropertySet} does not contain exactly one {@link
 * Section}.</p>
 *
 * <p>The constructors of this class are analogous to those of its
 * superclass and documented there.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class NoSingleSectionException extends HPSFRuntimeException
{

    public NoSingleSectionException()
    {
        super();
    }


    public NoSingleSectionException(final String msg)
    {
        super(msg);
    }


    public NoSingleSectionException(final Throwable reason)
    {
        super(reason);
    }


    public NoSingleSectionException(final String msg, final Throwable reason)
    {
        super(msg, reason);
    }

}
