package org.sfa.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO for RequestIsLeadVol entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestIsLeadVolDTO {

	@NotNull(message = "requestIsLeadId cannot be null")
	//@JsonProperty("reqIsLeadId")
    private Integer requestIsLeadId;
}

