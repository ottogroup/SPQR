#!/bin/bash

BASEDIR=$(dirname $0)
PIDFILE=$BASEDIR/pidfile

if [ -z "$1" ]; then
	# write error message
	echo "usage: spqr-node.sh <configuration file>"
	exit 1
fi

CLASSPATH=.:$BASEDIR/../lib/*

java -d64 -server -XX:MaxPermSize=512M -XX:MaxGCPauseMillis=500 -XX:+UseG1GC -Xms1G -cp $CLASSPATH com.ottogroup.bi.spqr.node.server.SPQRNodeServer server $1

