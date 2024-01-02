package app.registrationSystem.controllers;

import app.registrationSystem.dto.mappers.IllnessMapper;
import app.registrationSystem.dto.mappers.SpecializationMapper;
import app.registrationSystem.dto.request.PasswordChangeRequest;
import app.registrationSystem.dto.response.IllnessResponse;
import app.registrationSystem.dto.response.Response;
import app.registrationSystem.dto.request.UserRegistrationRequest;
import app.registrationSystem.dto.response.SpecializationResponse;
import app.registrationSystem.jpa.entities.Illness;
import app.registrationSystem.jpa.entities.Specialization;
import app.registrationSystem.security.CustomUserDetails;
import app.registrationSystem.security.Privilege;
import app.registrationSystem.security.RequiredPrivilege;
import app.registrationSystem.services.IllnessService;
import app.registrationSystem.services.SpecializationService;
import app.registrationSystem.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {
    private final IllnessService illnessService;
    private final SpecializationService specializationService;
    private final UserService userService;

    @GetMapping("/auth/illnesses")
    public ResponseEntity<List<IllnessResponse>> getAllIllnesses() {
        return ResponseEntity.status(HttpStatus.OK).body(illnessService.getAll().stream().map(IllnessMapper.INSTANCE::convert).toList());
    }

    @RequiredPrivilege(value = Privilege.CHECK_SPECIALIZATIONS)
    @GetMapping("/specializations")
    public ResponseEntity<List<SpecializationResponse>> getAllSpecializations() {
        return ResponseEntity.status(HttpStatus.OK).body(specializationService.getAll().stream().map(SpecializationMapper.INSTANCE::convert).toList());
    }

    @RequiredPrivilege(value = Privilege.CHANGE_CREDENTIALS)
    @PatchMapping("/change-credentials")
    public ResponseEntity<Response> changeCredentials(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody UserRegistrationRequest user) {
        Response response = userService.changeCredentials(customUserDetails.getUsername(), user);
        return ResponseEntity.status(response.httpStatus()).body(response);
    }

    @RequiredPrivilege(value = Privilege.CHANGE_CREDENTIALS)
    @PatchMapping("/change-password")
    public ResponseEntity<Response> changePassword(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody @Valid PasswordChangeRequest passwordChangeRequest) {
        Response response = userService.changePassword(customUserDetails.getUsername(), passwordChangeRequest);
        return ResponseEntity.status(response.httpStatus()).body(response);
    }

}
