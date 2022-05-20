# Distributed System Simulation 

This was a semester long project in our operating systems course. We were required to write seperate applications that represent certain components of a distributed system: a client, worker, and conductor. Clients are instantiated with tasks, and workers execute them. Both tasks and workers can be of types of A and B. Some workers are best suited to work on tasks of type A, and others of type B. Workers and Clients can connect to the central conductor which then facilitates all communications between connected components.

## How to Simulate at Home
To run the simulation, clone the repository and compile each class. Once done, each component of the system required the following arguments:
- Conductor: a port number
- Worker: the Conductor's port number, the Conductor's IP (localhost), and the type of task this worker is suited to execute (A or B)
- Client: the Conductor's port number, the Conductor's IP (localhost), and the amount of tasks the client should instantiate and send out for completion.

### By Avi Reich and Michael Tanami
