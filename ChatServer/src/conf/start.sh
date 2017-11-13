#!/bin/bash
#This script will run the java server for multi threaded chat server
echo Started Executing
THIS_IP=10.62.0.59
PORT=$1
cd "../../src/"
java com.scalablecomputing.server.MultiThreadChatServerSync $PORT $THIS_IP

