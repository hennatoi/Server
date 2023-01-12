package com.server;

import com.sun.net.httpserver.*;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Scanner;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;


public class Server {

    private Server() {
    }

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        try {
            UserAuthenticatorDB authenticator = new UserAuthenticatorDB();

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
        
            final HttpContext finalContext = server.createContext("/coordinates", new CoordinateHandler());

            server.createContext("/registration", new RegistrationHandler(authenticator));

            finalContext.setAuthenticator(authenticator);

            // I call the function coordinatesServerSSLContext() to create a SSLContext for my HTTPS server,
            // using the self signed certificate I created.
            // Then I configure the HttpsServer to use the sslContext by adding this call to setHttpsConfigurator,
            // which I give the sslContext.
             
            SSLContext sslContext = coordinatesServerSSLContext(args[0], args[1]);

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    // get the remote address if needed
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    // get the default parameters
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });

            //  I want several threads to handle the client requests to make the server more efficient.
            // In order to support threading, I use cached thread pool Executor:
            // 
            server.setExecutor(null);
            CoordinateDatabase db = CoordinateDatabase.getInstance();
            db.open("coordinatesDB");
            
            server.start();

            boolean running = true;

            while (running){
                String message = scanner.nextLine();
                if (message.equals("/quit")){
                    running = false;
                    server.stop(3);
                    db.closeDB();
                }
            }
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLContext coordinatesServerSSLContext(String keystore, String password) throws Exception {

        char[] passphrase = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystore), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }
}