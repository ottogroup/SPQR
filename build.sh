#!/bin/sh

cd spqr-parent
mvn clean; mvn install;

cd ..
cd spqr-build
mvn clean; mvn install;
cd ..

mkdir -p /opt/transport/spqr/spqr-node/lib
mkdir -p /opt/transport/spqr/spqr-node/bin
mkdir -p /opt/transport/spqr/spqr-node/etc
mkdir -p /opt/transport/spqr/spqr-node/repo
mkdir -p /opt/transport/spqr/spqr-node/log
mkdir -p /opt/transport/spqr/spqr-node/queues

cd spqr-node/src/main/config
cp * /opt/transport/spqr/spqr-node/etc/
cd ../../..

cd target/lib
rm log4j-over-slf4j-*.jar
cp * /opt/transport/spqr/spqr-node/lib
cd ../..

cd src/main/scripts
cp * /opt/transport/spqr/spqr-node/bin/
