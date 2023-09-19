package io.retak.can.model.account;

import io.retak.can.model.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class Account extends Entity {
    private String username;
    private String password;
    private Set<String> roles;
    private boolean locked;
    private boolean disabled;
}
