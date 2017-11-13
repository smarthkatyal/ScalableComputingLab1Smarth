#!/bin/bash
#This script will compile the java server for multi threaded chat server
echo Started Executing
cd '../../src/com/scalablecomputing/server/'
pwd
javac -cp -sourcepath *.java
