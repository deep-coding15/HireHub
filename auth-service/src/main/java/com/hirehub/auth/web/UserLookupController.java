package com.hirehub.auth.web;

import com.hirehub.auth.model.UserAccount;
import com.hirehub.auth.repo.UserAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserLookupController {

    private final UserAccountRepository userAccountRepository;

    public UserLookupController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserInfoResponse> getUserById(@PathVariable String id) {
        return userAccountRepository.findById(UUID.fromString(id))
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserInfoResponse toResponse(UserAccount user) {
        return new UserInfoResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                "",
                user.getRole().name()
        );
    }

    public record UserInfoResponse(
            String id,
            String email,
            String firstName,
            String lastName,
            String role
    ) {}
}
