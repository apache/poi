poi SharedStringsTable MapDB Implementation
== 

Problem : SXSSFWorkbook defaults to using inline strings instead of a shared strings table.This is very efficient, since no document content needs to be kept in memory, but is also known to produce documents that are incompatible with some clients and work book size will be large.

SXSSFWorkbook with shared strings enabled all unique strings in the document has to be kept in memory but it use a lot more resources than with shared strings disabled.

Solution : To reduce memory footprint of POI’s shared strings table implementation we implemented shared strings table usin MapDB.

Overall, the MapDB solution is slower than pure POI, but takes much lesser amount of memory.

It flows data to disk as per availability of memory (Reference).

We couldn't so far find a clean way to achieve this without patching POI code (there is no clean hook available to use an extended class for SharedStringsTable).

Mirror of Apache POI
