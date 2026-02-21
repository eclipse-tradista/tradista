# Eclipse Tradista

### *Liberate your capital market operations. Own your technology.*
**The modular, enterprise-grade Open Source platform for Cross-Asset Trading and Risk Management, powered by Jakarta EE.**

![License](https://img.shields.io/badge/License-Apache_2.0-33ff99.svg?link=https://www.apache.org/licenses/LICENSE-2.0)&emsp;
![Version](https://img.shields.io/badge/Version%20-%203.1.0%20-%2033ff99?color=33ff99)&emsp;

<br>

**Eclipse Tradista** is a modular, high-performance financial framework designed to unify and secure your capital market operations. From real-time market data analysis to post-trade lifecycle management, it provides a transparent and extensible alternative to proprietary systems. 

Built on a robust **Jakarta EE** architecture, Eclipse Tradista enables you to orchestrate complex workflows in a single, auditable environment:

* **Market Data & Analysis:** Seamlessly collect, store, and redistribute market data.
* **Front-to-Ops Lifecycle:** Handle pre-deal checks, trade booking, and automated cash/securities transfers.
* **Position & Risk:** Manage real-time P&L, multi-product positions, and cross-asset risk reporting.
* **Inventory Management:** Track cash and securities movements with precision.

Learn more at [**tradista.finance**](https://tradista.finance).

<p align="center">
  <img src="https://github.com/user-attachments/assets/c904ba36-8378-448f-97fb-ae215171abaf"  alt="Tradista Dashboard" width="900">
</p>
<p align="center">
  <em>Full Lifecycle Management: Seamlessly monitor trades, risk, and inventory.</em>
</p>

---

Eclipse Tradista is a Java based application. The server is based on WildFly and the clients are based on JavaFX and PrimeFaces.

Tested configuration:
- Server: WildFly 36.0.1.Final
- JRE: Oracle JRE v24.0.2+12-54 and OpenJDK JRE Eclipse Temurin implementation v24.0.2+12
- Database: Derby 10.17.1.0
- Build automation tool: Maven 3.9.12

Tradista can be built using Maven, build the whole application using the tradista-parent project.

1. Run ```mvn validate``` once to ensure that all needed dependencies are added to your local repository.

2. Then run ```mvn clean install``` to build Tradista.

Thanks for you interest. 
Feel free to download the Tradista Demo Package [here](https://github.com/oasuncion/tradista-demo) to test the solution.
Ping us anytime for any question.

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
- [QuickFIX/J](https://www.quickfixj.org/) for FIX messages import
- [Spring](https://spring.io/) for dependency injection, introspection and many more
- [Tradista Flow](https://github.com/oasuncion/tradista-flow) for Workflow management
- [Tweety](https://tweetyproject.org/) for first order logic processing
- [WildFly](https://www.wildfly.org/) for the server
