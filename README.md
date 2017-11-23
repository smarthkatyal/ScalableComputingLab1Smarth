ScalableComputing Assignment 1 (Chat Server)
===================


Hey! 
This repository contains the server code for the chat server assignment.  This readme contains information about **how to correctly configure, compile and run the program.** The code was written using **Java 1.8**


>#### <i class="icon-upload"></i> Submission details:
Name: Smarth Katyal
TCD Student ID: 17306092
Email: katyals@tcd.ie

Dependencies
-------------
>- **Java 1.8** (Performance with Java version 1.7 is untested)
>- **dos2unix** (May be required as sometimes shell scripts give an error)



Configuration
-------------

>-  After unpacking all files, navigate to **ChatServer/src/conf/** and run **dos2unix** command on **compile.sh** & **start.sh** files.
>- Open the file **start.sh** using any editor like vim and specify the IP address of the server. You can obtain the IP Address by using the command **ifconfig** in the command line. Use this IP address in the **start.sh** script, in line *THIS_IP=< IP_ADDR_OF_SERVER>*.
>- Save and close the file.


Compiling
-------------
>-  navigate to **ChatServer/src/conf/** and run the below command:
	**sh compile.sh**

Running
-------------
>- navigate to **ChatServer/src/conf/** and run the below command:
	**sh start.sh**


Error Codes
-------------
ErrorCode| ErrorDescription
-------- | ---
1| Input Message not valid
2| Chat Room Does not exist
3| Error Processing Chat message
4| Error Processing Leave message
5| Invalid Input

