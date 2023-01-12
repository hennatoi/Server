package com.server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class UserCoordinate {

    public String sent;
    public String nick;
    public Double longitude;
    public Double latitude;

    public UserCoordinate(String nick, Double longitude, Double latitude){
        this.nick = nick;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getNick(){
        return this.nick;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }
}
