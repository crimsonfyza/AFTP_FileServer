# Introduction  
This is a CLI operated file server. Once installed and run, 
with this server you will have a public location where other users can PUSH - GET - DELETE - UPDATE their files to, 
using 1 or more FileClients connecting to the FileServer using CLI commands.   

# Installation
To install this Application clone the following link: 
https://github.com/crimsonfyza/AFTP_FileServer.git

To setup the application for the "first use" read #How to use

# How to use 
Run FileServer, FileServer will use port: "25444", make sure this port is available.
If the application says "Server started." the application is running, as shown in the screenshot below. 
![](screenshots/startedApplication.png)   

If the application doesn't run, the port is currently in use. The application will than show the error "Port already in use."

The steps above are the only ones needed to get the application "running"
to connect users you need to use the other application. 
connect the users to the fileservers IP or if on the same computer localhost, if they connect succesfully the fileserver shows the following message for each user that is connected: 
![](screenshots/connectedUsers.png) 

# Contribute 
Other developers can give us feedback at filedevelopers@avans.nl