package org.sfa.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnumsResponse {
    private Map<Integer, String> requestFor;
    private Map<Integer, String> requestPriority;
    private Map<Integer, String> requestStatus;
    private Map<Integer, String> requestType;
}
