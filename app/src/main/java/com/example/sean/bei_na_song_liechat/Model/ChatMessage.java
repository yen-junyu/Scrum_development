package com.example.sean.bei_na_song_liechat.Model;

public class ChatMessage {
    private String textMessage;
    private Integer imageMessage;
    public ChatMessage(String message,Integer imageID) {
        if(imageID != null){
            this.imageMessage = imageID;
            this.textMessage = null;
        }
        else{
            this.textMessage = message;
            this.imageMessage = null;
        }
    }

    public ChatMessage() {
    }

    public String checkMessageType(){
        if(imageMessage != null)
            return "Image";
        if(textMessage != null)
            return "Text";
        return "None";
    }

    public String getTextMessage(){
        return textMessage;
    }

    public Integer getImageMessage(){
        return imageMessage;
    }
}
