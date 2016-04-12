#!/bin/bash
#
# $Id: run_igb.sh 4938 2010-01-06 20:21:41Z sgblanch $
#
# Simple bash launcher for IGB.  This launcher should be compatible with
# all UNIX-like systems with a bash interperter.
#
# Ways to Specify Java:
#  - Set JAVA_HOME to point to an installed JDK
#  - Set JRE_HOME to point to an installed JRE
#  - Set JAVACMD to point to a working java executable
#  - Have a java executable in your path
#
# Specifying options to the underlying VM.
#  - Specify on the command line using -J<vmarg>
#
# Specifying options to IGB
#  - Specify directly on command line

# Find out where we are installed
PREFIX=`readlink -f $0 2>/dev/null`

# Not all readlinks support -f.  Ideally, this should run recursively to
# determine its real path.  Currently it only will resolve the first
# link.
if [ $? -ne 0 ]; then
	PREFIX=`readlink $0`
	[ $? -ne 0  ] && PREFIX=$0
fi

PREFIX=`dirname $PREFIX`
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# File Locations
ICON="$PREFIX/igb/resources/com/affymetrix/igb/igb.png"

# Do our best to find java
test -z "$JAVACMD" -a -n "$JAVA_HOME" && JAVACMD=$JAVA_HOME/bin/java
test -z "$JAVACMD" -a -n "$JRE_HOME"  && JAVACMD=$JRE_HOME/bin/java
test -z "$JAVACMD" && JAVACMD=`which java` 2> /dev/null
test -z "$JAVACMD" && echo "$0: could not find java" >&2 && exit 127

# Sort VM arguments from program arguments
while (( "$#" )); do
	echo $1 | grep -q '^-J'
	if [ $? -eq 0 ]; then
		VMARGS[${#VMARGS[*]}]=`echo $1 | sed -e 's/^-J//'`
	else
		ARGS[${#ARGS[*]}]=$1
	fi
	shift
done

# Some Apple Specific settings. Should there be a way to override these?
if [[ `uname -s` == "Darwin" ]]; then
	VMARGS[${#VMARGS[*]}]="-Dapple.laf.useScreenMenuBar=true"
	VMARGS[${#VMARGS[*]}]="-Xdock:name=Integrated Genome Browser"
	VMARGS[${#VMARGS[*]}]="-Xdock:icon=$ICON"
	VMARGS[${#VMARGS[*]}]="-d64"
fi

# Launch IGB
IFS="
"
$JAVACMD ${VMARGS[*]} -jar $DIR/igb_exe.jar ${ARGS[*]}
