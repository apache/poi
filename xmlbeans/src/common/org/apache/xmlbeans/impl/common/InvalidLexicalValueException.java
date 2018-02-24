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

package org.apache.xmlbeans.impl.common;

import javax.xml.stream.Location;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 24, 2003
 */
public class InvalidLexicalValueException
    extends RuntimeException
{
    private Location _location;

    public InvalidLexicalValueException()
    {
        super();
    }

    public InvalidLexicalValueException(String msg)
    {
        super(msg);
    }

    public InvalidLexicalValueException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public InvalidLexicalValueException(Throwable cause)
    {
        super(cause);
    }

    public InvalidLexicalValueException(String msg, Location location)
    {
        super(msg);
        setLocation(location);
    }

    public InvalidLexicalValueException(String msg, Throwable cause, Location location)
    {
        super(msg, cause);
        setLocation(location);
    }

    public InvalidLexicalValueException(Throwable cause, Location location)
    {
        super(cause);
        setLocation(location);
    }

    public Location getLocation()
    {
        return _location;
    }

    public void setLocation(Location location)
    {
        this._location = location;
    }
}
