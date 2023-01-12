package com.server;

import com.sun.net.httpserver.BasicAuthenticator;
import org.json.JSONObject;
import java.sql.SQLException;
import org.json.JSONException;

public class UserAuthenticatorDB extends BasicAuthenticator {

    private CoordinateDatabase db = null;

    public UserAuthenticatorDB() {
        super("coordinates");
        db = CoordinateDatabase.getInstance();
    }


    @Override
    public boolean checkCredentials(String username, String password) {

        System.out.println("checking user: " + username + " " + password + "\n");

        boolean isValidUser;
        try {
            isValidUser = db.authenticateUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return isValidUser;
    }

    public boolean addUser(String username, String password, String email) throws JSONException, SQLException {

        if (db.checkIfUserExists(username)) {
            System.out.println("cannot register user");
            return false;
        }else{
            db.setUser(new JSONObject().put("username", username).put("password", password).put("email", email));
        }
        System.out.println(username + " registered");

        return true;
    }

}
