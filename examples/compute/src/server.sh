#!/bin/bash

echo "Starting server..."
java -cp /home/dhruv/workspace/java-rmi/src/:/home/dhruv/workspace/java-rmi/src/compute.jar \
	-Djava.rmi.server.codebase=file:/home/dhruv/workspace/java-rmi/src/compute.jar \
	-Djava.rmi.server.hostname=127.0.0.1 \
	-Djava.security.policy=policy \
	engine.ComputeEngine
