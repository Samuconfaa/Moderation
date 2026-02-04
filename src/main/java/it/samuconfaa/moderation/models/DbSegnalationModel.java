package it.samuconfaa.moderation.models;

import java.sql.Timestamp;
import java.util.UUID;

public class DbSegnalationModel {
    public UUID PlayerUUID;
    public int ID;
    public String Message;
    public Timestamp Timestamp;
    public static Action Action;

    public DbSegnalationModel( int id, UUID uuid, String message, Timestamp timestamp, Action action){
        this.PlayerUUID = uuid;
        this.ID = id;
        this.Message = message;
        this.Timestamp = timestamp;
        this.Action = action;
    }

    public enum Action{
        SIGN_MESSAGE,
        CHAT_MESSAGE_CAPS,
        CHAT_MESSAGE_BLACKLIST,
        PLUGIN_ERROR
    }
}

