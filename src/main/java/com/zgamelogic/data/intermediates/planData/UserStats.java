package com.zgamelogic.data.intermediates.planData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class UserStats {
    private final long acceptedCount;
    private final long declinedCount;
}
