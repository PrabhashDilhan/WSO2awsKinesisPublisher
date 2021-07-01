package org.wso2.aws;

import java.util.concurrent.ExecutionException;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

/*

<class name="es.inetum.world.KinesisMediator" >
	<property name="streamName" value="streamName" />
	<property name="region" value="region" />
	<property name="accessKey" value="accessKey" />
	<property name="secretKey" value="secretKey" />
	<property name="message" value="message" />
</class>

curl -X POST http://localhost:8280/hz
*/
public class KinesisMediator extends AbstractMediator{

	private static final Logger log = LoggerFactory.getLogger(KinesisMediator.class);
	
	private String streamName;
	private String region;
	private String accessKey;
	private String secretKey;
	private String message;

	public boolean mediate(MessageContext context) { 
		
	    Region reg = Region.of(region);
	    KinesisAsyncClient kinesisClient = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(reg));
	    
	    System.setProperty("aws.accessKeyId", accessKey);
	    System.setProperty("aws.secretAccessKey", secretKey);
		System.out.println("message:" + message);
		System.out.println("streamName:" + streamName);
		System.out.println("region:" + region);
		System.out.println("accessKey:" + accessKey);
		System.out.println("secretKey:" + secretKey);
	
		PutRecordRequest request = PutRecordRequest.builder()
        		.partitionKey(RandomStringUtils.randomAlphabetic(5, 20))
                .streamName(streamName)
                .data(SdkBytes.fromString(message, Charsets.UTF_8))
                .build();
		
        try {
            kinesisClient.putRecord(request).get();
        } catch (InterruptedException e) {
            log.info("Interrupted, assuming shutdown.");
        } catch (ExecutionException e) {
            log.error("Exception while sending data to Kinesis. Will try again next cycle.", e);
        }
		return true;
	}

	
	//Getters & Setters
	
	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	

}