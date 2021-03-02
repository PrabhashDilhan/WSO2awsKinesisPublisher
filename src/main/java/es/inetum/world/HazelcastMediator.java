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

public class HazelcastMediator extends AbstractMediator implements ManagedLifecycle {

	private static final Logger log = LoggerFactory.getLogger(HazelcastMediator.class);
	private static final String DIV_1_ID = "374";
	
	private static HazelcastInstance client = null;
	private static IExecutorService exec = null;  

	public boolean mediate(MessageContext context) { 
		
		GetSinglePricePerItemClientCallable("374","1","1");
		
		return true;
	}

	private static void GetSinglePricePerItemClientCallable(String divisionId, String clientId, String productSKU) {
		GetSinglePricePerItemClientCallable priceRequest = new GetSinglePricePerItemClientCallable(divisionId,clientId,productSKU);
		Future<NetProductPrice> retrievedPrice = exec.submitToKeyOwner(priceRequest, DIV_1_ID);
		try {
			System.out.println("**Retrieved Price = " + retrievedPrice.get());
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
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
	
}
