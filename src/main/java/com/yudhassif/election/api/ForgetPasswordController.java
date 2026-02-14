package com.yudhassif.election.api;

import com.yudhassif.election.request.RequestPasswordDiscovery;
import com.yudhassif.election.request.ResetPasswordRequest;
import com.yudhassif.election.request.verify_Otp;
import com.yudhassif.election.response.ErrorResponse;
import com.yudhassif.election.response.SuccessResponse;
import com.yudhassif.election.response.TokenResponse;
import com.yudhassif.election.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class ForgetPasswordController {

    private final UserService userService;

    // 1. Request OTP (email + phone)
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestReset(@Validated @RequestBody RequestPasswordDiscovery request) {
        var user = userService.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        String otpRaw = userService.generateOtpAndSend(user.get());

        return ResponseEntity.ok(
                java.util.Map.of(
                        "userId", user.get().getId(),
                        "message", "OTP sent to the registered email address"
                )
        );
    }

    //    // 2. Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Validated @RequestBody verify_Otp dto) {
        // 1. The service now returns a Temporary Reset Token (UUID or JWT)
        // instead of just a boolean.
        Optional<String> resetToken = userService.verifyOtpAndGenerateResetToken(dto.getOtpRaw(), dto.getEmail());

        if (resetToken.isEmpty()) {
            // Professional: Use a clear error DTO, not just a string
            return ResponseEntity.status(400).body(new ErrorResponse("INVALID_OTP", "The code is incorrect or expired"));
        }

        // 2. Return the token. The frontend MUST send this token back in the next request.
        return ResponseEntity.ok(new TokenResponse(resetToken.get()));
    }


    // 3. Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        userService.resetPassword(request.getRawResetToken(), request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(new SuccessResponse("PASSWORD_RESET_SUCCESS", "Password updated successfully"));
    }
}
  //todo after otp verified is completing testing then follow reset password


