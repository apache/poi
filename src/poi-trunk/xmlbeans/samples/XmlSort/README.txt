Sample: XmlSort
Category: cursor sample
Author: Radu Preotiuc-Pietro
Last Updated: Feb. 3, 2009

Versions:
    xmlbeans-v2

-----------------------------------------------------------------------------

This is another practical sample on how to use XmlCursor. It sorts all the
children elements of a given parent in (reverse) alphabetical order. It uses
insertion sort to minimize the number of swaps (and because it is stable) and
XmlCursor.moveXml() to perform the swaps.

The algorithm works by conceptually separating the list of children into a
sorted list and an unsorted list. Each list is represented by an XmlCursor
positioned at the head of the list. At the beginning, the sorted list
contains the first child and the unsorted list contains the rest of the
children (if any). Then, each of the elements in the unsorted list is
inserted in the already-sorted list in its appropriate place using the
moveXml() method (the text following each element is then moved along with
the element).

To build this sample, call 'ant build' and to run it against the provided
sample XML file, call 'ant run'.

To run the sample from the command line using 'java', provide as the first
parameter the name of the XML file to process and as the second argument
(optional), an XPath pointing to the element whose children are to be sorted
(by default, the children of the root element are sorted). The XPath must
evaluate to an element and one element only, otherwise an error will be
reported. The console output will mirror the input file, with the children
of the given element sorted in ascending alphabetical order.

This sample can also be used as a library routine by calling its main
static method: XmlSort.sort(XmlObject, Comparator). The XmlObject whose
children are to be sorted is passed directly as a parameter and the
Comparator defines the order in which the elements are to be sorted. The
parameters to the compare() method are two XmlCursors pointing to the
two elements to be compared. The inner class XmlSort.QNameComparator
implements comparison based on the QName of the elements (ascending or
descending) but custom comparison methods (based for instance on the
value of some attribute etc) can also be passed in.

To try out this sample:

1. Set XMLBEANS_HOME in your environment
2. Ant must be on your PATH
3. To compile the sample source, run "ant build"
4. To execute the sample, run "ant run"
