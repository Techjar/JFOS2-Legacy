@echo off
cd dist
java -Xmx1024M -classpath JFOS2.jar com.techjar.jfos2.server.Server
pause