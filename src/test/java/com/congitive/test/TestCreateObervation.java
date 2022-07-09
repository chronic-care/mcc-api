package com.congitive.test;

/*******************************************************************************
 * Copyright (c) 2018 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     seanmuir - initial API and implementation
 *
 *******************************************************************************/

import static org.junit.Assert.assertTrue;

import org.hl7.fhir.r4.model.Observation;
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
import com.cognitive.nih.niddk.mccapi.data.MccObservation;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCodeableConcept;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCoding;
import com.cognitive.nih.niddk.mccapi.data.primative.MccReference;
import com.cognitive.nih.niddk.mccapi.mappers.ObservationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(SpringRunner.class)

@SpringBootTest(classes = MccApiApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "FHIR_SERVER=https://gw.interop.community/CarePlanningQA/open" })
public class TestCreateObervation {

	@Autowired
	private TestRestTemplate template;

	@Test
	public void testTransform() {
		ObservationMapper om = new ObservationMapper();		
		MccObservation mccObservation = new MccObservation();

		mccObservation.setCode(createMccCodeableConcept("obervation code"));
		
		MccReference[] yy = new MccReference[1];
		yy[0] = new MccReference();
		yy[0].setDisplay("addressses");
		mccObservation.setBasedOn(yy);
		
		mccObservation.setStatus("final");
		
		Observation observation = ObservationMapper.local2fhir("patientid",mccObservation );
	}

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
	public void testCreateObservation() throws JsonProcessingException {
		
		MccObservation mccObservation = new MccObservation();
//		MccCodeableConcept code;
		mccObservation.setCode(createMccCodeableConcept("obervation code"));
		
		MccReference[] yy = new MccReference[1];
		yy[0] = new MccReference();
		yy[0].setDisplay("addressses");
		mccObservation.setBasedOn(yy);
		
		mccObservation.setStatus("final");
	
		ResponseEntity<String> response = template.postForEntity("/createPROM?patientId=smart-1557780", mccObservation,
				String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}



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

}