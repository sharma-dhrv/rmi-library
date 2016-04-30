#!/bin/bash

if [ $EUID -ne 0 ]; then
    echo "Need to be root. Try: sudo $0 $@"
    exit 1
fi

printf "\n\nCompiling RMI library JAR, PingServer and PingClient...\n"

make -f dockerMakefile



printf "\n\nBuilding PingServer image...\n"

sudo docker build -f ./pingserver-dockerfile -t "pingserver-image" .

printf "\n\nRunning PingServer container...\n"

sudo docker run -d --name "pingserver" "pingserver-image" java -cp pingpong.jar:. rmi.server.ServerDriver 3000

pingserver_ip=`sudo docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' pingserver`

printf "PingServer IP Address: ${server_ip}\n"


printf "\n\nBuilding PingClient image...\n"

sudo docker build -f ./pingclient-dockerfile -t "pingclient-image" .

printf "\n\nRunning PingClient container...\n"

sudo docker run --name "pingclient" "pingclient-image" java -cp pingpong.jar:. rmi.client.ClientDriver $pingserver_ip 3000



printf "\n\nVerifying PingClient output with expected output...\n"

sudo docker logs "pingclient" > .tmpfile.txt

printf "\nExpected Output:-\n"
cat expected-output.txt

if [ $(diff -q expected-output.txt .tmpfile.txt | wc -l) -eq 0 ]; then
    printf "\nPASS : Output verified to be correct!\n"
else
    printf "\nFAIL : Output does not match.\n"
fi

# wait for PingServer to shutdown
printf "\n\nWaiting for PingServer to finish..."
sleep 5

for instance in $(sudo docker ps -q); do
    printf "\n\nTerminating $instance...\n"
    sudo docker kill $instance
done

printf "\n\nPingServer Logs:-\n"
sudo docker logs pingserver


printf "\n\nCleaning up residual files, containers and images...\n"

rm .tmpfile.txt

make -f dockerMakefile clean

sudo docker rm pingserver pingclient
sudo docker rmi pingclient-image pingserver-image

printf "\nDone.\n"

exit 0
