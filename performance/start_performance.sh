#!/bin/bash
java -cp ./conf/:./lib/* util.geode.performance.Performance -Dlogfile-name= $*
