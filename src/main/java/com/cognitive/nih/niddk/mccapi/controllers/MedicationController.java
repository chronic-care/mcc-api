/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.cognitive.nih.niddk.mccapi.data.Context;
import com.cognitive.nih.niddk.mccapi.data.MccMedicationRecord;
import com.cognitive.nih.niddk.mccapi.data.MedicationLists;
import com.cognitive.nih.niddk.mccapi.data.MedicationSummaryList;
import com.cognitive.nih.niddk.mccapi.managers.ContextManager;
import com.cognitive.nih.niddk.mccapi.managers.QueryManager;
import com.cognitive.nih.niddk.mccapi.mappers.IR4Mapper;
import com.cognitive.nih.niddk.mccapi.services.FHIRServices;
import com.cognitive.nih.niddk.mccapi.util.FHIRHelper;
import org.hl7.fhir.r4.model.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class MedicationController {
    private final QueryManager queryManager;
    private final IR4Mapper mapper;
    private final ContextManager contextManager;

    public MedicationController(QueryManager queryManager, IR4Mapper mapper, ContextManager contextManager) {
        this.queryManager = queryManager;
        this.mapper = mapper;
        this.contextManager = contextManager;
        ;
    }

    private MedicationSummaryList commonMedicationSummary(String subjectId, String careplanId, Map<String, String> headers, WebRequest webRequest) {
        MedicationSummaryList out = new MedicationSummaryList();

        FHIRServices fhirSrv = FHIRServices.getFhirServices();
        IGenericClient client = fhirSrv.getClient(headers);
        Context ctx = contextManager.setupContext(subjectId, client, mapper, headers);
//        HashMap<String, String> carePlanMedicationsRequests = new HashMap<>();
//        getClanPlanMedReqIds(careplanId, carePlanMedicationsRequests, client, ctx);

        Map<String, String> values = new HashMap<>();
        String callUrl = queryManager.setupQuery("MedicationRequest.Query", values, webRequest);

        if (callUrl != null) {
            Bundle results = client.fetchResourceFromUrl(Bundle.class, callUrl);

            //Bundle results = client.search().forResource(MedicationRequest.class).where(MedicationRequest.SUBJECT.hasId(subjectId))
            //   .returnBundle(Bundle.class).execute();
            for (Bundle.BundleEntryComponent e : results.getEntry()) {
                if (e.getResource().fhirType().compareTo("MedicationRequest") == 0) {
                    MedicationRequest mr = (MedicationRequest) e.getResource();
                    out.addMedicationRequest(mr,  ctx);
                }
            }
        }
 
        return out;
    }

    private void xxxgetClanPlanMedReqIds(String careplanId, HashMap<String, String> carePlanMedicationsRequests, IGenericClient client, Context ctx) {
        if (careplanId != null) {
            String[] cps = careplanId.split(",");
            Map<String, String> values = new HashMap<>();

            // Fetch the careplan and grab medications
            for (String cpId : cps) {
                values.put("id", cpId);
                String callUrl = queryManager.setupQuery("CarePlan.Lookup", values);
                if (callUrl != null) {
                    CarePlan cp = client.fetchResourceFromUrl(CarePlan.class, callUrl);
                    if (cp != null) {
                        List<CarePlan.CarePlanActivityComponent> acp = cp.getActivity();
                        for (CarePlan.CarePlanActivityComponent a : acp) {
                            if (a.hasReference()) {
                                Reference ref = a.getReference();
                                if (FHIRHelper.isReferenceOfType(ref, "MedicationRequest")) {
                                    String key = ref.getReference();
                                    if (ref != null) {
                                        if (carePlanMedicationsRequests.containsKey(key)) {
                                            String val = carePlanMedicationsRequests.get(key);
                                            carePlanMedicationsRequests.put(key, val + "," + cpId);

                                        } else {
                                            carePlanMedicationsRequests.put(key, cpId);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @GetMapping("/medication")
    public MccMedicationRecord getMedication(@RequestParam(required = true, name = "type") String type, @RequestParam(required = true, name = "id") String id, @RequestHeader Map<String, String> headers, WebRequest webRequest) {

        FHIRServices fhirSrv = FHIRServices.getFhirServices();
        IGenericClient client = fhirSrv.getClient(headers);
        MccMedicationRecord out = null;

        Context ctx = contextManager.setupContext(null, client, mapper, headers);

        if (type.compareTo("MedicationRequest") == 0) {
            //DIRECT-FHIR-REF
            MedicationRequest mr = client.fetchResourceFromUrl(MedicationRequest.class, id);
            if (mr != null) {
                out = mapper.fhir2local(mr, ctx);
            }
        }  
        return out;
    }

    @GetMapping("/medicationlists")
    public MedicationLists getMedicationLists(@RequestParam(required = true, name = "subject") String subjectId, @RequestParam(required = false, name = "careplan") String careplanId, @RequestHeader Map<String, String> headers, WebRequest webRequest) {
        MedicationLists out = new MedicationLists();

        FHIRServices fhirSrv = FHIRServices.getFhirServices();
        IGenericClient client = fhirSrv.getClient(headers);
        Context ctx = contextManager.setupContext(subjectId, client, mapper, headers);

//        HashMap<String, String> carePlanMedicationsRequests = new HashMap<>();
//        getClanPlanMedReqIds(careplanId, carePlanMedicationsRequests, client, ctx);

        Map<String, String> values = new HashMap<>();
        String callUrl = queryManager.setupQuery("MedicationRequest.Query", values, webRequest);

        if (callUrl != null) {
            Bundle results = client.fetchResourceFromUrl(Bundle.class, callUrl);
            // Bundle results = client.search().forResource(MedicationRequest.class).where(MedicationRequest.SUBJECT.hasId(subjectId))
            //    .returnBundle(Bundle.class).execute();
            for (Bundle.BundleEntryComponent e : results.getEntry()) {
                if (e.getResource().fhirType().compareTo("MedicationRequest") == 0) {
                    MedicationRequest mr = (MedicationRequest) e.getResource();
                    out.addMedicationRequest(mr,  ctx);
                }
            }
        }

      
        return out;
    }

    @GetMapping("/summary/medications")
    public MedicationSummaryList getMedicationSummary(@RequestParam(required = true, name = "subject") String subjectId, @RequestParam(required = false, name = "careplan") String careplanId, @RequestHeader Map<String, String> headers, WebRequest webRequest) {
        return commonMedicationSummary(subjectId, careplanId, headers, webRequest);
    }

    /**
     * @param subjectId
     * @param careplanId
     * @param headers
     * @param webRequest
     * @return
     * @Depercated Please use /summary/medications
     */
    @GetMapping("/medicationsummary")
    public MedicationSummaryList getMedicationSummaryOld(@RequestParam(required = true, name = "subject") String subjectId, @RequestParam(required = false, name = "careplan") String careplanId, @RequestHeader Map<String, String> headers, WebRequest webRequest) {
        return commonMedicationSummary(subjectId, careplanId, headers, webRequest);
    }
}
