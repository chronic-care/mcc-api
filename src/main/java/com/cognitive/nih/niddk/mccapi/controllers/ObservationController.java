package com.cognitive.nih.niddk.mccapi.controllers;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.cognitive.nih.niddk.mccapi.data.Context;
import com.cognitive.nih.niddk.mccapi.data.MccObservation;
import com.cognitive.nih.niddk.mccapi.data.MccValueSet;
import com.cognitive.nih.niddk.mccapi.data.primative.GenericType;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCodeableConcept;
import com.cognitive.nih.niddk.mccapi.exception.ItemNotFoundException;
import com.cognitive.nih.niddk.mccapi.managers.ContextManager;
import com.cognitive.nih.niddk.mccapi.managers.QueryManager;
import com.cognitive.nih.niddk.mccapi.managers.ValueSetManager;
import com.cognitive.nih.niddk.mccapi.mappers.IR4Mapper;
import com.cognitive.nih.niddk.mccapi.services.FHIRServices;
import com.cognitive.nih.niddk.mccapi.util.MccHelper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class ObservationController {
    private final QueryManager queryManager;
    private final IR4Mapper ir4Mapper;

    private static HashMap<String,String> revPanelMap = new HashMap<>();


    static {
        //Blood Pressure
        revPanelMap.put("8462-4","85354-9");
        revPanelMap.put("8480-6","85354-9");
    }
    public ObservationController(QueryManager queryManager, IR4Mapper ir4Mapper) {
        this.queryManager = queryManager;
        this.ir4Mapper = ir4Mapper;
    }

    private ArrayList<MccObservation> QueryObservations(String baseQuery, String mode, IGenericClient client, String subjectId, String sortOrder, WebRequest webRequest, Map<String, String> headers, Map<String, String> values) {
        ArrayList<MccObservation> out = new ArrayList<>();
        List<String> calls = getQueryStrings(baseQuery, mode);

        if (calls.size() > 0) {
            Context ctx = ContextManager.getManager().findContextForSubject(subjectId, headers);
            ctx.setClient(client,ir4Mapper);

            for (String key : calls) {
                String callUrl = queryManager.setupQuery(key, values, webRequest);
                Bundle results = client.fetchResourceFromUrl(Bundle.class, callUrl);
                //In general the we expect the return value to be in descending date order

                for (Bundle.BundleEntryComponent e : results.getEntry()) {
                    if (e.getResource().fhirType().compareTo("Observation") == 0) {
                        Observation o = (Observation) e.getResource();
                        out.add(ir4Mapper.fhir2local(o, ctx));
                    }
                }
            }
            //Now we need possibly to sort the output
            if (sortOrder.compareTo("ascending") == 0) {
                //We need ascending order
                out.sort((MccObservation o1, MccObservation o2) -> o1.getEffective().compareTo(o2.getEffective()));
            } else if (calls.size() > 1) {
                Comparator<MccObservation> comparator = (MccObservation o1, MccObservation o2) -> o1.getEffective().compareTo(o2.getEffective());

                //We need to merge multiples into one and sort descending
                out.sort(comparator.reversed());
            }

        } else {
            //TODO: Deal with suppressed query return
            log.info(baseQuery + " suppressed by override");
        }
        return out;
    }

    /**
     * Creates a compound query key
     *
     * @param query the Query Name
     * @param mode  the Mode (code, combo, panel, component)
     * @return A compound key
     */
    private String getKey(String query, String mode) {
        return query + "." + mode;
    }

    @GetMapping("/find/latest/observation")
    public MccObservation getLatestObservation(@RequestParam(required = true, name = "subject") String subjectId, @RequestParam(required = true, name = "code") String code,  @RequestParam(name = "mode", defaultValue = "code") String mode, @RequestParam(name = "translate", defaultValue = "false") String translate, @RequestHeader Map<String, String> headers, WebRequest webRequest) {

        ArrayList<MccObservation> list = new ArrayList<>();
        //Implement a mode that will translate a single code to it's containing value set
        if (translate.toLowerCase().compareTo("true")==0)
        {
            //For now we only support 1 code
            ArrayList<String> vs;
            if (code.contains("|")) {
                String[] parts = code.split("|");
                vs = ValueSetManager.getValueSetManager().findCodesValuesSets(parts[0],parts[1]);
            }
            else
            {
                //Default to loinc
                vs = ValueSetManager.getValueSetManager().findCodesValuesSets("http://loinc.org", code);
            }
            if (vs != null && vs.size()>0)
            {
                MccValueSet valueSetObj = ValueSetManager.getValueSetManager().findValueSet(vs.get(0));
                code = valueSetObj.asQueryString();
            }
            else if (revPanelMap.containsKey(code))
            {
                code = revPanelMap.get(code);
            }


        }

        FHIRServices fhirSrv = FHIRServices.getFhirServices();
        IGenericClient client = fhirSrv.getClient(headers);
        Map<String, String> values = new HashMap<>();
        values.put("code",code);
        String baseQuery = "Observation.QueryLatest";
        list = this.QueryObservations(baseQuery, mode, client, subjectId, "descending", webRequest, headers, values);

        if (list.size() == 0) {
            //throw new ItemNotFoundException(code);
            return notFound(code);
        }

        return list.get(0);


    }


    @GetMapping("/observations")
    public MccObservation[] getObservation(@RequestParam(required = true, name = "subject") String subjectId, @RequestParam(required = true, name = "code") String code, @RequestParam(name = "count", defaultValue = "100") int maxItems, @RequestParam(name = "sort", defaultValue = "ascending") String sortOrder, @RequestParam(name = "mode", defaultValue = "code") String mode, @RequestHeader Map<String, String> headers, WebRequest webRequest) {


        FHIRServices fhirSrv = FHIRServices.getFhirServices();
        IGenericClient client = fhirSrv.getClient(headers);

        Map<String, String> values = new HashMap<>();
        values.put("count", Integer.toString(maxItems));
        String baseQuery = "Observation.Query";
        List<MccObservation> out = this.QueryObservations(baseQuery, mode, client, subjectId, sortOrder, webRequest, headers, values);

        if (out.size()>maxItems)
        {
            out = out.subList(0,maxItems-1);
        }
        MccObservation[] outA = new MccObservation[out.size()];
        outA = out.toArray(outA);
        return outA;
    }

    @GetMapping("/observationsbyvalueset")
    public MccObservation[] getObservationsByValueSet(@RequestParam(required = true, name = "subject") String subjectId, @RequestParam(required = true, name = "valueset") String valueset, @RequestParam(name = "max", defaultValue = "100") int maxItems, @RequestParam(name = "sort", defaultValue = "ascending") String sortOrder, @RequestParam(name = "mode", defaultValue = "code") String mode, @RequestHeader Map<String, String> headers, WebRequest webRequest) {

        List<MccObservation> out = new ArrayList<>();

        FHIRServices fhirSrv = FHIRServices.getFhirServices();
        IGenericClient client = fhirSrv.getClient(headers);


        //Right now this search is using the local expanded version of the value set
        //If the server supports code:in then it would be better to use that feature
        MccValueSet valueSetObj = ValueSetManager.getValueSetManager().findValueSet(valueset);
        if (valueSetObj != null) {

            Map<String, String> values = new HashMap<>();
            values.put("codes", valueSetObj.asQueryString());
            values.put("count", Integer.toString(maxItems));
            String baseQuery = "Observation.QueryValueSetExpanded";
            out = this.QueryObservations(baseQuery, mode, client, subjectId, sortOrder, webRequest, headers, values);

        } else {
            throw new ItemNotFoundException("No such valaue set: " + valueset);

        }

        if (out.size()>maxItems)
        {
            out = out.subList(0,maxItems-1);
        }
        MccObservation[] outA = new MccObservation[out.size()];
        outA = out.toArray(outA);
        return outA;
    }

    /**
     * Finds the queries that will match the request, for most modes this will be a single item
     *
     * @param query the Query Name
     * @param mode  the Mode (code, combo, panel, component)
     * @return a list of query keys
     */
    private List<String> getQueryStrings(String query, String mode) {
        //Normalize the mode (Combo, Code, Panel, Component)
        mode = mode.toLowerCase();
        //Prep Output
        ArrayList<String> out = new ArrayList<>();
        String key;
        //Search for matches
        if (mode.equals("combo")) {
            key = getKey(query, "combo");
            if (queryManager.doesQueryExist(key) == false) {
                key = getKey(query, "code");
                if (queryManager.doesQueryExist(key)) {
                    out.add(key);
                }
                key = getKey(query, "component");
                if (queryManager.doesQueryExist(key)) {
                    out.add(key);
                }
            } else {
                out.add(key);
            }

        } else {
            key = getKey(query, mode);
            if (queryManager.doesQueryExist(key)) {
                out.add(key);
            }
        }
        return out;
    }

    private MccObservation notFound(String code)
    {
        MccObservation out = new MccObservation();
        out.setFHIRId("notfound");
        out.setStatus("notfound");
        MccCodeableConcept cc = MccHelper.conceptFromCode(code,null);
        out.setCode(cc);
        GenericType value = MccHelper.genericFromString("No Data Available");
        out.setValue(value);
        return out;
    }
}
