package com.congitive.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.cognitive.nih.niddk.mccapi.MccApiApplication;
import com.cognitive.nih.niddk.mccapi.data.Acceptance;
import com.cognitive.nih.niddk.mccapi.data.GoalSummary;
import com.cognitive.nih.niddk.mccapi.data.GoalTarget;
 
import com.cognitive.nih.niddk.mccapi.data.primative.MccCodeableConcept;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCoding;
import com.cognitive.nih.niddk.mccapi.data.primative.MccReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.CodeDt;


//@RunWith(SpringRunner.class)

//@SpringBootTest(classes = MccApiApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
//@TestPropertySource(properties = { "FHIR_SERVER=https://gw.interop.community/SyntheaTest8/open", "FHIRIO_SERVER=https://api.logicahealth.org/MCCSupplement2/open", "SECONDARY_SERVER=https://gw.interop.community/FHRIIOTest/open" })
public class TestCreateGoal {

	@Autowired
	private TestRestTemplate template;

	
	private static MccCodeableConcept createMccCodeableConcept(String text) {
		MccCodeableConcept description = new MccCodeableConcept();
		description.setText(text);
		MccCoding[] coding = new MccCoding[1];
		coding[0] = new MccCoding();
		coding[0].setDisplay(text);
		description.setCoding(coding);
		return description;
	}

	@Test
	public void testCreateGoal() throws JsonProcessingException {
		GoalSummary mccGoal = new GoalSummary();
		mccGoal.setDescription("description");

		MccCodeableConcept[] xx = new MccCodeableConcept[1];
		xx[0] = createMccCodeableConcept("outcomes");
		mccGoal.setOutcomeCodes(xx);

		MccReference[] yy = new MccReference[1];
		yy[0] = new MccReference();
		yy[0].setDisplay("addressses");
//		mccGoal.setAddresses("addressses");

		MccCodeableConcept[] zz = new MccCodeableConcept[1];
		zz[0] = createMccCodeableConcept("categories");
		mccGoal.setCategories(zz);
		mccGoal.setLifecycleStatus("proposed");
		mccGoal.setPriority("high-priority");
		mccGoal.setStatusDate("2022-01-01");
		String[] nnn = new String[1];
		nnn[0] = new String("notes");
		mccGoal.setNotes(nnn);
//		mccGoal.setStartConcept(createMccCodeableConcept("StartConcept"));
		mccGoal.setStartDateText("2002");
		GoalTarget[] ggg = new GoalTarget[1];
		ggg[0] = new GoalTarget();
		ggg[0].setDueAsText("DUEASTTEXT");
		ggg[0].setMeasure(createMccCodeableConcept("goalMeasure"));
		mccGoal.setTargets(ggg);

		Acceptance[] aaa = new Acceptance[1];
		aaa[0] = new Acceptance();
		aaa[0].setCode("acceptance");

		aaa[0].setPriority(createMccCodeableConcept("priority"));
//		mccGoal.setAcceptance("aaa");
		
		mccGoal.setAchievementStatus(createMccCodeableConcept("AchievementStatus"));;
		
		
		ObjectMapper objMapper = new ObjectMapper();
		String jsonString = objMapper.writeValueAsString(mccGoal);
		System.out.println(jsonString);
		
		
//		ResponseEntity<String> response = template.postForEntity("/creategoal?patientId=smart-1557780", mccGoal,
//				String.class);
//		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
//		System.out.println(response.getBody());
	}

//	@Test
//	public void testErrorGoal() {
//		GoalSummary mccGoal = new GoalSummary();
//		ResponseEntity<String> response = template.postForEntity("/creategoal?patientId=smart-1557780", mccGoal,
//				String.class);
//		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
//		System.out.println(response.getBody());
//	}
//
//	@Test
//	public void testCreateGoalNulls() throws JsonProcessingException {
//		GoalSummary mccGoal = new GoalSummary();
//		mccGoal.setDescription(createMccCodeableConcept("description"));
//		mccGoal.setLifecycleStatus("proposed");
//		ResponseEntity<String> response = template.postForEntity("/creategoal?patientId=smart-1557780", mccGoal,
//				String.class);
//		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
//		System.out.println(response.getBody());
//
//	}
//	
	@Test
	public void testGetGoals() {
		 
		ResponseEntity<String> response = template.getForEntity("/goal?subject=mcc-pat-pnoelle", 
				String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}
	
	@Test
	public void testGetGoalSummary() {
//		 template.headForHeaders(null, null)
		ResponseEntity<String> response = template.getForEntity("/summary/goals?subject=mcc-pat-pnoelle", 
				String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}
	
	@Test
	public void testRelatedGoals() {
		Goal goal = new Goal();
		
		Extension parent = new Extension("http://hl7.org/fhir/StructureDefinition/goal-relationship");
		goal.addExtension(parent);

		CodeableConcept cc =  new CodeableConcept();
		cc.setText("typecode");
		Extension child1 = new Extension("type", cc);
		parent.addExtension(child1);

		Reference reference = new Reference();
		reference.setDisplay("display");
		
		Extension child2 = new Extension("target", reference);
		parent.addExtension(child2);
		
		
		
		Extension parent1 = new Extension("http://hl7.org/fhir/StructureDefinition/goal-relationship");
		goal.addExtension(parent1);

		CodeableConcept cc1 =  new CodeableConcept();
		cc1.setText("typecode");
		Extension child11 = new Extension("type1", cc1);
		parent1.addExtension(child11);

		Reference reference1 = new Reference();
		reference1.setDisplay("display1");
		
		Extension child21 = new Extension("target", reference1);
		parent1.addExtension(child21);
		
		
		FhirContext ctx = FhirContext.forR4();
		System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(goal));
		
		List<Extension> relationships = goal.getExtensionsByUrl("http://hl7.org/fhir/StructureDefinition/goal-relationship");
		for (Extension relationship:relationships) {
			
		
		Extension type = relationship.getExtensionByUrl("type");
		CodeableConcept valueCodeableConcept =(CodeableConcept) type.getValue();
		System.out.println(valueCodeableConcept.getText());
		Extension target = relationship.getExtensionByUrl("target");
		Reference valueReference = (Reference) target.getValue();
		System.out.println(valueReference.getDisplay());
		}

//		 template.headForHeaders(null, null)
	 
	}
	
	@Test
	public void testAcceptance() {
		
		Patient p;
		
		Goal goal = new Goal();
		Extension parent = new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/goal-acceptance");
		goal.addExtension(parent);
		 
			CodeableConcept cc = new CodeableConcept();
			cc.setText("priority");
			Extension child1 = new Extension("priority", cc);
			parent.addExtension(child1);
		 

		 
			Reference reference = new Reference();
			reference.setDisplay("indivdual");

			Extension child2 = new Extension("indivdual", reference);
			parent.addExtension(child2);
		 

		 
			CodeType status = new CodeType("status");
			Extension child3 = new Extension("status", status);
			parent.addExtension(child3);
		 
			FhirContext ctx = FhirContext.forR4();	
		System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(goal));
		
		
	}

}