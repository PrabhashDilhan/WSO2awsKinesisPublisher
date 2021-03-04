package es.inetum.world;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;

import es.fluidra.prices.data.process.GetSinglePricePerItemClientCallable;
import es.fluidra.prices.data.values.NetProductPrice;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.json.JSONObject;

/*
<class name="es.inetum.world.HazelcastMediator" >
	<property name="method" value="GetSinglePricePerItemClientCallable" />
	<property name="divisionId" value="374" />
	<property name="clientId" value="1" />
	<property name="productSKU" value="1" />
</class>
*/
public class HazelcastMediator extends AbstractMediator implements ManagedLifecycle {

	private static final Logger log = LoggerFactory.getLogger(HazelcastMediator.class);
	private static final String DIV_1_ID = "374";
	
	private static HazelcastInstance client = null;
	private static IExecutorService exec = null;

	private String method;
	private String divisionId;
	private String clientId;
	private String productSKU;

	public boolean mediate(MessageContext context) { 
		
		//GetSinglePricePerItemClientCallable("374","1","1");
		String body = null;
		switch (method) {
			case "GetSinglePricePerItemClientCallable":
				body = GetSinglePricePerItemClientCallable(divisionId,clientId,productSKU);
				break;
			default:
				log.info("Método no reconocido");
		}
		
		boolean success = false;
		if (body != null ) {
			// Setting the new json payload.
			JsonUtil.newJsonPayload(((Axis2MessageContext) context).getAxis2MessageContext(),body, true, true);
			success = true;
		}
		return success;
	}

	private static String GetSinglePricePerItemClientCallable(String divisionId, String clientId, String productSKU) {
		GetSinglePricePerItemClientCallable priceRequest = new GetSinglePricePerItemClientCallable(divisionId,clientId,productSKU);
		Future<NetProductPrice> retrievedPrice = exec.submitToKeyOwner(priceRequest, DIV_1_ID);
		JSONObject jsonObject = null;
		try {
			log.info("**Retrieved Price = " + retrievedPrice.get());
			jsonObject = new JSONObject(retrievedPrice.get());
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	public void destroy() {
		if (client != null) {
			log.info("Shutting down hazelcast client.");
			client.shutdown();
		}
	}

	public void init(SynapseEnvironment arg0) {
		if (client == null) {
			synchronized(this) {
				if (client == null) {
					try {
						log.info("Initializing Hazelcast instance.");
						client = HazelcastClient.newHazelcastClient();
						exec = client.getExecutorService("default");
						
					}catch (Exception e){
						log.error("Exception occured while creating hazelcast client:", e);
					}
				}
			}
		}
	}

	//Getters & Setters

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(String divisionId) {
		this.divisionId = divisionId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getProductSKU() {
		return productSKU;
	}

	public void setProductSKU(String productSKU) {
		this.productSKU = productSKU;
	}
	
}
