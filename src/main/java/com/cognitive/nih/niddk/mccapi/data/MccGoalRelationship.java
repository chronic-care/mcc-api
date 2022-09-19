package com.cognitive.nih.niddk.mccapi.data;

import javax.validation.constraints.NotBlank;

import com.cognitive.nih.niddk.mccapi.data.primative.MccCodeableConcept;
import com.cognitive.nih.niddk.mccapi.data.primative.MccReference;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data class MccGoalRelationship {

	@NotBlank
	MccReference target;
	@NotBlank
	MccCodeableConcept type;
}
