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
import com.cognitive.nih.niddk.mccapi.data.GoalTarget;
import com.cognitive.nih.niddk.mccapi.data.MccGoal;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCodeableConcept;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCoding;
import com.cognitive.nih.niddk.mccapi.data.primative.MccReference;
import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(SpringRunner.class)

@SpringBootTest(classes = MccApiApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "FHIR_SERVER=https://gw.interop.community/CarePlanningQA/open" })
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
		MccGoal mccGoal = new MccGoal();
		mccGoal.setDescription(createMccCodeableConcept("description"));

		MccCodeableConcept[] xx = new MccCodeableConcept[1];
		xx[0] = createMccCodeableConcept("outcomes");
		mccGoal.setOutcomeCodes(xx);

		MccReference[] yy = new MccReference[1];
		yy[0] = new MccReference();
		yy[0].setDisplay("addressses");
		mccGoal.setAddresses(yy);

		MccCodeableConcept[] zz = new MccCodeableConcept[1];
		zz[0] = createMccCodeableConcept("categories");
		mccGoal.setCategories(zz);
		mccGoal.setLifecycleStatus("proposed");
		mccGoal.setPriority(createMccCodeableConcept("priority"));
		mccGoal.setStatusDate("2022-01-01");
		String[] nnn = new String[1];
		nnn[0] = new String("notes");
		mccGoal.setNotes(nnn);
		mccGoal.setStartConcept(createMccCodeableConcept("StartConcept"));
		GoalTarget[] ggg = new GoalTarget[1];
		ggg[0] = new GoalTarget();
		ggg[0].setDueAsText("DUEASTTEXT");
		ggg[0].setMeasure(createMccCodeableConcept("goalMeasure"));
		mccGoal.setTargets(ggg);

		Acceptance[] aaa = new Acceptance[1];
		aaa[0] = new Acceptance();
		aaa[0].setCode("acceptance");

		aaa[0].setPriority(createMccCodeableConcept("priority"));
		mccGoal.setAcceptance(aaa);
		ResponseEntity<String> response = template.postForEntity("/creategoal?patientId=smart-1557780", mccGoal,
				String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}

	@Test
	public void testErrorGoal() {
		MccGoal mccGoal = new MccGoal();
		ResponseEntity<String> response = template.postForEntity("/creategoal?patientId=smart-1557780", mccGoal,
				String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
		System.out.println(response.getBody());
	}

	@Test
	public void testCreateGoalNulls() throws JsonProcessingException {
		MccGoal mccGoal = new MccGoal();
		mccGoal.setDescription(createMccCodeableConcept("description"));
		mccGoal.setLifecycleStatus("proposed");
		ResponseEntity<String> response = template.postForEntity("/creategoal?patientId=smart-1557780", mccGoal,
				String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());

	}

}