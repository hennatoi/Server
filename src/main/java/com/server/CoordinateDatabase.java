package com.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


// When implementing the database, other classes need to call CoordinateDatabase -class in order to execute database functions.
// I implemented the database class as a Singleton, so that I can get access to it from anywhere in the
// server by calling CoordinateDatabase.getInstance()

public class CoordinateDatabase {

    private Connection dbConnection = null;
    private static CoordinateDatabase dbInstance = null;
    boolean dbExists = false;
    String dbName;
    SecureRandom secureRandom = new SecureRandom();

    //  One technique to make sure the method call is safe is to use is to mark the method synchronized.
    // This makes sure no two threads are calling the method at the same time.
    // If several threads attempt to call the method while another thread is executing it,
    // the other threads stop and wait for the another thread to exit the method.

    public static synchronized CoordinateDatabase getInstance() {
        if (null == dbInstance) {
            dbInstance = new CoordinateDatabase();
        }
        return dbInstance;
    }

    private CoordinateDatabase() {

    }

    // CoordinateDatabase.open method should check if the database file exist or not:

    public void open(String dbName) throws SQLException {

        this.dbName = dbName;

        File file = new File(dbName);

        if (file.isFile()) {
            dbExists = true;
            String database = "jdbc:sqlite:" + dbName;
            dbConnection = DriverManager.getConnection(database);
        } else {
            initializeDatabase(dbName);
        }
    }

    // If the file above does not exist, initializeDatabase() is used to create
    // the necessary database tables in the database.
    // In initializeDatabase, the Connection is used to execute a SQL query to create the database tables:

    private void initializeDatabase(String dbName) throws SQLException {

        String database = "jdbc:sqlite:" + dbName;
        dbConnection = DriverManager.getConnection(database);

        if (null != dbConnection) {
            String createBasicDB = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, salt varchar(150) NOT NULL, email varchar(50), primary key(username));"
                    +
                    "create table coordinates (username varchar(50) NOT NULL, longitude number NOT NULL, latitude number NOT NULL, sent varchar(50) NOT NULL, description varchar(1024), primary key (sent))";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createBasicDB);
            createStatement.close();
            System.out.println("DB successfully created");
        }
    }

    public void closeDB() throws SQLException {
        if (null != dbConnection) {
            dbConnection.close();
            dbConnection = null;
            System.out.println("closing db connection");
        }
    }

    // At the end I implemented hashing and salting the password.
    // Hashing is implemented so that the server uses a hash function to create a hash of the password.
    // To make the hashed passwords even more securely stored, I also used salt, which is used to further messing up the hashed password.

    public synchronized boolean setUser(JSONObject user) throws SQLException {

        if (checkIfUserExists(user.getString("username"))) {
            return false;
            // System.out.println("user already exists");
        }

        byte bytes[] = new byte[13];
        secureRandom.nextBytes(bytes);

        String saltedB = new String(Base64.getEncoder().encode(bytes));

        String salt = "$6$" + saltedB;
        System.out.println(salt);

        String userPasswordToBeSaved = Crypt.crypt(user.getString("password"), salt);

        String setUserString = "insert into users " +
                "VALUES('" + user.getString("username") + "','" + userPasswordToBeSaved + "','"
                + salt + "','" + user.getString("email") + "')";
        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setUserString);
        createStatement.close();

        return true;
    }

    public boolean checkIfUserExists(String givenUserName) throws SQLException {

        Statement queryStatement = null;
        ResultSet rs = null;

        String checkUser = "select username from users where username = '" + givenUserName + "'";

        try {
            queryStatement = dbConnection.createStatement();
        } catch (SQLException e) {
            throw e;
        }

        try {
            rs = queryStatement.executeQuery(checkUser);
        } catch (Exception e) {
            throw e;
        }

        if (rs.next()) {
            System.out.println("user exists");
            return true;
        } else {
            return false;
        }
    }

    public boolean authenticateUser(String givenUserName, String givenPassword) throws SQLException {

        Statement queryStatement = null;
        ResultSet rs;

        String getMessagesString = "select username, password, salt from users where username = '" + givenUserName + "'";

        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(getMessagesString);


        if (rs.next() == false) {
            System.out.println("cannot find such user");
            return false;
        } else {
            String password = rs.getString("password");

            if (password.equals(Crypt.crypt(givenPassword, password))) {
               return true;

            } else {
                return false;
            }
        }
    }

    public void setCoordinates(JSONObject coordinates) throws SQLException {

        String description;
        try {
            description = coordinates.getString("description");
                if (description.length() == 0){
                    description = "nodata";
                }    
        }catch(JSONException exception){
            description = "";
        }

        String setMessageString = "insert into coordinates " +
                "VALUES('" + coordinates.getString("username") + "','" + coordinates.getDouble("longitude") + "','"
                + coordinates.getDouble("latitude") + "','" + coordinates.getString("sent") + "','" + description + "')";

        Statement createStatement;

        try {
            createStatement = dbConnection.createStatement();
        } catch (Exception e) {
            throw e;
        }

        try {
            createStatement.executeUpdate(setMessageString);
        } catch (Exception e) {
            throw e;
        }

        createStatement.close();
    }

    public JSONArray getCoordinates() throws SQLException {

        Statement queryStatement = null;
        JSONArray array = new JSONArray();

        String getMessagesString = "select username, longitude, latitude, sent, description from coordinates ";

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);

        while (rs.next()) {
            JSONObject obj = new JSONObject();
            obj.put("username", rs.getString("username"));
            obj.put("longitude", rs.getDouble("longitude"));
            obj.put("latitude", rs.getDouble("latitude"));
            obj.put("sent", rs.getString("sent"));

            if (rs.getString("description").length() == 0){
                ;
            } else if (rs.getString("description").equals("nodata")){
                obj.put("description", "");
            }else{
                obj.put("description", rs.getString("description"));
            }

            array.put(obj);
        }

        return array;

    }

}
