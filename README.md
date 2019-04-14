# SEC
Highly Dependable Systems Project - HDS Notary

## Requirements
You must have the following tools:

- GNU/Linux
- Smart Card reader
- Middleware PT-CC
- Maven 3.x.x
- Java Development Kit 8 (JDK 8) 164+


Also check if JAVA_HOME and M2_HOME are set properly.

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
    mvn exec:java -Dexec.args="alice Uvv1j7a60q2q0a4"
```


## Client Bob
Start the client Bob:
```
    mvn exec:java -Dexec.args="bob JNTpC0SE9Hzb3SG"
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
