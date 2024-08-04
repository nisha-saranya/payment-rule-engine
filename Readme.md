# Payment Rule Engine

## Introduction

The Payment Rule Engine is designed to evaluate and process payment transactions based on a set of flexible and dynamic rules. This engine allows for real-time decision-making regarding payment methods, routing, fees, and additional verification requirements. Leveraging AWS services such as Lambda, DynamoDB, and API Gateway, this solution provides a scalable and maintainable way to manage payment processing rules. The engine is built to accommodate a variety of rules and criteria, including customer type, transaction amount, payment method, country, currency, 3DS information etc. By storing rules in DynamoDB and processing them with AWS Lambda, the system is both adaptable and resilient, ensuring that new rules can be added without modifying the core codebase.

## Project Structure
Inorder to test and work easily, the project is designed in two modules. 
1. A simple spring boot application to test the Lambda handler function locally.
2. The actual Lambda handler to be deployed in AWS

## Build and Running (Locally)

### Prerequisites

- Java 17
- Maven
- Docker, Docker-compose
- Make

### Build

To build the project, run the following command in the project root directory:

```sh
make build
```
If **make** is not available, run the following command from root directory.
```sh
mvn clean install
```
### Run
To simply run the application in a docker container, without bothering much just run the following command.

```sh
make just-run
```
If **make** is not available, run the following command from root directory.
```sh
docker-compose up -d
```
### Stop
To stop the application and kill the created docker containers, run the following command.
```sh
make stop
```
If **make** is not available, run the following command from root directory.
```sh
docker-compose down
```

Please refer the Makefile to see other options to build and run the app in your local.