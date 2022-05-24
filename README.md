# Distributed System Simulation 

This was a semester long project in our operating systems course. We were required to write separate applications that represent certain components of a distributed system: a client, worker, and conductor. Clients are instantiated with tasks which are executed by workers. Both tasks and workers can be of type A or B. Workers are best suited to execute tasks of the same type. Workers and clients can connect to the central conductor which facilitates all communication between connected components and chooses the best worker to execute each task.

## How to Simulate at Home
To run the simulation, clone the repository and compile each class. Once compiled, each component of the system requires the following arguments:
- Conductor: a port number
- Worker: the Conductor's IP (localhost), the Conductor's port number, and the type of task this worker is best suited to execute (A or B)
- Client: the Conductor's IP (localhost), the Conductor's port number, and the amount of tasks the client should instantiate and send out for completion

### By Avi Reich and Michael Tanami
