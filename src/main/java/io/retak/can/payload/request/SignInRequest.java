package io.retak.can.payload.request;

import lombok.Data;

@Data
public class SignInRequest {
    private String username;
    private String password;
}
