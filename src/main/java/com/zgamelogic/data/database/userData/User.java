package com.zgamelogic.data.database.userData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Users")
public class User {
    @Id
    private Long id;
    private Long phone_number;
    private String user_name;
    private Boolean no_hour_message;

    public User(long id){
        this.id = id;
    }
}
