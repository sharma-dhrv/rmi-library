all: interface client server

interface: compute/Compute.java compute/Task.java
	javac compute/Compute.java compute/Task.java; jar cvf compute.jar compute/*.class

client: compute.jar engine/ComputeEngine.java
	javac -cp compute.jar engine/ComputeEngine.java

server: compute.jar client/ComputePi.java client/PiTask.java
	javac -cp compute.jar client/ComputePi.java client/PiTask.java

clean:
	rm compute/*.class engine/*.class client/*.class compute.jar
