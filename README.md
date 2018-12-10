# xProject Java sample application for SAP Cloud Platform Cloud Foundry environment

This repository contains the xProject sample application for the cloud foundry environment in SAP Cloud Platform (SCP). It has been used as a sample application at SAP conferences such as [TechEd](https://sessioncatalog.sapevents.com/go/agendabuilder.sessions/?l=192&sid=62444_483736&locale=en_US).

## Prerequisites
- You have [Maven](https://maven.apache.org/) installed
- You have the [Cloud Foundry CLI (Command Line Interface)](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/4ef907afb1254e8286882a2bdef0edf4.html) installed 
- You have your SCP subaccount (e.g. your trial account) [enabled for the CF environment](https://help.sap.com/viewer/65de2977205c403bbc107264b8eccf4b/Cloud/en-US/3609c701207e4c7eae452017b3ef05b0.html).
- In your SCP subaccount you have created an organization and space to deploy the application.

## Setup instructions
1. Building the application
    1. Go to the root directory (/) of the application and run `mvn clean install`
    2. Check if you the deployable web archive file `/target/xproject.war` has been created
    
2. Deploy the application
    1. Go to the root (/) of the application source directory
    2. cf login -a https://api.cf.eu10.hana.ondemand.com (replace eu10 with us10 if your are in a different region) -o <your org> -s <your space> -u <your platform user> -p <your password>
    3. cf create-service xsuaa application xproject-xsuaa -c xs-security.json
    4. cf create-service postgresql v9.6-xsmall xproject-postgresql
    5. cf push --no-route
    6. cf map-route xproject cfapps.eu10.hana.ondemand.com -n xproject-<your subaccount subdomain name>
    7. cf logout
