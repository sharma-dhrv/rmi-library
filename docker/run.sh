docker-machine create --driver virtualbox vbk
eval $(docker-machine env vbk)

echo -e "\nCreate the data volume container\n"
docker build -t db -f volumeDockerfile .

echo -e "\nRun the db container\n"
docker run -itd -v /data --name db_container db
volname=$(docker inspect -f '{{ (index .Mounts 0).Name }}' db_container)

echo -e "\nRun the server\n"
docker build -t server -f serverDockerfile .
docker run -itd -P --volumes-from db_container --name server --net=bridge server python server.py /data/strings.txt 2000
ip=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' server)
echo -e $ip

echo -e "\nRun the client\n"
docker build -t client -f clientDockerfile .
docker run -itd -P --volumes-from db_container --name client --net=bridge --link server client python client.py /data/strings.txt $ip 2000

echo -e "\nRunning the client and server\n"
sleep 40

echo -e "\nCollecting logs\n"
docker logs --tail=100 server >server.log
docker logs --tail=100 client >client.log

echo -e "\nVerifying client\n"
python verify.py client.log

echo -e "\nStopping client container\n"
docker stop client

echo -e "\nStopping server container\n"
docker stop server

echo -e "\nStopping data volume container\n"
docker stop db_container

echo -e "\nRemoving client container\n"
docker rm client

echo -e "\nRemoving server container\n"
docker rm server

echo -e "\nRemoving data volume container\n"
docker rm db_container

echo -e "\nRemoving the data volume\n"
docker volume rm $volname

echo -e "\nContainers and volumes are cleaned-up"
docker ps -a

echo 
docker volume ls

echo "\nRemoving the docker virtual machine\n"
docker-machine rm -f vbk

echo -e "\nEND!!\n"
