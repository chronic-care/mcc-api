/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.mappers;

import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Duration;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.SampledData;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Timing;
import org.springframework.stereotype.Component;

import com.cognitive.nih.niddk.mccapi.data.Context;
import com.cognitive.nih.niddk.mccapi.data.MccObservation;
import com.cognitive.nih.niddk.mccapi.data.ObservationComponent;
import com.cognitive.nih.niddk.mccapi.data.QuestionnaireResponseItem;
import com.cognitive.nih.niddk.mccapi.data.QuestionnaireResponseItemAnswer;
import com.cognitive.nih.niddk.mccapi.data.ReferenceRange;
import com.cognitive.nih.niddk.mccapi.data.SimpleQuestionnaireItem;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCoding;
import com.cognitive.nih.niddk.mccapi.data.primative.MccDateTime;
import com.cognitive.nih.niddk.mccapi.data.primative.MccReference;
import com.cognitive.nih.niddk.mccapi.util.FHIRHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ObservationMapper implements IObservationMapper {

	public MccObservation fhir2local(Observation in, Context ctx) {
		MccObservation out = new MccObservation();
		IR4Mapper mapper = ctx.getMapper();

		out.setFHIRId(in.getIdElement().getIdPart());
		out.setCode(mapper.fhir2local(in.getCode(), ctx));
		if (in.hasBasedOn()) {
			out.setBasedOn(mapper.fhir2local_referenceArray(in.getBasedOn(), ctx));
		}
		out.setStatus(in.getStatus().toCode());
		if (in.hasEffective()) {
			MccObservation.Effective effective = out.defineEffective();

			if (in.hasEffectiveDateTimeType()) {
				effective.setDateTime(mapper.fhir2local(in.getEffectiveDateTimeType(), ctx));
			} else if (in.hasEffectiveTiming()) {
				effective.setTiming(mapper.fhir2local(in.getEffectiveTiming(), ctx));
			} else if (in.hasEffectiveInstantType()) {
				effective.setInstant(mapper.fhir2local(in.getEffectiveInstantType(), ctx));
			} else if (in.hasEffectivePeriod()) {
				effective.setPeriod(mapper.fhir2local(in.getEffectivePeriod(), ctx));
			}
		} else {
			out.defineEffective();
		}
		if (in.hasValue()) {
			out.setValue(mapper.fhir2local(in.getValue(), ctx));
		}
		if (in.hasNote()) {
			out.setNote(FHIRHelper.annotationsToString(in.getNote(), ctx));

		}
		if (in.hasReferenceRange()) {
			out.setReferenceRanges(fhir2local(in.getReferenceRange(), ctx));
		}
		if (in.hasComponent()) {
			List<Observation.ObservationComponentComponent> comps = in.getComponent();
			ObservationComponent[] ocomps = new ObservationComponent[comps.size()];
			int i = 0;
			for (Observation.ObservationComponentComponent comp : comps) {
				ocomps[i] = fhir2local(comp, ctx);
				i++;
			}
			out.setComponents(ocomps);
		}
		if (in.hasDataAbsentReason()) {
			out.setDataAbsentReason(mapper.fhir2local(in.getDataAbsentReason(), ctx));
		}
		if (in.hasCategory()) {
			out.setCategory(mapper.fhir2local(in.getCategory(), ctx));
		}
		return out;
	}

	
	
	 
	static public Observation local2fhir(String patientId,MccObservation in) {
		Observation out = new Observation();
		
		Reference subject = new Reference();

		subject.setDisplay("patient");
		subject.setReference("Patient/" + patientId);
		out.setSubject(subject);
		 

		Identifier identifier = new Identifier();

		identifier.setId(in.getFHIRId());
		out.getIdentifier().add(identifier);

		CodeableConcept cc = new CodeableConcept();
		for (MccCoding mcccode : in.getCode().getCoding()) {
			cc.addCoding().setCode(mcccode.getCode()).setSystem(mcccode.getSystem()).setDisplay(mcccode.getDisplay());
		}

		for (MccReference mccbasedon : in.getBasedOn()) {
			out.addBasedOn().setDisplay(mccbasedon.getDisplay()).setReference(mccbasedon.getReference())
					.setType(mccbasedon.getType());
		}

		out.setStatus(ObservationStatus.fromCode(in.getStatus()));

		if (in.defineEffective() != null) {

			if (in.defineEffective().getDateTime() != null) {
				DateTimeType datetime = new DateTimeType();
				datetime.setValueAsString(in.defineEffective().getDateTime().getDate());
				out.setEffective(datetime);
			}

			if (in.defineEffective().getInstant() != null) {
				InstantType instant = new InstantType();
				instant.setValueAsString(in.defineEffective().getInstant().getValue());
				out.setEffective(instant);

			}

			if (in.defineEffective().getPeriod() != null) {
				Period period = new Period();
				period.setStart(in.defineEffective().getPeriod().getStart().getRawDate());
				period.setEnd(in.defineEffective().getPeriod().getEnd().getRawDate());
				out.setEffective(period);

			}

			if (in.defineEffective().getTiming() != null) {
				Timing timing = new Timing();
				for (MccDateTime event : in.defineEffective().getTiming().getEvent()) {
					timing.addEvent(event.getRawDate());
				}
				out.setEffective(timing);

			}
		}

//        if (in.hasEffective())
//        {
//            MccObservation.Effective effective = out.defineEffective();
//
//            if (in.hasEffectiveDateTimeType())
//            {
//                effective.setDateTime(mapper.fhir2local(in.getEffectiveDateTimeType(),ctx));
//            }
//            else if (in.hasEffectiveTiming())
//            {
//                effective.setTiming(mapper.fhir2local(in.getEffectiveTiming(),ctx));
//            }
//            else if (in.hasEffectiveInstantType())
//            {
//                effective.setInstant(mapper.fhir2local(in.getEffectiveInstantType(),ctx));
//            }
//            else if (in.hasEffectivePeriod())
//            {
//                effective.setPeriod(mapper.fhir2local(in.getEffectivePeriod(),ctx));
//            }
//        }
		if (in.getValue() != null) {

			if (in.getValue().getStringValue() != null) {
				StringType typeValue = new StringType(in.getValue().getStringValue());
				out.setValue(typeValue);
			}
			if (in.getValue().getIntegerValue() != null) {
				IntegerType typeValue = new IntegerType(in.getValue().getStringValue());
				out.setValue(typeValue);
			}
			if (in.getValue().getBooleanValue() != null) {
				BooleanType typeValue = new BooleanType(in.getValue().getStringValue());
				out.setValue(typeValue);
			}
			if (in.getValue().getIdValue() != null) {
				IdType typeValue = new IdType(in.getValue().getStringValue());
			}
			if (in.getValue().getCodeableConceptValue() != null) {
				CodeableConcept typeValue = new CodeableConcept();
				for (MccCoding mcccoding : in.getValue().getCodeableConceptValue().getCoding()) {
					typeValue.addCoding().setCode(mcccoding.getCode()).setDisplay(mcccoding.getDisplay())
							.setSystem(mcccoding.getSystem());
				}
				out.setValue(typeValue);

			}
			if (in.getValue().getQuantityValue() != null) {
				Quantity typeValue = new Quantity();
				typeValue.setValue(Double.valueOf(in.getValue().getStringValue()));
				out.setValue(typeValue);
			}
			if (in.getValue().getRangeValue() != null) {
				Range typeValue = new Range();
				Quantity low = new Quantity();
				low.setValue(in.getValue().getRangeValue().getLow().getValue());
				typeValue.setLow(low);
				Quantity high = new Quantity();
				high.setValue(in.getValue().getRangeValue().getLow().getValue());
//				typeValue.setLow(low);
				typeValue.setHigh(high);
				out.setValue(typeValue);
			}
			if (in.getValue().getRatioValue() != null) {
				Ratio typeValue = new Ratio();

				Quantity d = new Quantity();
				d.setValue(in.getValue().getRatioValue().getDenominator().getValue());
				typeValue.setDenominator(d);

				Quantity n = new Quantity();
				n.setValue(in.getValue().getRatioValue().getDenominator().getValue());

				typeValue.setNumerator(n);
				out.setValue(typeValue);
			}
			if (in.getValue().getPeriodValue() != null) {
				Period typeValue = new Period();
				typeValue.setStart(in.getValue().getPeriodValue().getStart().getRawDate());				
				typeValue.setEnd(in.getValue().getPeriodValue().getEnd().getRawDate());				
				out.setValue(typeValue);
			}
			if (in.getValue().getDateValue() != null) {
				DateType typeValue = new DateType();				
				typeValue.setValue(in.getValue().getDateValue().getRawDate());
				out.setValue(typeValue);
			}
			if (in.getValue().getTimeValue() != null) {
				TimeType typeValue = new TimeType();
				typeValue.setValue(in.getValue().getTimeValue().getValue());				
				out.setValue(typeValue);
			}
			if (in.getValue().getDateTimeValue() != null) {
				DateTimeType typeValue = new DateTimeType();
				typeValue.setValue(in.getValue().getDateTimeValue().getRawDate());
				out.setValue(typeValue);
			}
			if (in.getValue().getSampledDataValue() != null) {
				SampledData typeValue = new SampledData();
//				typeValue.et
				out.setValue(typeValue);
			}
			if (in.getValue().getDurationValue() != null) {
				Duration typeValue = new Duration();
				out.setValue(typeValue);
			}
			if (in.getValue().getTimingValue() != null) {
				Timing typeValue = new Timing();
				out.setValue(typeValue);
			}
			if (in.getValue().getInstantValue() != null) {
				InstantType typeValue = new InstantType();
				out.setValue(typeValue);
			}
			if (in.getValue().getIdentiferValue() != null) {
				Identifier typeValue = new Identifier();
				out.setValue(typeValue);
			}
			if (in.getValue().getCodingValue() != null) {
				Coding typeValue = new Coding();
				out.setValue(typeValue);
			}
			if (in.getValue().getDecimalValue() != null) {
				DecimalType typeValue = new DecimalType();
				out.setValue(typeValue);
			}

		}

		
		if (in.getNote() != null) {
			out.addNote().setText(in.getNote());
		}
//		if (in.hasReferenceRange()) {
//			out.setReferenceRanges(fhir2local(in.getReferenceRange(), ctx));
//		}
//		if (in.hasComponent()) {
//			List<Observation.ObservationComponentComponent> comps = in.getComponent();
//			ObservationComponent[] ocomps = new ObservationComponent[comps.size()];
//			int i = 0;
//			for (Observation.ObservationComponentComponent comp : comps) {
//				ocomps[i] = fhir2local(comp, ctx);
//				i++;
//			}
//			out.setComponents(ocomps);
//		}
//		if (in.hasDataAbsentReason()) {
//			out.setDataAbsentReason(mapper.fhir2local(in.getDataAbsentReason(), ctx));
//		}
//		if (in.hasCategory()) {
//			out.setCategory(mapper.fhir2local(in.getCategory(), ctx));
//		}
		 
		return out;
	}

	public ObservationComponent fhir2local(Observation.ObservationComponentComponent in, Context ctx) {
		ObservationComponent out = new ObservationComponent();
		IR4Mapper mapper = ctx.getMapper();

		if (in.hasCode()) {
			out.setCode(mapper.fhir2local(in.getCode(), ctx));
		}
		if (in.hasDataAbsentReason()) {
			out.setDataAbsentReason(mapper.fhir2local(in.getDataAbsentReason(), ctx));
		}
		if (in.hasValue()) {
			out.setValue(mapper.fhir2local(in.getValue(), ctx));
		}
		if (in.hasReferenceRange()) {
			out.setReferenceRanges(fhir2local(in.getReferenceRange(), ctx));
		}
		if (in.hasInterpretation()) {
			out.setInterpretation(mapper.fhir2local(in.getInterpretation(), ctx));
		}
		return out;
	}

	public ReferenceRange fhir2local(Observation.ObservationReferenceRangeComponent in, Context ctx) {
		ReferenceRange out = new ReferenceRange();
		IR4Mapper mapper = ctx.getMapper();

		if (in.hasLow()) {
			out.setLow(mapper.fhir2local((Quantity) in.getLow(), ctx));
		}
		if (in.hasHigh()) {
			out.setLow(mapper.fhir2local((Quantity) in.getHigh(), ctx));
		}
		if (in.hasType()) {
			out.setType(mapper.fhir2local(in.getType(), ctx));
		}
		if (in.hasAppliesTo()) {
			out.setAppliesTo(mapper.fhir2local(in.getAppliesTo(), ctx));
		}
		if (in.hasAge()) {
			out.setAge(mapper.fhir2local(in.getAge(), ctx));
		}
		out.setText(in.getText());

		return out;
	}

	public ReferenceRange[] fhir2local(List<Observation.ObservationReferenceRangeComponent> in, Context ctx) {
		ReferenceRange[] out = new ReferenceRange[in.size()];
		int i = 0;
		for (Observation.ObservationReferenceRangeComponent rr : in) {
			out[i] = fhir2local(rr, ctx);
			i++;
		}
		return out;

	}

	public SimpleQuestionnaireItem fhir2SimpleItem(Observation in, Context ctx, String linkId) {
		SimpleQuestionnaireItem out = new SimpleQuestionnaireItem();
		IR4Mapper mapper = ctx.getMapper();
		out.setType("Observation");
		out.setFHIRId(in.getIdElement().getIdPart());
		if (in.hasEffectiveDateTimeType()) {
			Date eftDate = in.getEffectiveDateTimeType().getValue();
			out.setAuthored(mapper.fhir2local(eftDate, ctx));
		}
		QuestionnaireResponseItem item = new QuestionnaireResponseItem();
		out.setItem(item);
		item.setLinkid(linkId);
		if (in.hasValue()) {
			QuestionnaireResponseItemAnswer[] answers = new QuestionnaireResponseItemAnswer[1];
			answers[0] = new QuestionnaireResponseItemAnswer();
			answers[0].setValue(ctx.getMapper().fhir2local(in.getValue(), ctx));
			item.setAnswers(answers);
		}
		if (in.hasComponent()) {
			List<Observation.ObservationComponentComponent> components = in.getComponent();
			QuestionnaireResponseItem[] items = new QuestionnaireResponseItem[components.size()];
			int i = 0;
			// Handle components
			for (Observation.ObservationComponentComponent c : components) {
				QuestionnaireResponseItem subItem = new QuestionnaireResponseItem();
				if (c.hasCode()) {
					subItem.setLinkid(c.getCode().getCodingFirstRep().getCode());
				}
				if (c.hasValue()) {
					QuestionnaireResponseItemAnswer[] answers = new QuestionnaireResponseItemAnswer[1];
					answers[0] = new QuestionnaireResponseItemAnswer();
					answers[0].setValue(ctx.getMapper().fhir2local(in.getValue(), ctx));
					subItem.setAnswers(answers);
				}
				i++;
			}
			item.setItems(items);
		}
		return out;
	}

}
