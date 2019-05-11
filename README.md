# SEC
Highly Dependable Systems Project - HDS Notary

## Requirements
You must have the following tools:

- GNU/Linux
- Java Development Kit 8 (JDK 8) 164+
- Maven 3.x.x 
- Middleware PT-CC [Optional]
- Smart Card reader [Optional] 


Also check if JAVA_HOME and M2_HOME are set properly.

## Enable Portuguese Citizen Card
### Install Maven Dependency
The library of the Portuguese Citizen Card is not in the maven repository, to install it in your local 
maven repository, run the following command in **/hds-notary**
```
mvn install:install-file -Dfile=server/src/main/resources/jar/pteidlibj.jar -DgroupId=pt.ulisboa.tecnico.sec -DartifactId=pteidlibj -Dversion=1.0 -Dpackaging=jar
```

#### GNU/Linux
To enable the library of the Portuguese Citizen Card, you need to set the path library, to do so, run the following command:

```
  export LD_LIBRARY_PATH=/usr/local/lib/:$LD_LIBRARY_PATH
```

## How to compile
To compile just run the command in the directory **/hds-notary/**:
```
    mvn clean install -DskipTests
```
**Note:** Is essential the presence of the flag _-DskipTests_, otherwise the tests will certainly fail
as the class loaders don't yet exist at this point.
## How to run
### Server
Tu run the server run the following command under **/hds-notary/client**:
``` 
mvn exec:java -Dexec.args="<arguments>"
```

The **\<arguments>** can be filled with the following arguments:
```
usage: HDS-Server [-help] [-noCC] -sid <id>
 -help                   Prints this message
 -noCC,--noCitizenCard   Disables signature using the Portuguese Citizen
                         Card
 -sid,--server-id <id>   The server's identifier.

```


### Client
Tu run the client run the following command under **/hds-notary/client**:
``` 
mvn exec:java -Dexec.args="<arguments>"
```

The **\<arguments>** can be filled with the following arguments:
```
usage: HDS-Client [-help] [-p <password>] [-u <username>]
 -help                      Prints this message
 -p,--password <password>   The password of the user to login.
 -u,--username <username>   The name of the user to login.
```

## How to run tests
To run tests, you need first to compile as explained in the previous section, and setup the server and 2 clients (Alice and Bob).

## Server
Start the server in folder **/server/**:
```
    mvn exec:java
```

## Client Alice
Start the client Alice:
```
    mvn exec:java -Dexec.args="-u alice -p Uvv1j7a60q2q0a4"
```


## Client Bob
Start the client Bob:
```
    mvn exec:java -Dexec.args="-u bob -p JNTpC0SE9Hzb3SG"
```

## Run Tests
To run the tests:
```
    mvn test
```

## Passwords for the users
|    User       |  Password       |
| :-----------: |:---------------:|
| alice         | Uvv1j7a60q2q0a4 |
| bob           | JNTpC0SE9Hzb3SG |   
| charlie       | 9QrKUNt9HAXPKG9 |   
| server        | admin           |   
