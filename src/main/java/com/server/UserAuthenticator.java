package com.server;

import java.util.ArrayList;
import com.sun.net.httpserver.BasicAuthenticator;

// I added UserAuthenticator.java to authenticate users,
// so users cannot POST or GET any messages unless authenticated.

public class UserAuthenticator extends BasicAuthenticator{

    private CoordinateDatabase db = null;
    private ArrayList<User> users = null;

    public UserAuthenticator(){
        super("coordinates");
        db = CoordinateDatabase.getInstance();
        users = new ArrayList<User>();
    }

    public boolean checkCredentials(String username, String password){

        for(int i = 0; i < users.size(); i++){
            if(users.get(i).getUsername().equals(username) && users.get(i).getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }
    
    public boolean addUser(String username, String password, String email){

        System.out.println("registering user");

        for(int i = 0; i < users.size(); i++){
            if(users.get(i).getUsername().equals(username)){
                System.out.println(username + " already exists");
                return false;
            }
        }
        User registerUser = new User(username, password, email);
        users.add(registerUser);
        System.out.println(username + " registered");

        return true;
    }
}
