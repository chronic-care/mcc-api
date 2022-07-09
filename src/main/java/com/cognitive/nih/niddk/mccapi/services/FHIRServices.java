/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import com.cognitive.nih.niddk.mccapi.data.FHIRServer;
import com.cognitive.nih.niddk.mccapi.managers.FHIRServerManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FHIRServices {

	private final FhirContext stu4Context;

	private static final FHIRServices singleon = new FHIRServices();

	public static FHIRServices getFhirServices() {
		return singleon;
	}

	public FHIRServices() {

		stu4Context = FhirContext.forR4();
		FHIRServerManager srvMgr = FHIRServerManager.getManager();
		stu4Context.getRestfulClientFactory().setConnectTimeout(srvMgr.connTimeout);
		stu4Context.getRestfulClientFactory().setSocketTimeout(srvMgr.reqTimeout);
	}

	public FhirContext getR4Context() {
		return stu4Context;
	}

	public IGenericClient getClient(Map<String, String> headers) {
		IGenericClient client;
		FHIRServerManager srvMgr = FHIRServerManager.getManager();
		// FhirContext fhirContext = FHIRServices.getFhirServices().getR4Context();

		if (headers.containsKey("mcc-fhir-server")) {
			String server = headers.get("mcc-fhir-server");
			// if (server != null && server.equals("https://ocp.fhir4.ocp-nonprod.net:8082/fhir")) {
				// server = "https://test-ocp.mynjinck.com:443/fhir";
			// }
			log.info("Server is " + server);
			client = stu4Context.newRestfulGenericClient(server);
			if (headers.containsKey("mcc-token")) {
				BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(headers.get("mcc-token"));
				client.registerInterceptor(authInterceptor);
			}
		} else {
			log.warn("No Server provided - using default");
			FHIRServer srv = srvMgr.getDefaultFHIRServer();
			client = stu4Context.newRestfulGenericClient(srv.getBaseURL());
		}
		if (srvMgr.isEnableFHIRLogging()) {
			client.registerInterceptor(srvMgr.getLoggingInterceptor());
		}
		return client;
	}


	public IGenericClient getClientSecondaryServer(Map<String, String> headers) {
		IGenericClient client = null;
		FHIRServerManager srvMgr = FHIRServerManager.getManager();
		if (headers.containsKey("mcc-secondaryfhir-server")) {
			String server = headers.get("mcc-secondaryfhir-server");
 			log.info("Server is " + server);
			client = stu4Context.newRestfulGenericClient(server);
			if (headers.containsKey("mcc-token")) {
				BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(headers.get("mcc-token"));
				client.registerInterceptor(authInterceptor);
			}
			if (srvMgr.isEnableFHIRLogging()) {
				client.registerInterceptor(srvMgr.getLoggingInterceptor());
			}
		} else {
			log.warn("No Secondary Server provided");
		}
		
		return client;
	}
	public IGenericClient getIOClient() {
		IGenericClient client;
		FHIRServerManager srvMgr = FHIRServerManager.getManager();

		FHIRServer srv = srvMgr.getFHIRIOServer();
		
		stu4Context.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		client = stu4Context.newRestfulGenericClient(srv.getBaseURL());

//		if (srvMgr.isEnableFHIRLogging()) {
			client.registerInterceptor(srvMgr.getLoggingInterceptor());
//		}
		return client;
	}
///**
// * This is to mimic multiple endpoints similar to facade
// * @return
// */
//	public IGenericClient getSecondaryServer() {
//		IGenericClient client;
//		FHIRServerManager srvMgr = FHIRServerManager.getManager();
//
//		FHIRServer srv = srvMgr.getSecondaryServer();
//		
//		stu4Context.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
//		client = stu4Context.newRestfulGenericClient(srv.getBaseURL());
//
//		if (srvMgr.isEnableFHIRLogging()) {
//			client.registerInterceptor(srvMgr.getLoggingInterceptor());
//		}
//		return client;
//	}
}
