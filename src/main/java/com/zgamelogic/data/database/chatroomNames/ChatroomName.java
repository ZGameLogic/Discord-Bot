package com.zgamelogic.data.database.chatroomNames;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chatroom_names")
@NoArgsConstructor
@Data
public class ChatroomName {
    @Id
    private String name;
    private String game;
}
