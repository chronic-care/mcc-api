/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.mappers;

import com.cognitive.nih.niddk.mccapi.data.Context;
import com.cognitive.nih.niddk.mccapi.data.Education;
import com.cognitive.nih.niddk.mccapi.data.EducationSummary;
import com.cognitive.nih.niddk.mccapi.data.ServiceRequestSummary;

import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.ServiceRequest;

public interface IServiceRequestMapper {
    
 
    ServiceRequestSummary fhir2summary(ServiceRequest in, Context ctx);
}
