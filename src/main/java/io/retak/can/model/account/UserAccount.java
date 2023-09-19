package io.retak.can.model.account;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAccount extends Account {
    private String fullName;
    private String phoneNumber;
}
