/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.data;

import com.cognitive.nih.niddk.mccapi.mappers.IMedicationMapper;
import com.cognitive.nih.niddk.mccapi.mappers.MedicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.MedicationRequest;
 

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class MedicationLists {
    @NotBlank
    private ArrayList<MccMedicationRecord> activeMedications;
    @NotBlank
    private ArrayList<MccMedicationRecord> inactiveMedications;
    private HashMap<String, MccMedicationRecord> actMedConflictMap;

    private static final int ACTIVE_LIST = 0;
    private static final int INACTIVE_LIST = 1;
    private static final int IGNORE = 2;



    private static HashMap<String,Integer> activeMedReqKeys = new HashMap<>();
    private static HashMap<String,Integer> activeMedStmtKeys = new HashMap<>();

    static {
        //Hash as verified
        Integer active = Integer.valueOf(ACTIVE_LIST);
        Integer inactive = Integer.valueOf(INACTIVE_LIST);
        Integer ignore = Integer.valueOf(IGNORE);

        //Medication Request Status: 	active | on-hold | cancelled | completed | entered-in-error | stopped | draft | unknown
        //
        activeMedReqKeys.put("active",active);
        activeMedReqKeys.put("on-hold",inactive);
        activeMedReqKeys.put("cancelled",inactive);
        activeMedReqKeys.put("completed",inactive);
        activeMedReqKeys.put("entered-in-error",ignore);
        activeMedReqKeys.put("stopped",inactive);
        activeMedReqKeys.put("unknown",inactive);

        //Medication Statement Status: 	active | completed | entered-in-error | intended | stopped | on-hold | unknown | not-taken
        activeMedStmtKeys.put("active",active);
        activeMedStmtKeys.put("completed",inactive);
        activeMedStmtKeys.put("entered-in-error",ignore);
        activeMedStmtKeys.put("intended",inactive);
        activeMedStmtKeys.put("stopped",inactive);
        activeMedStmtKeys.put("on-hold",inactive);
        activeMedStmtKeys.put("unknown",inactive);
        activeMedStmtKeys.put("not-taken",inactive);

    }

    public MedicationLists()
    {
        activeMedications = new ArrayList<>();
        inactiveMedications = new ArrayList<>();
        //
        //medications = new HashMap<>();
    }

    public MccMedicationRecord[] getActiveMedications() {

        MccMedicationRecord[] out = new MccMedicationRecord[activeMedications.size()];
        return activeMedications.toArray(out);
    }


    public MccMedicationRecord[] getInactiveMedications() {

        MccMedicationRecord[] out = new MccMedicationRecord[inactiveMedications.size()];
        return inactiveMedications.toArray(out);
    }

   

    public void addMedicationRequest(MedicationRequest mreq,    Context ctx)
    {
        MccMedicationRecord mr = ctx.getMapper().fhir2local(mreq,ctx);
//        if (cpRefs.containsKey(mr.getFhirId()))
//        {
//            mr.setOnCareplans(cpRefs.get(mr.getFhirId()));
//        }
        String status = mr.getStatus();
        Integer s = activeMedReqKeys.get(status);
        if (s != null) {
            int active = s.intValue();

            switch (active) {
                case ACTIVE_LIST: {
                    activeMedications.add(mr);
                    break;
                }
                case INACTIVE_LIST: {
                    inactiveMedications.add(mr);
                    break;
                }
                case IGNORE: {
                    log.debug("Ignoring status ");
                    break;
                }
                default: {
                    log.debug("Code error - Unhandled status swithc");
                }
            }
        }
        else
        {
            log.warn("Unknown Medication Status: "+status);
        }

    }

    private boolean duplicateAndConflictCheck(MccMedicationRecord mr)
    {
        // Returns true id the recorded should be considered a duplicate.
        // When a conflict is detected it will return false, but the conflicting resources will be updated


        // Get the code of medication
        // If not null
        //    Check if we have seen this code before?
        //       if yes - Does the dosage match?
        //          No - Then
        return false;
    }


}
