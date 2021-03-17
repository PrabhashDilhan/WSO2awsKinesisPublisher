package es.inetum.world;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.map.IMap;

import es.fluidra.hazelcast.prices.process.task.GetSinglePricePerItemClientCallable;
import es.fluidra.hazelcast.prices.process.task.GetPricesPerClientNItemsCallable;
import es.fluidra.hazelcast.prices.data.values.NetProductPriceResponse;
import es.fluidra.hazelcast.prices.data.keys.BasicTariffKey;
import es.fluidra.hazelcast.prices.data.values.TariffProductPrice;
import es.fluidra.hazelcast.prices.data.values.NetProductPrice;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/*
<class name="es.inetum.world.HazelcastMediator" >
	<property name="method" value="GetSinglePricePerItemClientCallable" />
	<property name="divisionId" value="001" />
	<property name="clientId" value="001" />
	<property name="productSKU" value="001" />
	<property name="umv" value="0" />
</class>
<class name="es.inetum.world.HazelcastMediator" >
	<property name="method" value="GetPricesPerClientNItemsCallable" />
	<property name="divisionId" value="001" />
	<property name="clientId" value="001" />
	<property name="productSKU" value="001,999" />
	<property name="umv" value="0" />
</class>

curl -X POST http://localhost:8280/hz
*/
public class HazelcastMediator extends AbstractMediator implements ManagedLifecycle {

	private static final Logger log = LoggerFactory.getLogger(HazelcastMediator.class);
	private static final String DIV_1_ID = "001";
	
	private static HazelcastInstance client = null;
	private static IExecutorService exec = null;
	private static IMap<BasicTariffKey, TariffProductPrice> basicTariffMap = null;

	private String method;
	private String divisionId;
	private String clientId;
	private String productSKU;
	private int umv;

	public boolean mediate(MessageContext context) { 
		
		String body = null;
		switch (method) {
			case "GetSinglePricePerItemClientCallable":
				body = GetSinglePricePerItemClientCallable(divisionId,clientId,productSKU,umv);
				break;
			case "GetPricesPerClientNItemsCallable":
				body = GetPricesPerClientNItemsCallable(divisionId,clientId,productSKU,umv);
				break;
			/*
			case "GetPricesTariffNItemsCallable":
				body = GetPricesTariffNItemsCallable(divisionId,clientId,productSKU,umv);
				break;
			*/
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

	private static String GetSinglePricePerItemClientCallable(String divisionId, String clientId, String productSKU, int umv) {
		GetSinglePricePerItemClientCallable priceRequest = new GetSinglePricePerItemClientCallable(divisionId,clientId,productSKU,umv);
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

	private static String GetPricesPerClientNItemsCallable(String divisionId, String clientId, String productSKU,int umv) {
		List<String> productSKUCollection = Arrays.stream(productSKU.split(",")).map(String::trim).collect(Collectors.toList());
	
		GetPricesPerClientNItemsCallable priceRequest = new GetPricesPerClientNItemsCallable(divisionId,clientId, productSKUCollection,umv);
		Future<List<NetProductPrice>> retrievedPrice = exec.submitToKeyOwner(priceRequest, DIV_1_ID);
		
		JSONObject jsonObject = null;
		try {
			log.info("**Retrieved Price = " + retrievedPrice.get());
			jsonObject = new JSONObject();
			JSONArray array = new JSONArray();
			
			for(NetProductPrice price : retrievedPrice.get()) {
				if (checkNetProductPrice(price)) {
			        JSONObject p = new JSONObject();
			        
			        // Discounts
			        JSONObject discountsObject = new JSONObject();
			        discountsObject.put("total", price.getDiscounts().getTotal());
			        discountsObject.put("percentages", price.getDiscounts().getPercentages());
			       
			        JSONArray discountsArray = new JSONArray();
			        discountsArray.put(discountsObject);
			        p.put("discounts", discountsArray);
			        
			        // Others params
			        p.put("currency", price.getCucd());
			        p.put("item-no", price.getItno());
			        p.put("net", price.getNet());
			        p.put("sales", price.getSales());
			        p.put("vat-amount", price.getVat());
			        p.put("vat-percentage", price.getVat100());
			        
			        //add to prices array
			        array.put(p);
				}
		    }
			jsonObject.put("prices",array);
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	private static Boolean checkNetProductPrice (NetProductPrice price) {
		Boolean res = false;
		if (price.getCucd()!=null && price.getDiscounts()!=null && price.getNet() != 0.0 && price.getSales() != 0.0 && price.getVat() != 0.0 && price.getVat100() != 0.0) {
			res = true;
		}
		else {
			log.info("Product '"+ price.getItno()+"' not found");
		}
		
		return res;
	}

	/*
	private static String GetPricesTariffNItemsCallable(String divisionId, String clientId, String productSKU) {
		
		Set<BasicTariffKey> multipleTariffKeys = new HashSet<>();
		for (String product : productSKU.split(",")) {
			multipleTariffKeys.add(new BasicTariffKey(DIV_1_ID, product));
		}
		JSONObject jsonObject = null;
		try {
			Map<BasicTariffKey, TariffProductPrice> tariff = basicTariffMap.getAll(multipleTariffKeys);
			jsonObject = new JSONObject(tariff);
		}
		catch (Exception e) {
			log.error("Error in HazelcastMediator.GetPricesTariffNItemsCallable ", e);
		}
		return jsonObject.toString();
	}
	*/
	public void destroy() {
		if (client != null) {
			log.info("Shutting down hazelcast client.");
			//client.shutdown();
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
						basicTariffMap = client.getMap("price-tariffrate-cust");
						
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
	
	public int getUmv() {
		return umv;
	}

	public void setUmv(int umv) {
		this.umv = umv;
	}

}