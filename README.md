# WSO2awsKinesisPublisher

## This class mediator can be used to publish messages to AWS kinesis using WSO2 EI.

*SAMPLE SERVICE*

### Steps to appy the class mediator

1. build maven project and copy the bundle jar file to the <EI_HOME>/dropins direcotry.
2. Start the server and you can use below service to test class mediator.

```
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="test"
       transports="http https"
       startOnLoad="true">
   <description/>
   <target>
      <inSequence>
         <class name="org.wso2.aws.KinesisMediator">
            <property name="streamName" value="testk"/>
            <property name="region" value="us-east-1"/>
            <property name="accessKey" value="your aws accessKey"/>
            <property name="secretKey" value="your aws secretKey"/>
            <property name="message" expression="json-eval($)"/>
         </class>
         <respond/>
      </inSequence>
   </target>
</proxy>
```
