package com.zgamelogic.data.database.huntData.headshot;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@ToString
@Entity
public class HeadShot {
    @Id
    @GeneratedValue
    private long id;
    private ZonedDateTime date;
    private long recordedBy;

    public HeadShot(ZonedDateTime date, long recordedBy) {
        this.date = date;
        this.recordedBy = recordedBy;
    }
}
