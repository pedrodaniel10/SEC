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
    mvn clean install 
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
 -help                   			Prints this message
 -noCC,--noCitizenCard    			Disables signature using the
                                    Portuguese Citizen Card
 -np,--notary-password <password>   The Notary's private key password in
                                    case Citizen Card is disabled. It is
                                    required in that case.
 -p,--server-password <password>    The server's private key password.
 -sid,--server-id <id>              The server's identifier.

```


### Client
Tu run the client run the following command under **/hds-notary/client**:
``` 
mvn exec:java@<type> -Dexec.args="<arguments>"
```

The **\<arguments>** can be filled with the following arguments:
```
usage: HDS-Client [-help] [-p <password>] [-u <username>]
 -help                      Prints this message
 -p,--password <password>   The password of the user to login.
 -u,--username <username>   The name of the user to login.
```
The **\<type>** can be filled with the following:
```
good	Correct user
evil	Byzantine user
```

## How to run tests
To run tests, you need first to compile as explained in the previous section, and setup 4 servers and 3 clients (Alice, Bob, Charlie).

## Server 0
Start the server in folder **/server/**:
```
    mvn exec:java -Dexec.args="-noCC -np admin -p admin -sid 0"
```

## Server 1
Start the server in folder **/server/**:
```
    mvn exec:java -Dexec.args="-noCC -np admin -p admin -sid 1"
```

## Server 2
Start the server in folder **/server/**:
```
    mvn exec:java -Dexec.args="-noCC -np admin -p admin -sid 2"
```

## Server 3
Start the server in folder **/server/**:
```
    mvn exec:java -Dexec.args="-noCC -np admin -p admin -sid 3"
```

## Client Alice
Start the client Alice:
```
    mvn exec:java@good -Dexec.args="-u alice -p Uvv1j7a60q2q0a4"
```


## Client Bob
Start the client Bob:
```
    mvn exec:java@good -Dexec.args="-u bob -p JNTpC0SE9Hzb3SG"
```

## Client Evil Charlie
Start the client Bob:
```
    mvn exec:java@evil -Dexec.args="-u charlie -p 9QrKUNt9HAXPKG9"
```

## Run Tests

### 1st Test - Correct Servers and Correct Clients
Use the Alice and Bob clients to execute actions and trade goods between them.

### 2nd Test - One Byzantine Server and Correct Clients
Terminate one of the servers silently.
Use the Alice and Bob clients to execute actions.

### 3rd Test - Correct Servers and One Byzantine Client
Start the server terminated.
Use Charlie to execute actions.

### 4th Test - One Byzantine Server and One Byzantine Client
Terminate one of the servers silently.
Use Charlie to execute actions.


## Passwords for the users
|    User       |  Password       |
| :-----------: |:---------------:|
| alice         | Uvv1j7a60q2q0a4 |
| bob           | JNTpC0SE9Hzb3SG |   
| charlie       | 9QrKUNt9HAXPKG9 |   
| server        | admin           |   
