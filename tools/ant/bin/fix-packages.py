#! /usr/bin/env python
#
# Mass string translation for java/xml files (change Wanted re for other files)
#
# Author: Peter Donald <donaldp@apache.org>
#
# Adapted from fixid.py in python distribution under Tools
#

import sys
import re
import string
import os
from stat import *
import getopt

err = sys.stderr.write
dbg = err
out = sys.stdout.write

def usage():
	progname = sys.argv[0]
	err('Usage: ' + progname + ' [-s file] ... file-or-directory ...\n')
	err('\n')
	err('-s substfile : add a file of substitutions\n')
	err('\n')
	err('Each non-empty non-comment line in a substitution file must\n')
	err('contain exactly two words separated by a | character.\n')

def main():
	try:
		opts, args = getopt.getopt(sys.argv[1:], 'crs:')
	except getopt.error, msg:
		err('Options error: ' + str(msg) + '\n')
		usage()
		sys.exit(2)
	bad = 0
	if not args: # No arguments
		usage()
		sys.exit(2)
	for opt, arg in opts:
		if opt == '-s':
			addsubst(arg)
	initre()
	for arg in args:
		if os.path.isdir(arg):
			if recursedown(arg): bad = 1
		elif os.path.islink(arg):
			err(arg + ': will not process symbolic links\n')
			bad = 1
		else:
			if fix(arg): bad = 1
	sys.exit(bad)

# Change this regular expression to select a different set of files
Wanted = '^[a-zA-Z0-9_\-]+\.(java|xml|xinfo)$'
def wanted(name):
	return None <> re.match(Wanted, name)

def recursedown(dirname):
	dbg('recursedown(' + `dirname` + ')\n')
	bad = 0
	try:
		names = os.listdir(dirname)
	except os.error, msg:
		err(dirname + ': cannot list directory: ' + str(msg) + '\n')
		return 1
	names.sort()
	subdirs = []
	for name in names:
		if name in (os.curdir, os.pardir): continue
		fullname = os.path.join(dirname, name)
		if os.path.islink(fullname): pass
		elif os.path.isdir(fullname):
			subdirs.append(fullname)
		elif wanted(name):
			if fix(fullname): bad = 1
	for fullname in subdirs:
		if recursedown(fullname): bad = 1
	return bad

def fix(filename):
	dbg('fix(' + `filename` + ')\n')
    
    # File replacement mode
	try:
		f = open(filename, 'r')
	except IOError, msg:
		err(filename + ': cannot open: ' + str(msg) + '\n')
		return 1

	head, tail = os.path.split(filename)
	tempname = os.path.join(head, '@' + tail)

	# If we find a match, we rewind the file and start over but
	# now copy everything to a temp file.
	lineno = 0
	g = None
	while 1:
		line = f.readline()
		if not line: break
		lineno = lineno + 1
		newline = fixline(line)
		if newline != line:
			if g is None:
				try:
					g = open(tempname, 'w')
				except IOError, msg:
					f.close()
					err(tempname+': cannot create: '+
					    str(msg)+'\n')
					return 1
				f.seek(0)
				lineno = 0
				out(filename + ':\n')
				continue # restart from the beginning
			out(`lineno` + '\n')
			out('< ' + line)
			out('> ' + newline)
		if g is not None:
			g.write(newline)

	# End of file
	f.close()
	if not g: return 0 # No changes

	# Finishing touch -- move files

	# First copy the file's mode to the temp file
	try:
		statbuf = os.stat(filename)
		os.chmod(tempname, statbuf[ST_MODE] & 07777)
	except os.error, msg:
		err(tempname + ': warning: chmod failed (' + str(msg) + ')\n')

	# Then make a backup of the original file as filename~
	try:
		os.rename(filename, filename + '~')
	except os.error, msg:
		err(filename + ': warning: backup failed (' + str(msg) + ')\n')

	# Now move the temp file to the original file
	try:
		os.rename(tempname, filename)
	except os.error, msg:
		err(filename + ': rename failed (' + str(msg) + ')\n')
		return 1

	# Return succes
	return 0

def initre():
	global SearcherExpr

	keys = Dict.keys()
	keyCount = len(keys)
	i = 0
	reStr = ""
	while i < len(keys):
		if i != 0: reStr = reStr + '|'
		reStr = reStr + keys[i]
		i = i + 1
	SearcherExpr = re.compile( reStr )

def fixline(line):
	global Program
##	print '-->', `line`
	i = 0

	while i < len(line):
		match = SearcherExpr.search(line, i)
		if match == None: break
		found = match.group(0)
		i = match.start()
		n = len(found)
		if Dict.has_key(found):
			subst = Dict[found]

			line = line[:i] + subst + line[i+n:]
			n = len(subst)
		i = i + n
	return line

Dict = {}
NotInComment = {}

def addsubst(substfile):
	try:
		fp = open(substfile, 'r')
	except IOError, msg:
		err(substfile + ': cannot read substfile: ' + str(msg) + '\n')
		sys.exit(1)
	lineno = 0
    
	while 1:
		line = fp.readline()
		if not line: break
		lineno = lineno + 1
		try:
			i = string.index(line, '#')
		except string.index_error:
			i = -1		# Happens to delete trailing \n
		words = string.split(line[:i],'|')
		if not words: continue
		if len(words) <> 2:
			err(substfile + ':' + `lineno` + ': warning: bad line: ' + line)
			continue
		[key, value] = words

##		print 'Will replace ', key, ' with ', value

		Dict[key] = value
        
	fp.close()

main()
