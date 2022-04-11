/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.mappers;

import com.cognitive.nih.niddk.mccapi.data.*;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCodeableConcept;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCoding;
import com.cognitive.nih.niddk.mccapi.data.primative.MccReference;
import com.cognitive.nih.niddk.mccapi.services.NameResolver;
import com.cognitive.nih.niddk.mccapi.util.FHIRHelper;
import com.cognitive.nih.niddk.mccapi.util.JavaHelper;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Goal.GoalLifecycleStatus;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

@Slf4j
@Component
public class GoalMapper implements IGoalMapper {

	public MccGoal fhir2local(Goal in, Context ctx) {
		MccGoal out = new MccGoal();
		IR4Mapper mapper = ctx.getMapper();
		out.setFHIRId(in.getIdElement().getIdPart());
		out.setId(in.getIdElement().getValue());
		out.setDescription(mapper.fhir2local(in.getDescription(), ctx));
		out.setOutcomeCodes(mapper.fhir2local(in.getOutcomeCode(), ctx));
		if (in.hasAddresses()) {
			out.setAddresses(mapper.fhir2local_referenceArray(in.getAddresses(), ctx));
		}
		out.setCategories(mapper.fhir2local(in.getCategory(), ctx));
		out.setCategorySummary(FHIRHelper.getConceptsAsDisplayString(in.getOutcomeCode()));
		if (in.hasExpressedBy()) {
			out.setExpressedBy(mapper.fhir2local(in.getExpressedBy(), ctx));
		}
		if (in.hasLifecycleStatus()) {
			out.setLifecycleStatus(in.getLifecycleStatus().toCode()); // Weird mapping
		} else {
			// Catch errors
			log.info("Goal violates US Core - Using a status of unknown");
			out.setLifecycleStatus("unknown");
		}
		out.setPriority(mapper.fhir2local(in.getPriority(), ctx));
		out.setCategories(mapper.fhir2local(in.getCategory(), ctx));

		out.setStatusDate(FHIRHelper.dateTimeToString(in.getStatusDate()));
		out.setStatusReason(in.getStatusReason());
		out.setNotes(FHIRHelper.annotationsToStringList(in.getNote(), ctx));

		if (in.hasStartCodeableConcept()) {
			out.setUseStartConcept(true);
			out.setStartConcept(mapper.fhir2local(in.getStartCodeableConcept(), ctx));
		} else {
			out.setUseStartConcept(false);
			out.setStartDateText(FHIRHelper.dateToString(in.getStartDateType().getValue()));
			out.setStartDate(mapper.fhir2local(in.getStartDateType(), ctx));
		}

		List<Goal.GoalTargetComponent> targets = in.getTarget();
		if (targets.size() > 0) {
			int index = 0;
			GoalTarget[] outTargets = new GoalTarget[targets.size()];
			for (Goal.GoalTargetComponent t : targets) {
				outTargets[index] = fhir2local(t, ctx);
				index++;
			}
			out.setTargets(outTargets);
		}

		// http://hl7.org/fhir/StructureDefinition/goal-acceptance
		List<Extension> acc = in.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/goal-acceptance");
		if (acc != null && acc.size() > 0) {
			Acceptance[] acceptances = new Acceptance[acc.size()];
			int cnt = 0;
			for (Extension e : acc) {
				Acceptance a = new Acceptance();
				List<Extension> i = e.getExtensionsByUrl("individual");
				if (!i.isEmpty()) {
					Base b = i.get(0).getValue();
					Reference r = b.castToReference(b);
					a.setIndividual(mapper.fhir2local(r, ctx));

				}
			
				
				List<Extension> s = e.getExtensionsByUrl("status");
				if (!s.isEmpty()) {
					Base b = s.get(0).getValue();
					a.setCode(b.castToCode(b).getValue());
				}
				List<Extension> p = e.getExtensionsByUrl("priority");
				if (!p.isEmpty()) {
					Base b = p.get(0).getValue();
					a.setPriority(mapper.fhir2local(b.castToCodeableConcept(b), ctx));
				}
				acceptances[cnt] = a;
				cnt++;
			}
			out.setAcceptance(acceptances);
		}
		return out;
	}

	public static Goal local2FHIR(String patientId, MccGoal in) {
		Goal out = new Goal();

		Reference subject = new Reference();

		subject.setDisplay("patient");
		subject.setReference("Patient/" + patientId);
		out.setSubject(subject);

		if (!StringUtils.isEmpty(in.getFHIRId())) {
			Identifier identifier = new Identifier();

			identifier.setId(in.getFHIRId());
			out.getIdentifier().add(identifier);
		}

		if (in.getDescription() != null) {
			CodeableConcept cc = new CodeableConcept();
			for (MccCoding mcccode : in.getDescription().getCoding()) {
				cc.addCoding().setCode(mcccode.getCode()).setSystem(mcccode.getSystem())
						.setDisplay(mcccode.getDisplay());
			}
			out.setDescription(cc);
		}

		if (in.getOutcomeCodes() != null) {
			for (MccCodeableConcept oc : in.getOutcomeCodes()) {
				CodeableConcept cc1 = out.addOutcomeCode();
				cc1.setText(oc.getText());
				for (MccCoding mccode : oc.getCoding()) {
					cc1.addCoding().setCode(mccode.getCode()).setSystem(mccode.getSystem())
							.setDisplay(mccode.getDisplay());
				}
			}
		}

		if (in.getAddresses() != null) {
			for (MccReference addr : in.getAddresses()) {
				out.addAddresses().setDisplay(addr.getDisplay()).setReference(addr.getReference())
						.setType(addr.getType());
			}
		}

		if (in.getCategories() != null) {
			for (MccCodeableConcept cat : in.getCategories()) {
				CodeableConcept cc1 = out.addCategory();
				cc1.setText(cat.getText());
				for (MccCoding mccode : cat.getCoding()) {
					cc1.addCoding().setCode(mccode.getCode()).setSystem(mccode.getSystem())
							.setDisplay(mccode.getDisplay());
				}
			}
		}

		if (in.getExpressedBy() != null) {
			Reference reference = new Reference();
			;
			reference.setDisplay(in.getExpressedBy().getDisplay());
			reference.setReference(in.getExpressedBy().getReference());
			out.setExpressedBy(reference);
		}

		if (!StringUtils.isEmpty(in.getLifecycleStatus())) {
			out.setLifecycleStatus(GoalLifecycleStatus.fromCode(in.getLifecycleStatus()));
		}

		if (in.getPriority() != null) {
			CodeableConcept cc1 = new CodeableConcept();
			cc1.setText(in.getPriority().getText());
			for (MccCoding mccode : in.getPriority().getCoding()) {
				cc1.addCoding().setCode(mccode.getCode()).setSystem(mccode.getSystem()).setDisplay(mccode.getDisplay());
			}
			out.setPriority(cc1);
		}

		if (!StringUtils.isEmpty(in.getStatusDate())) {
			out.setStatusDate(Date.valueOf(in.getStatusDate()));
		}

		if (!StringUtils.isEmpty(in.getStatusReason())) {
			out.setStatusReason(in.getStatusReason());
		}

		if (in.getNotes() != null) {
			for (String n : in.getNotes()) {
				out.addNote().setText(n);
			}
		}

		if (in.getStartConcept() != null) {
			CodeableConcept cc3 = new CodeableConcept();
			cc3.setText(in.getStartConcept().getText());
			for (MccCoding mccode : in.getStartConcept().getCoding()) {
				cc3.addCoding().setCode(mccode.getCode()).setSystem(mccode.getSystem()).setDisplay(mccode.getDisplay());
			}
			out.setStart(cc3);
		}

		if (in.getTargets() != null) {
			for (GoalTarget gt : in.getTargets()) {
				CodeableConcept cc4 = new CodeableConcept();
				cc4.setText(gt.getMeasure().getText());
				out.addTarget().setMeasure(cc4);
			}
		}

		if (in.getAcceptance() != null) {
			for (Acceptance acc2 : in.getAcceptance()) {
				CodeableConcept acc = new CodeableConcept();
				acc.setText(in.getStartConcept().getText());
				for (MccCoding mccode : acc2.getPriority().getCoding()) {
					acc.addCoding().setCode(mccode.getCode()).setSystem(mccode.getSystem())
							.setDisplay(mccode.getDisplay());
				}

				Extension extension = new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/goal-acceptance");
				extension.addExtension().setUrl("status").setValue(new CodeType("lang"));
				extension.addExtension().setUrl("priority").setValue(acc);
				out.getExtension().add(extension);

			}
		}

		return out;
	}

	public GoalSummary fhir2summary(Goal in, Context ctx) {
		GoalSummary out = new GoalSummary();
		IR4Mapper mapper = ctx.getMapper();
		if (in.getIdElement() != null) {
			out.setFHIRId(in.getIdElement().getIdPart());
		}
		if (in.getDescription() != null) {
			out.setDescription(in.getDescription().getText());
		}
		if (in.getLifecycleStatus() != null) {
			out.setLifecycleStatus(in.getLifecycleStatus().toCode());
		}
		Coding pcd = FHIRHelper.getCodingForSystem(in.getPriority(),
				"http://terminology.hl7.org/CodeSystem/goal-priority");
		out.setPriority(pcd == null ? "Undefined" : pcd.getCode());
		List<Goal.GoalTargetComponent> targets = in.getTarget();
		MccReference ref = mapper.fhir2local(in.getExpressedBy(), ctx);
		out.setExpressedByType(ref.getType());
		out.setAchievementStatus(mapper.fhir2local(in.getAchievementStatus(), ctx));
		if (in.hasAchievementStatus()) {
			out.setAchievementText(FHIRHelper.getConceptDisplayString(in.getAchievementStatus()));
		}
		if (in.hasStart()) {
			if (in.hasStartCodeableConcept()) {
				out.setStartDateText(in.getStartCodeableConcept().getText());
			} else if (in.hasStartDateType()) {
				out.setStartDateText(FHIRHelper.dateToString(in.getStartDateType().getValue()));
			}
		}
		boolean needTargetDate = true;
		if (targets.size() > 0) {
			int index = 0;
			GoalTarget[] outputTargets = new GoalTarget[targets.size()];
			for (Goal.GoalTargetComponent t : targets) {
				if (needTargetDate && t.hasDue()) {
					if (t.hasDueDateType()) {
						out.setTargetDateText(FHIRHelper.dateToString(t.getDueDateType().getValue()));
					} else if (t.hasDueDuration()) {
						out.setTargetDateText(FHIRHelper.durationToString(t.getDueDuration()));
					}
					needTargetDate = false;
				}
				outputTargets[index] = fhir2local(t, ctx);
				index++;
			}
			out.setTargets(outputTargets);
		}
		if (in.hasAddresses()) {
			out.setAddresses(NameResolver.getReferenceNames(in.getAddresses(), ctx));
		}

		if (in.hasExpressedBy()) {
			out.setExpressedBy(NameResolver.getReferenceName(in.getExpressedBy(), ctx));
		}

		// http://hl7.org/fhir/StructureDefinition/goal-acceptance
		List<Extension> acc = in.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/goal-acceptance");
		if (acc != null && acc.size() > 0) {
			StringBuilder val = new StringBuilder();
			int cnt = 0;
			for (Extension e : acc) {

				List<Extension> i = e.getExtensionsByUrl("individual");
				Base b = i.get(0).getValue();
				Reference r = b.castToReference(b);
				JavaHelper.addStringToBufferWithSep(val, NameResolver.getReferenceName(r, ctx), ",");
				List<Extension> s = e.getExtensionsByUrl("status");
				if (s != null && s.size() > 0) {
					b = s.get(0).getValue();
					// agree, disagree, pending - Exceptions to agree will be output.
					String agree = (b.castToCode(b).getValue());
					if (agree.compareTo("agree") != 0) {
						val.append(" (");
						val.append(agree);
						val.append(")");
					}
				}
			}
			out.setAcceptance(val.toString());
		}
		return out;
	}

	public GoalTarget fhir2local(Goal.GoalTargetComponent in, Context ctx) {
		GoalTarget out = new GoalTarget();
		IR4Mapper mapper = ctx.getMapper();
		out.setMeasure(mapper.fhir2local(in.getMeasure(), ctx));
		Type x = in.getDue();
		if (x != null) {
			out.setDueType(x.fhirType());
		}
		if (in.getDetail() != null) {
			out.setValue(mapper.fhir2local(in.getDetail(), ctx));
		}
		return out;

	}
}
