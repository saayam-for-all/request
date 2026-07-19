package org.sfa.request.client;

import lombok.RequiredArgsConstructor;
import org.sfa.request.dto.GuestDetailsDTO;
import org.sfa.request.dto.volunteer.CreateUserResponse;
import org.sfa.request.dto.volunteer.UserIdResponseDTO;
import org.sfa.request.response.SaayamResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VolunteerServiceClient {

    private final WebClient volunteerWebClient;

    // Checks if a user already exists in volunteer-service by email.
    public Optional<String> findUserIdByEmail(String email) {
        try {
            SaayamResponse<UserIdResponseDTO> response = volunteerWebClient.post()
                    .uri("/0.0.1/users/userIdByEmail")
                    .bodyValue(Map.of("email", email))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<SaayamResponse<UserIdResponseDTO>>() {})
                    .block();

            String userId = (response != null && response.getData() != null)
                    ? response.getData().getUserId()
                    : null;

            return Optional.ofNullable(userId);

        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        }
    }

    // Creates a new user in volunteer-service.
    public String createUser(GuestDetailsDTO guestDetails) {
        Map<String, Object> payload = Map.of(
                "firstName", guestDetails.getReqFname(),
                "lastName", guestDetails.getReqLname(),
                "email", guestDetails.getReqEmail(),
                "phone", guestDetails.getReqPhone(),
                "age", guestDetails.getReqAge(),
                "gender", guestDetails.getReqGender(),
                "preferredLanguage", guestDetails.getReqPrefLang()
        );

        SaayamResponse<CreateUserResponse> response = volunteerWebClient.post()
                .uri("/0.0.1/users")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SaayamResponse<CreateUserResponse>>() {})
                .block();

        if (response == null || response.getData() == null || response.getData().getUserId() == null) {
            throw new IllegalStateException("Volunteer service did not return a user id after creation");
        }

        return response.getData().getUserId();
    }

    /** Find-or-create in one call — this is what RequestServiceImpl should use. */
    public String getOrCreateUserId(GuestDetailsDTO guestDetails) {
        return findUserIdByEmail(guestDetails.getReqEmail())
                .orElseGet(() -> createUser(guestDetails));
    }
}