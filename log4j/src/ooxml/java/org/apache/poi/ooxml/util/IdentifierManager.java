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
package org.apache.poi.ooxml.util;

import java.util.LinkedList;
import java.util.ListIterator;

public class IdentifierManager {

    public static final long MAX_ID = Long.MAX_VALUE - 1;
    public static final long MIN_ID = 0L;

    private final long upperbound;
    private final long lowerbound;

    /**
	 * List of segments of available identifiers
	 */
    private LinkedList<Segment> segments;

    /**
     * @param lowerbound the lower limit of the id-range to manage. Must be greater than or equal to {@link #MIN_ID}.
     * @param upperbound the upper limit of the id-range to manage. Must be less then or equal {@link #MAX_ID}.
     */
    public IdentifierManager(long lowerbound, long upperbound) {
        if (lowerbound > upperbound) {
            throw new IllegalArgumentException("lowerbound must not be greater than upperbound, had " + lowerbound + " and " + upperbound);
        }
        else if (lowerbound < MIN_ID) {
            String message = "lowerbound must be greater than or equal to " + Long.toString(MIN_ID);
            throw new IllegalArgumentException(message);
        }
        else if (upperbound > MAX_ID) {
            /*
             * while MAX_ID is Long.MAX_VALUE, this check is pointless. But if
             * someone subclasses / tweaks the limits, this check is fine.
             */
            throw new IllegalArgumentException("upperbound must be less than or equal to " + Long.toString(MAX_ID) + " but had " + upperbound);
        }
        this.lowerbound = lowerbound;
        this.upperbound = upperbound;
        this.segments = new LinkedList<>();
        segments.add(new Segment(lowerbound, upperbound));
    }

    public long reserve(long id) {
        if (id < lowerbound || id > upperbound) {
            throw new IllegalArgumentException("Value for parameter 'id' was out of bounds, had " + id + ", but should be within [" + lowerbound + ":" + upperbound + "]");
        }
        verifyIdentifiersLeft();

        if (id == upperbound) {
            Segment lastSegment = segments.getLast();
            if (lastSegment.end == upperbound) {
                lastSegment.end = upperbound - 1;
                if (lastSegment.start > lastSegment.end) {
                    segments.removeLast();
                }
                return id;
            }
            return reserveNew();
        }

        if (id == lowerbound) {
            Segment firstSegment = segments.getFirst();
            if (firstSegment.start == lowerbound) {
                firstSegment.start = lowerbound + 1;
                if (firstSegment.end < firstSegment.start) {
                    segments.removeFirst();
                }
                return id;
            }
            return reserveNew();
        }

        ListIterator<Segment> iter = segments.listIterator();
        while (iter.hasNext()) {
            Segment segment = iter.next();
            if (segment.end < id) {
                continue;
            }
            else if (segment.start > id) {
                break;
            }
            else if (segment.start == id) {
                segment.start = id + 1;
                if (segment.end < segment.start) {
                    iter.remove();
                }
                return id;
            }
            else if (segment.end == id) {
                segment.end = id - 1;
                if (segment.start > segment.end) {
                    iter.remove();
                }
                return id;
            }
            else {
                iter.add(new Segment(id + 1, segment.end));
                segment.end = id - 1;
                return id;
            }
        }
        return reserveNew();
    }

    /**
     * @return a new identifier.
     * @throws IllegalStateException if no more identifiers are available, then an Exception is raised.
     */
    public long reserveNew() {
        verifyIdentifiersLeft();
        Segment segment = segments.getFirst();
        long result = segment.start;
        segment.start += 1;
        if (segment.start > segment.end) {
            segments.removeFirst();
        }
        return result;
    }

    /**
     * @param id
     * the identifier to release. Must be greater than or equal to
     * {@link #lowerbound} and must be less than or equal to {@link #upperbound}
     * @return true, if the identifier was reserved and has been successfully
     * released, false, if the identifier was not reserved.
     */
    public boolean release(long id) {
        if (id < lowerbound || id > upperbound) {
            throw new IllegalArgumentException("Value for parameter 'id' was out of bounds, had " + id + ", but should be within [" + lowerbound + ":" + upperbound + "]");
        }

        if (id == upperbound) {
            Segment lastSegment = segments.getLast();
            if (lastSegment.end == upperbound - 1) {
                lastSegment.end = upperbound;
                return true;
            } else if (lastSegment.end == upperbound) {
                return false;
            } else {
                segments.add(new Segment(upperbound, upperbound));
                return true;
            }
        }

        if (id == lowerbound) {
            Segment firstSegment = segments.getFirst();
            if (firstSegment.start == lowerbound + 1) {
                firstSegment.start = lowerbound;
                return true;
            } else if (firstSegment.start == lowerbound) {
                return false;
            } else {
                segments.addFirst(new Segment(lowerbound, lowerbound));
                return true;
            }
        }

        long higher = id + 1;
        long lower = id - 1;
        ListIterator<Segment> iter = segments.listIterator();

        while (iter.hasNext()) {
            Segment segment = iter.next();
            if (segment.end < lower) {
                continue;
            }
            if (segment.start > higher) {
                iter.previous();
                iter.add(new Segment(id, id));
                return true;
            }
            if (segment.start == higher) {
                segment.start = id;
                return true;
            }
            else if (segment.end == lower) {
                segment.end = id;
                /* check if releasing this elements glues two segments into one */
                if (iter.hasNext()) {
                  Segment next = iter.next();
                    if (next.start == segment.end + 1) {
                        segment.end = next.end;
                        iter.remove();
                    }
                }
                return true;
            }
            else {
                /* id was not reserved, return false */
                break;
            }
        }
        return false;
    }

    public long getRemainingIdentifiers() {
        long result = 0;
        for (Segment segment : segments) {
            result = result - segment.start;
            result = result + segment.end + 1;
        }
        return result;
    }

    /**
	 *
	 */
    private void verifyIdentifiersLeft() {
        if (segments.isEmpty()) {
            throw new IllegalStateException("No identifiers left");
        }
    }

    private static class Segment {
        private long start;
        private long end;

        public Segment(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return "[" + start + "; " + end + "]";
        }
    }
}
