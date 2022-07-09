/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.managers;

import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import com.cognitive.nih.niddk.mccapi.data.FHIRServer;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

@Log
@Component
public class FHIRServerManager {

	@Value("${FHIR_SERVER}")
	private String defaultFHIRServerAddress;

	@Value("${FHIRIO_SERVER}")
	private String fhirIOServerAddress;

	@Value("${SECONDARY_SERVER}")
	private String secondaryServerAddress;

	@Value("${fhir.connect.timeout:30000}")
	private String connectTimeout;
	@Value("${fhir.request.timeout:30000}")
	private String requestTimeout;
	@Value("${hapi.logging.enabled:false}")
	private String enableLogging;
	@Value("${hapi.logging.request.summary:true}")
	private String log_request_summary;
	@Value("${hapi.logging.request:body:false}")
	private String log_request_body;
	@Value("${hapi.logging.request.header:false}")
	private String log_request_header;
	@Value("${hapi.logging.response.summary:true}")
	private String log_response_summary;
	@Value("${hapi.logging.response.body:false}")
	private String log_response_body;
	@Value("${hapi.logging.response.header:false}")
	private String log_response_header;

	private FHIRServer defaultFHIRServer;

	private FHIRServer fhirIOServer;

	private FHIRServer secondaryServer;

	public LoggingInterceptor getLoggingInterceptor() {
		if (loggingInterceptor == null) {
			loggingInterceptor = new LoggingInterceptor();
		}
		return loggingInterceptor;
	}

	public boolean isEnableFHIRLogging() {
		return enableFHIRLogging;
	}

	private boolean enableFHIRLogging;
	public int connTimeout = 30 * 1000;
	public int reqTimeout = 30 * 1000;

	private LoggingInterceptor loggingInterceptor;
	private static FHIRServerManager singleton; // = new FHIRServerManager();

	public FHIRServerManager() {
		 
	}

	public static FHIRServerManager getManager() {
		return singleton;
	}

	public FHIRServer getDefaultFHIRServer() {
		return defaultFHIRServer;
	}

	public FHIRServer getFHIRIOServer() {
		return fhirIOServer;
	}

	public void setDefaultFHIRServer(FHIRServer defaultFHIRServer) {
		this.defaultFHIRServer = defaultFHIRServer;
	}

	@PostConstruct
	private void createServers() {
		defaultFHIRServer = defineDefaultServers("MMC eCarePlan Test", "MCCeCarePlanTest", defaultFHIRServerAddress);
		fhirIOServer = defineDefaultServers("MMC eCarePlan FHIR IO Server", "FHIRIOServer", fhirIOServerAddress);
		secondaryServer = defineDefaultServers("MMC eCarePlan Secondary Server", "SecondaryServer", secondaryServerAddress);

	}

	private FHIRServer defineDefaultServers(String name, String id, String fhirAddress) {
		FHIRServer srv = new FHIRServer();
		srv.setBaseURL(fhirAddress);
		srv.setName(name);
		srv.setId(id);

		log.info("Default FHIR Server = " + defaultFHIRServerAddress);
//        addServer(srv);
		if (singleton == null) {
			singleton = this;
		}

		if (requestTimeout != null) {
			if (requestTimeout.length() > 0) {
				try {
					reqTimeout = Integer.parseInt(requestTimeout);
					log.info("Config: fhir.request.timeout = " + requestTimeout);
				} catch (Exception e) {
					log.log(Level.WARNING,
							"Failed to parse FHIR request timeout (" + requestTimeout + "), using default", e);
				}
			}
		}

		if (connectTimeout != null) {
			if (connectTimeout.length() > 0) {
				try {
					connTimeout = Integer.parseInt(connectTimeout);
					log.info("Config: fhir.connect.timeout = " + connectTimeout);
				} catch (Exception e) {
					log.log(Level.WARNING,
							"Failed to parse FHIR conect timeout (" + connectTimeout + "), using default", e);
				}
			}
		}
		enableFHIRLogging = Boolean.valueOf(enableLogging).booleanValue();

		if (enableFHIRLogging) {
			loggingInterceptor = new LoggingInterceptor();
			loggingInterceptor.setLogRequestBody(Boolean.valueOf(log_response_body).booleanValue());
			loggingInterceptor.setLogRequestHeaders(Boolean.valueOf(log_request_header).booleanValue());
			loggingInterceptor.setLogRequestSummary(Boolean.valueOf(log_request_summary).booleanValue());
			loggingInterceptor.setLogResponseSummary(Boolean.valueOf(log_response_summary).booleanValue());
			loggingInterceptor.setLogResponseBody(Boolean.valueOf(log_response_body).booleanValue());
			loggingInterceptor.setLogResponseHeaders(Boolean.valueOf(log_response_header).booleanValue());
		}
		return srv;

	}

    public FHIRServer getSecondaryServer() {
        return secondaryServer;
    }

}
