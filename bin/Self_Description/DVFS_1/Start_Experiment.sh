#! /bin/bash

DIRCLOUDSIMJAR=../../../cloudsim/target/cloudsim-3.1-SNAPSHOT.jar
classname=Dvfs_example_simple

if [ "$1" == c ]
then
javac -Xlint -cp $DIRCLOUDSIMJAR:. -Xlint  $classname.java

elif [ "$1" == l ]
then
Date=`date +%H_%M_%S-%Y%m%d`
OUTPUT=/usr/local/share/OUTPUT_CLOUDSIM/$Date
mkdir $OUTPUT

java -cp $DIRCLOUDSIMJAR:. $classname > $OUTPUT/$classname.output_

fi
