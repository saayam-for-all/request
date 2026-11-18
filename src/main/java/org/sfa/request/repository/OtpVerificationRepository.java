package org.sfa.request.repository;

import org.sfa.request.model.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OTP verification records (Part 3 of the workflow, issue #95).
 * The backing table is proposed in {@code db/proposed/otp_verification.sql}.
 */
@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, String> {

    List<OtpVerification> findByRequestIdAndState(String requestId, String state);
}
