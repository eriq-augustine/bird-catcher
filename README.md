Bird Catcher
======

A way to gather tweets from twitter.

## Directory Structure
```
//
├── README.md
├── bin -- build output
├── build.xml -- Apache Ant build configuration
├── config -- Server Configuration
├── doc -- `ant doc` output directory
├── lib
├── log -- default location for logs
├── src -- source code
├── test -- test code
└── tools -- additional tools
```

## Deployment
Building and running requires:
   - Java >= 1.7 (Has been tested with both Oracle Java and OpenJDK.)
   - Apache Ant

### Configuration
Config files are located in the config folder.
Right now there is nothing that you need to have in base.proeprties since the defaults are sufficient.
But if you want, you can configure logging files with the following properties:
   - INFO_LOG
   - WARN_LOG
   - DEBUG_LOG
   - ERROR_LOG
   - FATAL_LOG
   - HOST_NAME

You will need a properties file yo hold your Twitter secrets.
The default location of this file is config/secrets.properties.
This file should define the following properties:
   - TWITTER_API_KEY
   - TWITTER_API_SECRET

## Running
There is a sample driver located at in src/edu/calpoly/twitter/GatherTweets.java.
You can run it with: `java edu.calpoly.twitter.GatherTweets`

There is also a driver to check to make sure that your Twitter API Key and API Secret are corrct.
You can run it with: `java edu.calpoly.twitter.util.TwitterAuth <api key> <api secret>`
