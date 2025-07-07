#!/usr/bin/env python3
"""
   ====================================================================
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
   ====================================================================
"""

"""This is a really crude throwaway script to get data out of Bugzilla and into status.xml
It'd be far better to have Forrest look this information up whenever the site is rebuilt.
Hopefully this is a one time effort
If a closed bug's component is changed in Bugzilla, this script could be used to keep the changelog in sync.

requires Python 3.1+ 
(Python 2.x doesn't do Unicode in CSVs nicely)
"""

import csv, io
import sys
import requests

def get_fixesbug_attr(line):
    pieces = [x.strip() for x in line.split('"')]
    bugs = pieces[pieces.index('fixes-bug=') + 1]
    return bugs

def get_bugzilla_bug_to_component():
    print("Fetching details of POI bugs, please wait...")
    bugzilla_bug_to_component = {}
    r = requests.get('https://bz.apache.org/bugzilla/buglist.cgi?bug_status=__all__&limit=0&no_redirect=1&product=POI&query_format=advanced&ctype=csv&human=1')
    with io.StringIO(r.text) as f:
        csvreader = csv.DictReader(f)
        for row in csvreader:
            bugzilla_bug_to_component[row['Bug ID']] = row['Component']
    return bugzilla_bug_to_component


def unique(seq):
    seen = set()
    for x in seq:
        if x not in seen:
            seen.add(x)
            yield x

def add_module_frombugzilla_attr(line):
    """Add module_frombugzilla="XSSF" to <action ...> tag

    line is a string, containing the <action> opening tag
    """
    global bugzilla_bug_to_component
    assert 'module=' not in line, \
       "Invalid action line, should not already contain module: %s" % line

    bugs = [x.strip() for x in get_fixesbug_attr(line).split(',')]
    modules = filter(bool, [bugzilla_bug_to_component.get(bug) for bug in bugs])
    module_frombugzilla = ','.join(unique(modules))
    line_with_module_frombugzilla = line.replace('>', ' module="{}">'.format(module_frombugzilla), 1)
    return line_with_module_frombugzilla

def add_module_attribute(inputfile, outputfile):
    print("Generating %s from %s and Bugzilla details"%(outputfile,inputfile))
    with open(inputfile, 'r') as infile, open(outputfile, 'w') as outfile:
        for line in infile:
            if '<action ' in line and ' fixes-bug=' in line and ' module=' not in line:
                # append "module="XXXX" at end of <action> tag
                outfile.write(add_module_frombugzilla_attr(line))
            else:
                outfile.write(line)


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print('Usage: python changelog.py inputfile outputfile')
    else:
        bugzilla_bug_to_component = get_bugzilla_bug_to_component()
        add_module_attribute(sys.argv[1], sys.argv[2])
        print("Generation complete!")
