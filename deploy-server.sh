clear && rm *.class;
rm -rf server/*.class;
javac trustcloud.java Server.java;
mkdir server;
cp *.class server;
cp server_key.p12 server;
cd server;
java Server
