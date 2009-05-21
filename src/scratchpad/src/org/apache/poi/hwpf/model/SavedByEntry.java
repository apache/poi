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

package org.apache.poi.hwpf.model;


/**
 * A single entry in the {@link SavedByTable}.
 *
 * @author Daniel Noll
 */
public final class SavedByEntry
{
  private String userName;
  private String saveLocation;

  public SavedByEntry(String userName, String saveLocation)
  {
    this.userName = userName;
    this.saveLocation = saveLocation;
  }

  public String getUserName()
  {
    return userName;
  }

  public String getSaveLocation()
  {
    return saveLocation;
  }

  /**
   * Compares this object with another, for equality.
   *
   * @param other the object to compare to this one.
   * @return <code>true</code> iff the other object is equal to this one.
   */
  public boolean equals(Object other)
  {
    if (other == this) return true;
    if (!(other instanceof SavedByEntry)) return false;
    SavedByEntry that = (SavedByEntry) other;
    return that.userName.equals(userName) &&
           that.saveLocation.equals(saveLocation);
  }

  /**
   * Generates a hash code for consistency with {@link #equals(Object)}.
   *
   * @return the hash code.
   */
  public int hashCode()
  {
    int hash = 29;
    hash = hash * 13 + userName.hashCode();
    hash = hash * 13 + saveLocation.hashCode();
    return hash;
  }

  /**
   * Returns a string for display.
   *
   * @return the string.
   */
  public String toString()
  {
    return "SavedByEntry[userName=" + getUserName() +
                       ",saveLocation=" + getSaveLocation() + "]";
  }
}
