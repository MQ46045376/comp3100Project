This is my comp3100 project stage 1 and 2.

Stage 1:
To run ds-sim server:
./ds-server -c ../../configs/sample-configs/ds-sample-config01.xml -v brief -n

To compile java file to class file:
javac Client.java

To run the S1Demo.tar tests:
-untar by: tar -xvf S1Demo.tar
-make new folder in home
-add in S1DemoConfigs, demoS1.sh (from home) Client.java (from cloned repo), ds-client and ds-server (from ds-sim)
-Compile java file to class file: javac Client.java
-./demoS1.sh -n Client.class

Terminate the process any time by Ctrl + C

Stage 2: 
chmod +x stage2-test-86
copy ds-server and Client.class into stage2 test directory
compile with "javac Client"
run test with:
./stage2-test-x86 "java Client" -o tt -n

