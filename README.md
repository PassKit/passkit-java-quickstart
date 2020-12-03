PassKit Java Quickstart
=======================

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/com.passkit.grpc/sdk.svg?label=Maven%20Central)](https://search.maven.org/artifact/com.passkit.grpc/sdk)

### Overview

This quickstart aims to help  get Java developers up and running with the PassKit SDK as quickly as possible.

### Prerequisites

You will need the following:

- A PassKit account (signup for free at https://app.passkit.com)
- Your PassKit SDK Credentials (available from the https://app.passkit.com/app/account/developer-tools)
- Java JDK 8 or above (11.0.9LTS recommended)
- Gradle Build Tool (https://gradle.org)

### Configuration

1. Download or clone this quickstart repository, then add the following three PassKit credential files to `src/main/resources/credentials`:
    - certificate.pem
    - ca-chain.pem
    - key-java.pem
    
    You can disregard the key.pem credentials file as it is not compatible with Java.

2. Edit `src/main/resources/passkit.properties`
    - set `credentials.password` to the password that you set when requesting your SDK credentials from https://app.passkit.com
    - set other options as required
    
3. If you wish to receive enrolment emails, edit lines 190 and 203 of the QuickStartLoyalty class to provide an address where you can receive mail.    
    
### Running the tests

Run `gradle test --tests QuickstartLoyaltyTest`

The Loyalty tests will create a membership program with 2 tiers, base and VIP.  It will enrol two members, one in each tier.

The tests will display URLs to the generated passes and to the enrolment page.  It will pause for a period determined in `passkit.properties` for you to check them.

The tests will then delete and clean-up all assets that it created.

### Notes

For implementing in your own projects, use the GrpcConnection class to manage connection to the PassKit gRPC endpoints.

Use the GrpcConnection's ManagedChannel object to create the stubs you require in your implementation. 


