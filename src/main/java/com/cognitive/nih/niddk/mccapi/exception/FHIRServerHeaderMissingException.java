package com.cognitive.nih.niddk.mccapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Missing Header mcc-fhir-server")
public class FHIRServerHeaderMissingException extends RequiredHeaderException {
}
