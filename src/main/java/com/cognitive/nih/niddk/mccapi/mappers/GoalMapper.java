package com.cognitive.nih.niddk.mccapi.mappers;

import com.cognitive.nih.niddk.mccapi.data.*;
import com.cognitive.nih.niddk.mccapi.data.primative.MccReference;
import com.cognitive.nih.niddk.mccapi.util.Helper;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Type;

import java.util.List;

public class GoalMapper {

    public static MccGoal fhir2local(Goal in, Context ctx)
    {
        MccGoal out = new MccGoal();
        out.setFHIRId(in.getIdElement().getIdPart());
        out.setId(in.getIdElement().getValue());
        out.setDescription(CodeableConceptMapper.fhir2local(in.getDescription(),ctx));
        out.setOutcomeCodes(CodeableConceptMapper.fhir2local(in.getOutcomeCode(),ctx));
        out.setAddresses(ReferenceMapper.fhir2local(in.getAddresses(),ctx));
        out.setCategories(CodeableConceptMapper.fhir2local(in.getCategory(),ctx));
        out.setCategorySummary(Helper.getConceptsAsDisplayString(in.getOutcomeCode()));
        out.setExpressedBy(ReferenceMapper.fhir2local(in.getExpressedBy(),ctx));
        out.setLifecycleStatus(in.getLifecycleStatus().toCode()); //Weird mapping
        out.setPriority(CodeableConceptMapper.fhir2local(in.getPriority(),ctx));
        out.setCategories(CodeableConceptMapper.fhir2local(in.getCategory(),ctx));

        out.setStatusDate(Helper.dateTimeToString(in.getStatusDate()));
        out.setStatusReason(in.getStatusReason());
        out.setNotes(Helper.AnnotationsToStringList(in.getNote()));


        if (in.hasStartCodeableConcept())
        {
            out.setUseStartConcept(true);
            out.setStartConcept(CodeableConceptMapper.fhir2local(in.getStartCodeableConcept(),ctx));
        }
        else
        {
            out.setUseStartConcept(false);
            out.setStartDateText(Helper.dateToString(in.getStartDateType().getValue()));
            out.setStartDate(GenericTypeMapper.fhir2local(in.getStartDateType(),ctx));
        }


        List<Goal.GoalTargetComponent> targets = in.getTarget();
        if (targets.size()>0)
        {
            int index=0;
            GoalTarget[] otargets = new GoalTarget[targets.size()];
            for (Goal.GoalTargetComponent t: targets) {
               otargets[index] = fhir2local(t,ctx);
               index++;
            }
            out.setTargets(otargets);
        }

        return out;
    }

    public static GoalSummary summaryfhir2local(Goal in, Context ctx)
    {
        GoalSummary out = new GoalSummary();
        out.setFHIRId(in.getIdElement().getIdPart());
        out.setDescription(in.getDescription().getText());
        out.setLifecycleStatus(in.getLifecycleStatus().toCode());
        Coding pcd = Helper.getCodingForSystem(in.getPriority(),"http://terminology.hl7.org/CodeSystem/goal-priority");
        out.setPriority(pcd==null?"Undefined":pcd.getCode());
        List<Goal.GoalTargetComponent> targets = in.getTarget();
        MccReference ref = ReferenceMapper.fhir2local(in.getExpressedBy(),ctx);
        out.setExpressedByType(ref.getType());
        out.setAchievementStatus(CodeableConceptMapper.fhir2local(in.getAchievementStatus(),ctx));
        if (in.hasStart())
        {
            if (in.hasStartCodeableConcept())
            {
                out.setStartDateText(in.getStartCodeableConcept().getText());
            }
            else if(in.hasStartDateType())
            {
                out.setStartDateText(Helper.dateToString(in.getStartDateType().getValue()));
            }
        }
        boolean needTargetDate = true;
        if (targets.size()>0)
        {
            int index=0;
            GoalTarget[] otargets = new GoalTarget[targets.size()];
            for (Goal.GoalTargetComponent t: targets) {
                if ( needTargetDate && t.hasDue())
                {
                    if (t.hasDueDateType())
                    {
                        out.setTargetDateText(Helper.dateToString(t.getDueDateType().getValue()));
                    }
                    else if (t.hasDueDuration())
                    {
                        out.setTargetDateText(Helper.DurationToString(t.getDueDuration()));
                    }
                    needTargetDate = false;
                }
                otargets[index] = fhir2local(t,ctx);
                index++;
            }
            out.setTargets(otargets);
        }
        return out;
    }

    public static GoalTarget fhir2local(Goal.GoalTargetComponent in, Context ctx)
    {
        GoalTarget out = new GoalTarget();
        out.setMeasure(CodeableConceptMapper.fhir2local(in.getMeasure(),ctx));
        Type x = in.getDue();
        in.getDetail();
        if (x != null)
        {
            out.setDueType(x.fhirType());
        }
        if (in.getDetail()!= null) {
            out.setValue(GenericTypeMapper.fhir2local(in.getDetail(), ctx));
        }
        return out;

    }
}