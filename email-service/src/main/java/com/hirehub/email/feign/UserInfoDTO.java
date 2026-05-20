package com.hirehub.email.feign;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
