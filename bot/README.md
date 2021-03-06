# LEGO Draw Bot

A LEGO EV3 Mindstorms with two motors: either winding strings, or an arm, that drag a dry erase marker around a
whiteboard.

## Architecture

* EV3 is running [LeJOS](http://www.lejos.org/).
* Client program is in Kotlin (jar deployed to EV3 via Maven)
* Client downloads a script JSON from a fixed web address
* As the script is downloading, the user "calibrates" the drawing by marking the upper-left and upper-right using EV3
  buttons
* Script executes, moving to the specified normalized X-Y coordinates in order.

To generate new scripts from images, see "scriptgen" module which is run on a PC.  
Paste the output into the config.json file and upload to web server.

## One Time Setup

### Dev Environment

Clone and load this project in IntelliJ to get Kotlin/Maven support

### Setup the Bot OS

To recreate the runtime:

1. Download from http://www.oracle.com/technetwork/java/embedded/downloads/java-embedded-java-se-download-359230.html
2. NOTE: The "-g" is from [stack overflow](https://stackoverflow.com/questions/23275519/jdwp-in-embedded-jre-in-java-8)

```bash
gunzip ejdk-8-fcs-b132-linux-arm-sflt-03_mar_2014.tar.gz
tar xvf ejdk-8-fcs-b132-linux-arm-sflt-03_mar_2014.tar
cd ejdk1.8.0/bin
export JAVA_HOME=/usr
./jrecreate.sh -g --dest ../../ejre-8u1-linux-arm-15_may_2015 --profile compact2 --vm client
cd ../..
tar cvf ejre-8u1-linux-arm-15_may_2015.tar ejre-8u1-linux-arm-15_may_2015
gzip ejre-8u1-linux-arm-15_may_2015.tar
```

### Setup a web host for your config.json file (I like firebase)

```bash
gcloud components update && gcloud components install beta npm install -g firebase-tools
```

## Start Testing Code

LeJOS clients are fun!  Fix the brick's IP address in pom.xml, and run maven:deploy

```bash
mvn deploy
mvn antrun:run
```

To SSH into the robot:

```bash
ssh -oKexAlgorithms=+diffie-hellman-group1-sha1 -c 3des-cbc root@192.168.86.45
# location: root@EV3:/home/lejos/programs# 
jrun -cp "bot-1.0-SNAPSHOT.jar:/home/root/lejos/lib/*" info.benjaminhill.sketchy.MainKt
```

TODO: https://github.com/salamanders/talkinghead/blob/master/pom.xml
https://stackoverflow.com/questions/58940864/how-do-i-only-build-the-jar-without-dependencies-in-maven

Copying files

```bash
cd bot
mvn dependency:copy-dependencies
# empty password
scp -oKexAlgorithms=+diffie-hellman-group1-sha1 -c 3des-cbc .\target\dependency\* root@192.168.86.45:/home/root/lejos/lib/
scp -oKexAlgorithms=+diffie-hellman-group1-sha1 -c 3des-cbc .\target\bot-1.0-SNAPSHOT.jar root@192.168.86.45:/home/lejos/programs/
```

To view console Run ev3console or Eclipse: ev3control
http://www.lejos.org/ev3/docs/

## TODO

- [ ] Script selection
- [ ] Fix the "squashed head" issue
- [ ] Move to https://github.com/ev3dev-lang-java/ev3dev-lang-java
- [ ] (Later) MQTT bidirectional communication with Firestore

* https://cloud.google.com/community/tutorials/cloud-iot-firestore-config
* https://cloud.google.com/functions/docs/calling/cloud-firestore#deploying_your_function
* https://cloud.google.com/iot/docs/how-tos/commands#iot-core-send-command-nodejs
* /devices/{id=ev3}, /telemetry/{id=ev3}
* https://console.firebase.google.com/project/whiteboardbot/database/firestore/data~2Fwbb~2Fboard01
* gcloud functions deploy deviceToDb --runtime nodejs8 --trigger-resource target --trigger-event
  google.pubsub.topic.publish

## Thanks To

* https://www.marginallyclever.com/2012/02/drawbot-overview/
* http://www.patriciogonzalezvivo.com/2014/vPlotter/
* https://github.com/patriciogonzalezvivo/vPlotter
* http://fabacademy.org/archives/2013/labs/amsterdam/class_15_machinedesign/math.html
* https://gist.github.com/pfdevmuller/473d03765906f5e25791