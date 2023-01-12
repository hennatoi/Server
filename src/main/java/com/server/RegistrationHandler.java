package com.server;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class RegistrationHandler implements HttpHandler {

    private UserAuthenticatorDB auth;

    public RegistrationHandler(UserAuthenticatorDB newAuth) {
        auth = newAuth;
    }

    String contentType = "";
    String response = "";
    int code = 0;
    JSONObject obj = null;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            handlePOSTRequest(exchange);
            handleResponsePOST(exchange);

        } else {
            handleResponse(exchange);
        }
    }

    private void handlePOSTRequest(HttpExchange exchange) throws IOException {

        try {
            Headers headers = exchange.getRequestHeaders();

            if (headers.containsKey("Content-Type")) {
                contentType = headers.get("Content-Type").get(0);
                System.out.println("Content-Type available");
            } else {
                System.out.println("No Content-Type");
                code = 411;
                response = "No content type in request!";
            }

            // Read the incoming JSON text from user:

            if (contentType.equalsIgnoreCase("application/json")) {
                InputStream stream = exchange.getRequestBody();

                String newUser = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));

                stream.close();
            
            //  Create the JSONObject from the text string: 
            
                try {
                    obj = new JSONObject(newUser);
                } catch (JSONException e) {
                    code = 412;
                    response = "json parse error";
                }

                if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0
                        || obj.getString("email").length() == 0) {
                    code = 413;
                    response = "No proper user credentials";
                } else {
                    System.out
                            .println("Registering user " + obj.getString("username") + " " + obj.getString("password"));

                    boolean result = auth.addUser(obj.getString("username"), obj.getString("password"),
                            obj.getString("email"));
                    if (result == false) {
                        code = 405;
                        response = "user already exists";
                    } else {
                        code = 200;
                        response = "user registered";
                    }
                }

            } else {
                code = 407;
                response = "Content Type is not application/json";
            }
        } catch (Exception e) {
            code = 500;
            response = "internal server error";
        }
    }

    private void handleResponsePOST(HttpExchange httpExchange) throws IOException {

        byte[] bytes = response.getBytes("UTF-8");
        httpExchange.sendResponseHeaders(code, bytes.length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private void handleResponse(HttpExchange httpExchange) throws IOException {

        byte[] bytes = "only POST accepted".getBytes("UTF-8");

        httpExchange.sendResponseHeaders(401, bytes.length);
        OutputStream outputStream = httpExchange.getResponseBody();

        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();

    }
}
