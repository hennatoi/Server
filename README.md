This is a school project. It is an individual work, for which I got help and support from the teachers at the time.
It is a simple server that uses the following contents: Databases, database programming, data formats, the design, implementation, and testing of a server interface, the safety and security of a server, concurrency.


-When the server starts, it opens the database and creates the database and tables, if necessary.
-Functionality to add the user registration data to the database.
-Check if username is registered before inserting a new user. Insert a new registered user to database if not registered already.
-Validate users based on data in database. Check if user who sent the request is a valid user (username and password match).
-Insert new coordinates to database. Create a method for that in CoordinateDatabase, to store coordinates to a database table.
-Read coordinates from the database. 


Inside the CoordinateHandler.java is the handle()- function, in which I check the request sent by the client, prepare the server’s response to the client, and write the response back to the client.

I created a self-signed certificate. In Server.java I call the function coordinatesServerSSLContext() (which is implemented also in Server.java) to create a SSLContext for my HTTPS server, using the self signed certificate I created. Then I configure the HttpsServer to use the sslContext by adding this call to setHttpsConfigurator, which I give the sslContext I just created.

I added a try/catch structure to the main function to catch errors that might happen during code
execution.

I added UserAuthenticator.java to authenticate users, so users cannot POST or GET any messages unless authenticated.

I changed user registration and the response the server sends to user to use JSON data format. The data element defines the username, password and email as a JSON element.
If user provides bad JSON object, there are missing fields or the POST message doesn’t contain a JSON object, the server must throw error code to client. When parsing the JSON object, the parser will throw an exception which need to be catched in order to send the error code.

I modified coordinates data to include timestamp when the coordinates are sent. I also return the timestamp.
After that I implemented database to store the user details and coordinates.
I created a new Java class called CoordinateDatabase. When implementing the database, other classes need to call CoordinateDatabase -class in order to execute database functions.
I implemented the database class as a Singleton, so that I can get access to it from anywhere in the server by calling CoordinateDatabase.getInstance().
I created the database this way: Server itself checks if the database exists and if not, creates it with all the necessary tables and indexes before continuing ahead launching the various HttpHandlers. This allows the program to create database and run it when necessary.

At the end I implemented hashing and salting the password in CoordinateDatabase.java setUser()- function. Hashing is implemented so that the server uses a hash function to create a hash of the password. To make the hashed passwords even more securely stored, I also used salt, which is used to further messing up the hashed password. This ensures that hashed passwords are hashed uniquely; each with the hash function and the password specific salt. No other user/ password uses the same salt.