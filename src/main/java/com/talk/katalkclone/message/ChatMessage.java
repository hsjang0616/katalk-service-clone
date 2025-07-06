package com.talk.katalkclone.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    private String roomId;
    private String senderId;
    private String receiverId;
    private String content;

}
