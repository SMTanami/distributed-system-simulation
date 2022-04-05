# Semester Project
Created: 03/15/2022 11:42 AM
Backlink: [[MOC - Operating Systems]]

---

### Task Class
A data Structure that contains:
	- an int for sim.task number
	- a boolean for completion status
	- a char for sim.client taskSender


### sim.master.Master Class
a java server with multiple server sockets. Slaves will connect two some sockets while clients will connect to others

Threadcount: 4

###### Data structures	
- Circular queue and implementation of Producer-Consumer pattern will allow for 'jobs' to be stored in the master and 'consumed' by the slaves
- Will have to be synchronized

Send:
- Clients: an int primitive that indicates which taskID has been complete, the sim.client will flip the completion status of that sim.task to `true` and increment its counter
- Slaves: the sim.task itself, Task A or Task B

Receive:
- Clients: Tasks, after the algo, will send those tasks to designated slaves
- Slaves: The completed sim.task


### Slave Class
a java sim.client with a single its own sim.client socket and server socket that its connected to

Threadcount: 1

Send:
- Tasks to master

Receive:
- Tasks from master


### Client Class
A java sim.client will connect directly to the master. This class can be written once and opened up multiple times.

Threadcount: 2

###### Data Structures
- Array - a single 'sim.task' array of a set size
- int - an int counter that indicates completion progress


---
## References
1. 


