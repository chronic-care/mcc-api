/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.controllers;

import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.LinkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import com.cognitive.nih.niddk.mccapi.data.Context;
import com.cognitive.nih.niddk.mccapi.data.FHIRServer;
import com.cognitive.nih.niddk.mccapi.data.GoalLists;
import com.cognitive.nih.niddk.mccapi.data.GoalSummary;
import com.cognitive.nih.niddk.mccapi.data.MccPatient;
import com.cognitive.nih.niddk.mccapi.data.GoalSummary;
import com.cognitive.nih.niddk.mccapi.managers.ContextManager;
import com.cognitive.nih.niddk.mccapi.managers.FHIRServerManager;
import com.cognitive.nih.niddk.mccapi.managers.QueryManager;
import com.cognitive.nih.niddk.mccapi.mappers.GoalMapper;
import com.cognitive.nih.niddk.mccapi.mappers.IR4Mapper;
import com.cognitive.nih.niddk.mccapi.services.FHIRServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class GoalController {
	private final QueryManager queryManager;
	private final IR4Mapper mapper;
	private final ContextManager contextManager;

	public GoalController(QueryManager queryManager, IR4Mapper mapper, ContextManager contextManager) {
		this.queryManager = queryManager;
		this.mapper = mapper;
		this.contextManager = contextManager;
	}

	/**
	 *
	 * @param subjectId
	 * @param careplanId
	 * @param headers
	 * @param webRequest
	 * @return
	 */
	@GetMapping("/summary/goals")
	public GoalLists getGoalSummary(@RequestParam(required = true, name = "subject") String subjectId,
			@RequestParam(required = false, name = "careplan") String careplanId,
			@RequestHeader Map<String, String> headers, WebRequest webRequest) {
		return commonGoalSummary(subjectId, careplanId, headers, webRequest);
	}

	/**
	 * @Deprecated please use /summary/gooal
	 * @param subjectId
	 * @param careplanId
	 * @param headers
	 * @param webRequest
	 * @return
	 */
	@GetMapping("/goalsummary")
	public GoalLists getGoalSummaryOld(@RequestParam(required = true, name = "subject") String subjectId,
			@RequestParam(required = false, name = "careplan") String careplanId,
			@RequestHeader Map<String, String> headers, WebRequest webRequest) {
		return commonGoalSummary(subjectId, careplanId, headers, webRequest);
	}

	private GoalLists commonGoalSummary(String subjectId, String careplanId, Map<String, String> headers,
			WebRequest webRequest) {

		logger.error("START commonGoalSummary");

		GoalLists out = new GoalLists();

		FHIRServices fhirSrv = FHIRServices.getFhirServices();
		IGenericClient client = fhirSrv.getClient(headers);
		IGenericClient secondaryClient = fhirSrv.getClientSecondaryServer(headers);

		Map<String, String> values = new HashMap<>();
		String callUrl = queryManager.setupQuery("Goal.Query", values, webRequest);

		if (callUrl != null) {
			Bundle results = client.fetchResourceFromUrl(Bundle.class, callUrl);
			Context ctx = contextManager.setupContext(subjectId, client, mapper, headers);
			for (Bundle.BundleEntryComponent e : results.getEntry()) {
				if (e.getResource().fhirType().compareTo("Goal") == 0) {
					Goal g = (Goal) e.getResource();
					GoalSummary gs = mapper.fhir2summary(g, ctx);
					out.addSummary(gs);
				}
			}

			String identifierScope = null;
			if (headers.containsKey("mcc-fhir-server")) {
				identifierScope = headers.get("mcc-fhir-server");

			} else {
				identifierScope = "https://gw.interop.community/SyntheaTest8/data";
			}

			logger.trace("getIOPatient( identifierScope, subjectId) " + identifierScope + " , " + subjectId);

			Patient ioPatient = null;
			try {
				ioPatient = getIOPatient(identifierScope, subjectId);
			} catch (Throwable ste) {
				logger.error("getIOPatient( identifierScope, subjectId) " + identifierScope + " , " + subjectId + ste.getLocalizedMessage());

			}

			if (ioPatient != null) {

				logger.error("END commonGoalSummary ioPatient != null");

				results = fhirSrv.getIOClient().fetchResourceFromUrl(Bundle.class,
						"/Goal?subject=" + URLEncoder.encode(ioPatient.getId(), StandardCharsets.UTF_8));

				for (Bundle.BundleEntryComponent e : results.getEntry()) {
					if (e.getResource().fhirType().compareTo("Goal") == 0) {
						Goal g = (Goal) e.getResource();
						GoalSummary gs = mapper.fhir2summary(g, ctx);
						out.addSummary(gs);
					}
				}
			} else {
				logger.error("END commonGoalSummary ioPatient == null");

			}

			if (secondaryClient != null) {
				Patient primaryPatient = getPatient(subjectId, client);

				Patient secondaryPatient = getPatientByName(primaryPatient.getNameFirstRep().getFamily(),
						secondaryClient);
				if (secondaryPatient != null) {
					
					
					Map<String, String> secondaryValues = new HashMap<>();
					secondaryValues.put("subject", secondaryPatient.getIdElement().getIdPart());
					
					String secondaryCallUrl = queryManager.setupQuery("Goal.Query", secondaryValues, webRequest);

					

					try {
					results = secondaryClient.fetchResourceFromUrl(Bundle.class, secondaryCallUrl);
				
					ctx = contextManager.setupContext(subjectId, secondaryClient, mapper, headers);
					for (Bundle.BundleEntryComponent e : results.getEntry()) {
						if (e.getResource().fhirType().compareTo("Goal") == 0) {
							Goal g = (Goal) e.getResource();
							GoalSummary gs = mapper.fhir2summary(g, ctx);
							out.addSummary(gs);
						}
					}
					
					} catch (Exception exception) {
						logger.error(exception.getMessage());
					}
				}
			}

		}
		logger.error("END commonGoalSummary");
		return out;
	}

	   public Patient getPatient(  String id, IGenericClient client) {

	        Map<String, String> values = new HashMap<>();
	        values.put("id",id);
	        String callUrl = queryManager.setupQuery("Patient.Lookup", values);

	        if (callUrl != null) {
	            Patient fp = client.fetchResourceFromUrl(Patient.class, callUrl);
	            return fp;
 	        }
	      return null;
	    }
	   
	   
	   public Patient getPatientByName( String name,IGenericClient client)
	    {
		   try {
	        log.debug("Searching for patients , name = "+name);

	        Map<String, String> values = new HashMap<>();
	        values.put("name",   URLEncoder.encode(name, StandardCharsets.UTF_8));
	        String callUrl = queryManager.setupQuery("Patient.QueryByName", values);

	        if (callUrl != null) {
	        	
	        	
	            Bundle results = client.fetchResourceFromUrl(Bundle.class, callUrl);

	            for (Bundle.BundleEntryComponent e : results.getEntry()) {
	                if (e.getResource().fhirType().compareTo("Patient") == 0) {
	                    Patient bp = (Patient) e.getResource();
	                    return bp;

	                }
	            }
	        }

		   } catch (Exception exception) {
			   logger.error("Patient not found");
		   }
	    return null;

	    }
	
	
	@GetMapping("/goal")
	public GoalSummary[] getGoals(@RequestParam(required = true, name = "subject") String subjectId,
			@RequestHeader Map<String, String> headers, WebRequest webRequest) {
		ArrayList<GoalSummary> out = new ArrayList<>();
		FHIRServices fhirSrv = FHIRServices.getFhirServices();
		IGenericClient client = fhirSrv.getClient(headers);
		Map<String, String> values = new HashMap<>();
		String callUrl = queryManager.setupQuery("Goal.Query", values, webRequest);

		if (callUrl != null) {
			Bundle results = client.fetchResourceFromUrl(Bundle.class, callUrl);
			Context ctx = contextManager.setupContext(subjectId, client, mapper, headers);
			for (Bundle.BundleEntryComponent e : results.getEntry()) {
				if (e.getResource().fhirType().compareTo("Goal") == 0) {
					Goal g = (Goal) e.getResource();
					out.add(mapper.fhir2local(g, ctx));
				}
			}
			
			
			FHIRServerManager srvMgr = FHIRServerManager.getManager();

			FHIRServer srv = srvMgr.getFHIRIOServer();
			
			String identifierScope = null;
			if (headers.containsKey("mcc-fhir-server")) {
				identifierScope = headers.get("mcc-fhir-server");
			 
			} else {
				identifierScope = srvMgr.getFHIRIOServer().getBaseURL();
			}
			
			Patient ioPatient = getIOPatient( identifierScope, subjectId);
			if (ioPatient != null) {
				IGenericClient ioClient = fhirSrv.getIOClient();
				
				URLEncoder asdf;
				
				
				
				System.err.println("/Goal?subject="+URLEncoder.encode(ioPatient.getId(),StandardCharsets.UTF_8));
				  results = ioClient.fetchResourceFromUrl(Bundle.class, "/Goal?subject="+URLEncoder.encode(ioPatient.getId(),StandardCharsets.UTF_8));
					 
					for (Bundle.BundleEntryComponent e : results.getEntry()) {
						if (e.getResource().fhirType().compareTo("Goal") == 0) {
							Goal g = (Goal) e.getResource();
							out.add(mapper.fhir2local(g, ctx));
						}
					}
			}
			
			
		}
		GoalSummary[] outA = new GoalSummary[out.size()];
		outA = out.toArray(outA);
		return outA;
	}

	@GetMapping("/goal/{id}")
	public GoalSummary getGoal(@PathVariable(value = "id") String id, @RequestHeader Map<String, String> headers,
			WebRequest webRequest) {
		GoalSummary g;
		FHIRServices fhirSrv = FHIRServices.getFhirServices();
		IGenericClient client = fhirSrv.getClient(headers);
		Map<String, String> values = new HashMap<>();
		values.put("id", id);
		String callUrl = queryManager.setupQuery("Goal.Lookup", values, webRequest);

		if (callUrl != null) {
			Goal fg = client.fetchResourceFromUrl(Goal.class, callUrl);

			// Goal fg = client.read().resource(Goal.class).withId(id).execute();
			String subjectId = fg.getSubject().getId();
			Context ctx = contextManager.setupContext(subjectId, client, mapper, headers);
			g = mapper.fhir2local(fg, ctx);
		} else {
			// TODO: Return unavailable goal
			g = new GoalSummary();
			log.warn("Goal Lookup disabled, Goal " + id + " Not found");
		}
		return g;
	}
	
	Patient getIOPatient(String identifierScope, String patientId) {
		
		
		FHIRServices fhirSrv = FHIRServices.getFhirServices();
		IGenericClient ioClient = fhirSrv.getIOClient();
	
 
		Bundle ioPatientResult = ioClient.search().forResource(Patient.class)
				.where(Patient.IDENTIFIER.exactly().systemAndIdentifier(identifierScope, patientId))
				.returnBundle(Bundle.class).execute();
		logger.error("Start getIOPatient ioPatientResult.hasEntry() " + identifierScope + "  " +  patientId);
		if (ioPatientResult.hasEntry()) {
			return (Patient) ioPatientResult.getEntryFirstRep().getResource();
		} else {
			logger.error("Start getIOPatient no ioPatientResult.hasEntry() " + identifierScope + "  " +  patientId);
			ioPatientResult = ioClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
			for (BundleEntryComponent e : ioPatientResult.getEntry()) {
				Patient pe = (Patient) e.getResource();
				if (pe.hasIdentifier()) {
					
				 
					
					for (Identifier pidentifier : pe.getIdentifier()) {
						
						logger.error("Start getIOPatient hasIdentifier Loop " + pidentifier.getSystem() +pidentifier.getId());
						
						
						if (patientId.equals(pidentifier.getId())
								&& identifierScope.equals(pidentifier.getSystem())) {
						return pe;
						}

					}
				}

			}
		}
		logger.error("end getIOPatient" + identifierScope + "  " +  patientId);
		return null;
	}

	 private static Logger logger  = LoggerFactory.getLogger(GoalController.class); 
	@PostMapping("/creategoal")
	public String createGoal(@RequestHeader Map<String, String> headers, WebRequest webRequest,
			@RequestParam(required = false, name = "fhirRepository") String fhirRepository,
			@RequestParam(required = true, name = "patientId") String patientId, @RequestBody GoalSummary mccGoal) {

		if (StringUtils.isEmpty(mccGoal.getLifecycleStatus()) || mccGoal.getDescription() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					(mccGoal.getDescription() == null ? "Description Required" : "")
							+ (StringUtils.isEmpty(mccGoal.getLifecycleStatus()) ? "LifecycleStatus Required" : ""));
		}
		
		
		FHIRServerManager srvMgr = FHIRServerManager.getManager();

		FHIRServer srv = srvMgr.getFHIRIOServer();
		
		
		logger.error("creategoal");		
		String identifierScope = null;
		if (!StringUtils.isEmpty(mccGoal.getServer())) {
			identifierScope =mccGoal.getServer();
		 
		} else {
			identifierScope =srvMgr.getFHIRIOServer().getBaseURL();
		}
		
		FHIRServices.getFhirServices().getIOClient();
		
		logger.error("identifierScope "+identifierScope);		
		FHIRServices fhirSrv = FHIRServices.getFhirServices();
		IGenericClient ioClient = fhirSrv.getIOClient();
		IGenericClient smartClient = fhirSrv.getClient(headers);

		Map<String, String> values = new HashMap<>();
		values.put("id", URLEncoder.encode(patientId));
		String callUrl = queryManager.setupQuery("Patient.Lookup", values, webRequest);

		logger.error("callUrl "+callUrl);		
		Patient smartPatient = smartClient.fetchResourceFromUrl(Patient.class, callUrl);

	 
		
		Patient ioPatient = getIOPatient( identifierScope, patientId);

			if (ioPatient == null) {

				Patient newPatient = new Patient();
				newPatient.setName(smartPatient.getName());
				Identifier newidentifier = newPatient.addIdentifier();
				newidentifier.setSystem(identifierScope);
				newidentifier.setId(patientId);
				Reference patientreference = new Reference();
				patientreference.setId(patientId);
				newPatient.addLink().setOther(patientreference).setType(LinkType.SEEALSO);

				ioClient.create().resource(newPatient).execute();
				ioPatient = getIOPatient( identifierScope, patientId);

				
			}


		MethodOutcome result = ioClient.create().resource(GoalMapper.local2FHIR(ioPatient.getId(), mccGoal)).execute();
		IParser parser = ioClient.getFhirContext().newJsonParser();
		return parser.encodeResourceToString(result.getResource());
	}

}
