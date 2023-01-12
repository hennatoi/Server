package com.server;

import com.sun.net.httpserver.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.io.*;

public class CoordinateHandler implements HttpHandler {

    private CoordinateDatabase coordinates;

    public CoordinateHandler() {

    }

    String contentType = "";
    String response = "";
    int code = 0;
    JSONObject obj = null;

    @Override
    // Inside the handle()- function, I check the request sent by the client, prepare the server’s response to the client,
    // and write the response back to the client.
    public void handle(HttpExchange t) throws IOException {

        if (t.getRequestMethod().equalsIgnoreCase("GET")) {

            handleGetRequest(t);
            handleResponseGET(t);

        } else if (t.getRequestMethod().equalsIgnoreCase("POST")) {

            handlePOSTRequest(t);
            handleResponsePOST(t);

        } else {

            handleResponse(t, "Not supported");
        }
    }

    private void handleResponseGET(HttpExchange httpExchange) throws IOException {

        try {
            JSONArray responseCoordinates = CoordinateDatabase.getInstance().getCoordinates();

            byte[] bytes = responseCoordinates.toString(responseCoordinates.length()).getBytes("UTF-8");

            httpExchange.sendResponseHeaders(200, bytes.length);
            OutputStream outputStream = httpExchange.getResponseBody();

            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
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

    private void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {

        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(requestParamValue);

        String htmlResponse = htmlBuilder.toString();

        httpExchange.sendResponseHeaders(400, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();

    }

    private String handleGetRequest(HttpExchange httpExchange) throws IOException {

        return "";

    }

    private void handlePOSTRequest(HttpExchange exchange) throws JSONException {

        Headers headers = exchange.getRequestHeaders();
        try {
            if (headers.containsKey("Content-Type")) {
                contentType = headers.get("Content-Type").get(0);
                System.out.println("Content-Type available");
            } else {
                System.out.println("No Content-Type");
                code = 411;
                response = "No content type in request!";
            }
            if (contentType.equalsIgnoreCase("application/json")) {
                InputStream stream = exchange.getRequestBody();

                String userCoordinate = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                stream.close();

                try {
                    obj = new JSONObject(userCoordinate);
                } catch (JSONException e) {
                    code = 412;
                    response = "json parse error";
                }

                if (obj.getString("username").length() == 0) {
                    code = 413;
                    response = "No proper credentials";
                } else {

                    try {
                        CoordinateDatabase.getInstance().setCoordinates(obj);
                        response = "coordinates added";
                        code = 200;

                    } catch (SQLException e) {
                        e.printStackTrace();

                    } catch (NumberFormatException e) {
                        code = 400;
                        response = "bad request";
                    } catch (JSONException e) {
                        code = 400;
                        response = "bad request";
                    }
                }

            } else {
                code = 407;
                response = "Content Type is not application/json";
            }

        } catch (IOException e) {
            code = 500;
            response = "internal server error";

        }
    }

}