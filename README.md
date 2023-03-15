### Project for FAF.PTR16.1 Spring 2023 Course

### Dubina Valeria - FAF-203

---
## Stream Processing with Actors

The goal is to build the Project as a functional stream processing system.

Each week requires 2 diagrams: a Message Flow Diagram and a Supervision Tree Diagram. The Message
Flow Diagram describes the message exchange between actors of your system whereas the
Supervision Tree Diagram analyzes the monitor structures of your application. The diagrams are stored in [here](./diagrams).

To run the project pull the docker image from [Alex Burlacu DockerHub](https://hub.docker.com/r/alexburlacu/rtp-server/tags)
with the tag **faf18x** as follows:

```bash
docker pull alexburlacu/rtp-server:faf18x
```

Run the docker image on port 50 and start the simulation.