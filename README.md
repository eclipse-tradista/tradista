# Eclipse Tradista: the first open source Financial Risk Management Solution

![License](https://img.shields.io/badge/License-Apache_2.0-33ff99.svg?link=https://www.apache.org/licenses/LICENSE-2.0)&emsp;
![Version](https://img.shields.io/badge/Version%20-%203.0.0--SNAPSHOT%20-%2033ff99?color=33ff99)&emsp;


Eclipse Tradista is a lightweight Financial Risk Management Solution enabling you to manage in a single tool your daily treasury & risk management tasks. Eclipse Tradista, among other possibilities, can be used for market data analyses, pre-deal checks and trades booking, P&L management, financial inventory management and reporting.
See more on [tradista.finance](https://www.tradista.finance)

Eclipse Tradista is released under the Apache License, Version 2.0.

|![Login](./login.PNG)|![Dashboard](./dashboard.PNG)|
|:-:|:-:|
| Login Page | Trading Dashboard |


Eclipse Tradista is a Java based application. The server is based on WildFly and the clients are based on JavaFX and PrimeFaces.

Tested configuration:
- Server: WildFly 27.0.1.Final
- JRE: Oracle JRE v17.0.15+9-LTS-241, OpenJDK JRE RedHat implementation v17.0.15+6-LTS and OpenJDK JRE Eclipse Temurin implementation v17.0.15+6
- Database: Derby 10.16.1.1
- Build automation tool: Maven 3.9.6

Tradista can be built using Maven, build the whole application using the tradista-parent project.

1. Run ```mvn validate``` once to ensure that all needed dependencies are added to your local repository.

2. Then run ```mvn clean install``` to build Tradista.

Thanks for you interest. 
Feel free to download the Tradista Demo Package [here](https://github.com/oasuncion/tradista-demo) to test the solution.
Ping me anytime for any question.

# Dependencies
Tradista is made possible using powerful third party tools:
- [Apache Commons](https://commons.apache.org/) for Maths, CSV processing and many more
- [Apache Derby](https://db.apache.org/derby/) for the database
- [Apache Maven](https://maven.apache.org/) for the builds
- [Apache POI](https://poi.apache.org/) for Microsoft Excel export
- [ASP4J](https://github.com/hbeck/asp4j) for Answer Set Programming
- [Bloomberg API](https://www.bloomberg.com/professional/support/api-library/) to connect to Bloomberg
- [JavaFX](https://openjfx.io/) for the Desktop client
- [PrimeFaces](https://www.primefaces.org/) for the WEB UI (a JSF implementation)
- [PrimeFlex](https://www.primeflex.org/) for the WEB UI (a CSS library)
- [Quandl4J](https://github.com/jimmoores/quandl4j) to connect to Quandl REST API
- [Quartz](http://www.quartz-scheduler.org/) for job scheduling
- [Spring](https://spring.io/) for dependency injection, introspection and many more
- [Tradista Flow](https://github.com/oasuncion/tradista-flow) for Workflow management
- [Tweety](https://tweetyproject.org/) for first order logic processing
- [WildFly](https://www.wildfly.org/) for the server
