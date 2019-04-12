# SEC
Highly Dependable Systems Project - HDS Notary

## Requirements
You must have installed the following tools:

- Maven 3.x.x
- Java Development Kit 8 (JDK 8) 

Also check if JAVA_HOME and M2_HOME are set properly.

## How to compile
To compile just run the command in the directory **/hds-notary/**:
```
    mvn clean install -DskipTests
```
**Note:** Is essential the presence of the flag _-DskipTests_, otherwise the tests will certainly fail 
as the class loaders don't yet exist at this point.

## How to run tests
To run tests, you need first to compile as explained in the previous section, to run the tests
simply execute the command
```
    mvn test
```
Running this command, all tests should pass.

## Passwords for the users
|    User       |  Password       |
| :-----------: |:---------------:| 
| alice         | Uvv1j7a60q2q0a4 | 
| bob           | JNTpC0SE9Hzb3SG |   
| charlie       | 9QrKUNt9HAXPKG9 |   