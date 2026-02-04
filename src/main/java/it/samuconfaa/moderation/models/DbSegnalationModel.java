package it.samuconfaa.moderation.models;

import java.sql.Timestamp;
import java.util.UUID;

public class DbSegnalationModel {
    public UUID PlayerUUID;
    public int ID;
    public String Message;
    public Timestamp Timestamp;
    public EnumAction Action;

    public DbSegnalationModel( int id, UUID uuid, String message, Timestamp timestamp, EnumAction enumAction){
        this.PlayerUUID = uuid;
        this.ID = id;
        this.Message = message;
        this.Timestamp = timestamp;
        this.Action = enumAction;
    }


}

