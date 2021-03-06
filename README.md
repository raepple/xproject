# xProject Java sample application for SAP Cloud Platform

This repository contains the xProject sample application used in units 2, 3 and 4 of week 5 (Security) in the openSAP course [SAP Cloud Platform Essentials (Update Q3/2017)](https://open.sap.com/courses/cp1-2).
Follow the instructions below to install the application on your local development system and deploy it in your SAP Cloud Platform Neo trial account.

## Prerequisites
- You have Eclipse NEON with the [SAP Cloud Platform Tools for Java](https://tools.hana.ondemand.com/#cloud) installed
- You have downloaded and extracted the SAP Cloud Platform Neo Environment SDK for Java Web Tomcat 8 SDK from [https://tools.hana.ondemand.com/#cloud](https://tools.hana.ondemand.com/#cloud)
- You have a Java 8 JDK installed

## Setup instructions
1. Create local server runtime
    1. Launch Eclipse NEON and create a new workspace
    2. Open **Windows > Preferences > Server > Runtime Environment** and click on **Add..**
    3. Select **SAP > Java Web Tomcat 8 Server** from the list and click **Next**
    4. Browse to your Java Web Tomcat 8 SDK directory and click **Finish**
    5. Click **OK**
2. Import project into Eclipse
    1. Open **File > Import > Git > Projects from Git** and click **Next**
    2. Select **Clone URI** as Repository Source
    3. Enter *https://github.com/raepple/xproject.git* for the URI and click **Next**
    4. Select *master* branch and click **Next**
    5. Select your local destination directory and click **Next**
    6. Choose **Import existing Eclipse projects** and click **Next**
    7. Click **Finish**
3. Deploy xproject application to your Neo trial account
    1. Open **File > New > Server** and click **Next**
    2. Select **SAP > SAP Cloud Platform** for the server type
    3. Enter *hanatrial.ondemand.com* in field **Region host**. Click **next**
    4. Enter *xproject* for the application name. Enter your trial account information in the subaccount information (e.g. p0123456789trial as subaccount name, P0123456789 as user name, and your password). Click **Next**
    5. Add xproject to your new server and click **Finish**
