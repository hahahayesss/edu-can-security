package io.retak.can.model.account;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminAccount extends Account {
    private String nickName;
}
