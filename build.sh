##########################################################################
# Copyright 2015 Otto (GmbH & Co KG)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##########################################################################

#!/bin/sh

if [ -z "$1" ]; then
	# write error message
        echo "spqr build and deployment script"
	echo "usage: build.sh <spqr git folder> <spqr destination folder>"
	exit 1
fi

if [ -z "$2" ]; then
	# write error message
        echo "spqr build and deployment script"
	echo "usage: build.sh <spqr git folder> <spqr destination folder>"
	exit 1
fi

SRC_FOLDER=$1
DEST_FOLDER=$2

echo "source folder: $SRC_FOLDER, destination folder: $DEST_FOLDER"

cd $SRC_FOLDER
mvn clean; mvn install;

# create destination folders
mkdir -p $DEST_FOLDER/spqr-node/lib
mkdir -p $DEST_FOLDER/spqr-node/bin
mkdir -p $DEST_FOLDER/spqr-node/etc
mkdir -p $DEST_FOLDER/spqr-node/repo
mkdir -p $DEST_FOLDER/spqr-node/log
mkdir -p $DEST_FOLDER/spqr-node/queues

mkdir -p $DEST_FOLDER/spqr-resman/lib
mkdir -p $DEST_FOLDER/spqr-resman/bin
mkdir -p $DEST_FOLDER/spqr-resman/etc
mkdir -p $DEST_FOLDER/spqr-resman/repo
mkdir -p $DEST_FOLDER/spqr-resman/log

mkdir -p $DEST_FOLDER/spqr-websocket-server/lib
mkdir -p $DEST_FOLDER/spqr-websocket-server/bin
mkdir -p $DEST_FOLDER/spqr-websocket-server/etc/
mkdir -p $DEST_FOLDER/spqr-websocket-server/log

#######################################################################
# spqr-node
# copy configuration from processing node project to destination folder
cp $SRC_FOLDER/spqr-node/src/main/config/* $DEST_FOLDER/spqr-node/etc/

# copy libraries from processing node project to destination folder (remove log4j-over-slf4j jar first)
cp $SRC_FOLDER/spqr-node/target/lib/* $DEST_FOLDER/spqr-node/lib/

# copy scripts from processing node project to destination folder
cp $SRC_FOLDER/spqr-node/src/main/scripts/* $DEST_FOLDER/spqr-node/bin/
#######################################################################

#######################################################################
# spqr-resman
# copy configuration from resource manager project to destination folder
cp $SRC_FOLDER/spqr-resman/src/main/config/* $DEST_FOLDER/spqr-resman/etc/

# copy libraries from resource manager project to destination folder (remove log4j-over-slf4j jar first)
cp $SRC_FOLDER/spqr-resman/target/lib/* $DEST_FOLDER/spqr-resman/lib/

# copy scripts from resource manager project to destination folder
cp $SRC_FOLDER/spqr-resman/src/main/scripts/* $DEST_FOLDER/spqr-resman/bin/
#######################################################################

#######################################################################
# spqr-json operator
mkdir -p $DEST_FOLDER/spqr-node/repo/spqr-json
mkdir -p $DEST_FOLDER/spqr-resman/repo/spqr-json
cp $SRC_FOLDER/spqr-operators/spqr-json/target/lib/* $DEST_FOLDER/spqr-node/repo/spqr-json/
cp $SRC_FOLDER/spqr-operators/spqr-json/target/lib/* $DEST_FOLDER/spqr-resman/repo/spqr-json/

# spqr-kafka operator
mkdir -p $DEST_FOLDER/spqr-node/repo/spqr-kafka
mkdir -p $DEST_FOLDER/spqr-resman/repo/spqr-kafka
cp $SRC_FOLDER/spqr-operators/spqr-kafka/target/lib/* $DEST_FOLDER/spqr-node/repo/spqr-kafka/
cp $SRC_FOLDER/spqr-operators/spqr-kafka/target/lib/* $DEST_FOLDER/spqr-resman/repo/spqr-kafka/

# spqr-twitter operator
mkdir -p $DEST_FOLDER/spqr-node/repo/spqr-twitter
mkdir -p $DEST_FOLDER/spqr-resman/repo/spqr-twitter
cp $SRC_FOLDER/spqr-operators/spqr-twitter/target/lib/* $DEST_FOLDER/spqr-node/repo/spqr-twitter/
cp $SRC_FOLDER/spqr-operators/spqr-twitter/target/lib/* $DEST_FOLDER/spqr-resman/repo/spqr-twitter/

# spqr-webtrends operator
mkdir -p $DEST_FOLDER/spqr-node/repo/spqr-webtrends
mkdir -p $DEST_FOLDER/spqr-resman/repo/spqr-webtrends
cp $SRC_FOLDER/spqr-operators/spqr-webtrends/target/lib/* $DEST_FOLDER/spqr-node/repo/spqr-webtrends/
cp $SRC_FOLDER/spqr-operators/spqr-webtrends/target/lib/* $DEST_FOLDER/spqr-resman/repo/spqr-webtrends/

# spqr-esper operator
mkdir -p $DEST_FOLDER/spqr-node/repo/spqr-esper
mkdir -p $DEST_FOLDER/spqr-resman/repo/spqr-esper
cp $SRC_FOLDER/spqr-operators/spqr-esper/target/lib/* $DEST_FOLDER/spqr-node/repo/spqr-esper/
cp $SRC_FOLDER/spqr-operators/spqr-esper/target/lib/* $DEST_FOLDER/spqr-resman/repo/spqr-esper/

#######################################################################

#######################################################################
# spqr-websocket-server
cp $SRC_FOLDER/spqr-websocket-server/target/lib/* $DEST_FOLDER/spqr-websocket-server/lib/
cp $SRC_FOLDER/spqr-websocket-server/src/main/scripts/* $DEST_FOLDER/spqr-websocket-server/bin
cp $SRC_FOLDER/spqr-websocket-server/src/main/config/* $DEST_FOLDER/spqr-websocket-server/etc

#######################################################################

echo "Build and Deployment finished"
