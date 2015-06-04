/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            ClearScreen();
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                ClearScreen();
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Friends");
                System.out.println("2. Update Profile");
                System.out.println("3. Send Friend Request");
                System.out.println("4. Accept/Deny Friend Request");
                System.out.println("5. Search for a user");
                System.out.println("6. View Messages");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: FriendList(esql, authorisedUser); break;
                   case 2: UpdateProfile(esql, authorisedUser); break;
                   case 3: SendRequest(esql, authorisedUser); break;
                   case 4: AcceptDenyRequests(esql, authorisedUser); break;
                   case 5: SearchUsers(esql, authorisedUser); break;
                   case 6: ViewMessages(esql, authorisedUser); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();

      	 //Creating empty contact\block lists for a user
      	 String query = String.format("INSERT INTO USR (userId, password, email) VALUES ('%s','%s','%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
         Sleep(1000);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void ClearScreen(){
     try{
       final String ANSI_CLS = "\u001b[2J";
       final String ANSI_HOME = "\u001b[H";
       System.out.print(ANSI_CLS + ANSI_HOME);
       System.out.flush();
     }catch(Exception e){
       System.err.println (e.getMessage ());
     }
   }

   public static void Sleep(Integer time){
     try {
       Thread.sleep(time); //1000 milliseconds is one second.
     } catch(InterruptedException ex) {
       Thread.currentThread().interrupt();
     }
   }

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/

   public static Boolean UserExists(ProfNetwork esql, String uname){
     try{
       String query = String.format("SELECT * FROM USR WHERE userId = '%s'", uname);
       int userNum = esql.executeQuery(query);
       if (userNum > 0){
        return true;
       }
       else{
         return false;
       }
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return null;
     }
   }//end

   public static Boolean ConnectionExists(ProfNetwork esql, String authorisedUser, String uname){
     try{
       String query = String.format("SELECT * FROM CONNECTION_USR WHERE (userid = '%s' AND connectionid = '%s') OR (userid = '%s' AND connectionid = '%s')", uname, authorisedUser, authorisedUser, uname);
       int num = esql.executeQuery(query);
       if (num > 0){
         return true;
       }
       else{
         return false;
       }
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return null;
     }
   }//end

   public static Integer NumConnections(ProfNetwork esql, String uname){
     try{
       String query = String.format("SELECT * FROM CONNECTION_USR WHERE (userId = '%s' OR connectionid = '%s') AND status = '1'", uname, uname);
       int userNum = esql.executeQuery(query);
       return userNum;
     }catch(Exception e){
       System.err.println(e.getMessage());
       return null;
     }
   }

   public static List<String> GetConnections(ProfNetwork esql, String authorisedUser){
     try{
       List<String> connections = new ArrayList<String>();
       String query = String.format("SELECT * FROM CONNECTION_USR WHERE userId = '%s' AND status = '1'", authorisedUser);
       for (List<String> connection : esql.executeQueryAndReturnResult(query)){
         connections.add(connection.get(1));
       }
       query = String.format("SELECT * FROM CONNECTION_USR WHERE connectionid = '%s' AND status = '1'", authorisedUser);
       for (List<String> connection : esql.executeQueryAndReturnResult(query)){
         connections.add(connection.get(0));
       }
       return connections;
     }catch(Exception e){
       System.err.println(e.getMessage());
       return null;
     }
   }

   public static Boolean WithinThreeConnections(ProfNetwork esql, String authorisedUser, String uname){
     try{

       Set<String> withinThreeConnections = new HashSet<String>();
       // First level of connection
       String oneConnectionQuery = String.format("SELECT connectionid FROM CONNECTION_USR WHERE userid = '%s' AND status = '1' UNION SELECT userid FROM CONNECTION_USR WHERE connectionid = '%s' AND status = '1'", authorisedUser, authorisedUser);
       List<List<String>> OneConnections = esql.executeQueryAndReturnResult(oneConnectionQuery);
       for (List<String> OneConnection : OneConnections){
         String conn = OneConnection.get(0);
         withinThreeConnections.add(conn);

         // Second level of connection
         String twoConnectionQuery = String.format("SELECT connectionid FROM CONNECTION_USR WHERE userid = '%s' AND status = '1' UNION SELECT userid FROM CONNECTION_USR WHERE connectionid = '%s' AND status = '1'", conn, conn);
         List<List<String>> TwoConnections = esql.executeQueryAndReturnResult(twoConnectionQuery);
         for (List<String> TwoConnection : TwoConnections){
           conn = TwoConnection.get(0);
           withinThreeConnections.add(conn);

           // Third level of connection
           String threeConnectionQuery = String.format("SELECT connectionid FROM CONNECTION_USR WHERE userid = '%s' AND status = '1' UNION SELECT userid FROM CONNECTION_USR WHERE connectionid = '%s' AND status = '1'", conn, conn);;
           List<List<String>> ThreeConnections = esql.executeQueryAndReturnResult(threeConnectionQuery);
           for (List<String> ThreeConnection : ThreeConnections){
             conn = ThreeConnection.get(0);
             withinThreeConnections.add(conn);
           }
         }
       }
       if(withinThreeConnections.contains(uname)){
         return true;
       }
       else{
         return false;
       }

     }catch(Exception e){
       System.err.println (e.getMessage ());
       return null;
     }
   }

   public static void SendMessage(ProfNetwork esql, String authorisedUser, String connectionId, String contents){
     try{
       String query = String.format("INSERT INTO message(senderid, receiverid, contents, deletestatus, status) VALUES('%s', '%s', '%s', 0, 0)", authorisedUser, connectionId, contents);
       esql.executeUpdate(query);
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }
   }

   public static void DeleteMessage(ProfNetwork esql, String msgid){
     try{
       String query = String.format("UPDATE message SET deletestatus = '1' WHERE msgid = '%s'", msgid);
       esql.executeUpdate(query);
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }
   }

   // deletestatus == 1 -> reciever has deleted message
   // status == 1 -> sender has delted message
   public static void ViewMessages(ProfNetwork esql, String authorisedUser){
     try{
       Boolean redoQuery = true;
       Boolean viewMessages = true;
       List<List <String>> messages = null;
       while(viewMessages){
         ClearScreen();
         if(redoQuery){
           String query = String.format("SELECT msgid, senderid, contents, deletestatus FROM message WHERE receiverid = '%s'", authorisedUser);
           messages = esql.executeQueryAndReturnResult(query);
         }
         Integer i = 0;
         System.out.println("Your inbox");
         for (List<String> message : messages){
           if (message.get(3).equals("0")){
             System.out.println("-------Message " + Integer.toString(i + 1) + "------");
             System.out.println("From: " + message.get(1));
             System.out.println("\t" + message.get(2));
             i = i + 1;
           }

         }
         System.out.println("...................");
         System.out.printf("%d. Go back\n", i + 1);
         Integer choice = readChoice();
         if(choice == i + 1){
           // User is choosing to go back.
           viewMessages = false;
           break;
         }
         else if (choice <= i && choice != 0){
           // Valid selection.
           ClearScreen();
           System.out.println("---------------");
           System.out.println("1. Delete message");
           System.out.println(".........................");
           System.out.println("2. Go back");
           switch (readChoice()){
             case 1:
              DeleteMessage(esql, messages.get(choice-1).get(0));
             break;
             case 2:
              break;
             default :
              System.out.println("Unrecognized choice!"); break;
            }
         }
         else{
           // Invalid selection.
           System.out.println("\tInvalid Selection");
           redoQuery = false;
           continue;
         }
       }


     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }
   }

   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);

         if (userNum > 0){
           return login;
         }
         else{
           System.out.println("\tInvalid credentials");
           Sleep(1500);
           return null;
         }
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void AcceptRequest(ProfNetwork esql, String authorisedUser, String friend){
     try{
       String query = String.format("UPDATE CONNECTION_USR SET status = '1' WHERE userid = '%s' AND connectionid = '%s' AND status = '0'", friend, authorisedUser);
       esql.executeUpdate(query);
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }
   }

   public static void DenyRequest(ProfNetwork esql, String authorisedUser, String friend){
     try{
       String query = String.format("UPDATE CONNECTION_USR SET status = '2' WHERE userid = '%s' AND connectionid = '%s' AND status = '0'", friend, authorisedUser);
       esql.executeUpdate(query);
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }
   }

   public static void EditPassword(ProfNetwork esql, String authorisedUser){
     try{
       System.out.print("\tWhat do you want your new password to be? ");
       String password = in.readLine();
       String query = String.format("UPDATE USR SET password = '%s' WHERE userid = '%s'", password, authorisedUser);
       esql.executeUpdate(query);
       System.out.println("\tUpdating password...");
     }catch(Exception e){
       System.err.printf("\t%s\n", e.getMessage ());
       return;
     }finally {
       Sleep(1500);
     }
   }

   public static void EditEmail(ProfNetwork esql, String authorisedUser){
     try{
       System.out.print("\tWhat do you want your new email to be? ");
       String email = in.readLine();
       String query = String.format("UPDATE USR SET email = '%s' WHERE userid = '%s'", email, authorisedUser);
       esql.executeUpdate(query);
       System.out.println("\tUpdating email...");
     }catch(Exception e){
       System.err.printf("\t%s\n", e.getMessage ());
       return;
     }finally {
       Sleep(1500);
     }
   }

   public static void EditName(ProfNetwork esql, String authorisedUser){
     try{
       System.out.print("\tWhat do you want your new name to be? ");
       String name = in.readLine();
       String query = String.format("UPDATE USR SET name = '%s' WHERE userid = '%s'", name, authorisedUser);
       esql.executeUpdate(query);
       System.out.println("\tUpdating name...");
     }catch(Exception e){
       System.err.printf("\t%s\n", e.getMessage ());
       return;
     }finally {
       Sleep(1500);
     }
   }

   public static void EditBirthday(ProfNetwork esql, String authorisedUser){
     try{
       System.out.print("\tWhat month were you born? ");
       Integer month = readChoice();
       System.out.print("\tWhat day were you born? ");
       Integer day = readChoice();
       System.out.print("\tWhat year were you born? ");
       Integer year = readChoice();

       String date = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);

       String query = String.format("UPDATE USR SET dateOfBirth = '%s' WHERE userid = '%s'", date, authorisedUser);
       esql.executeUpdate(query);
       System.out.println("\tUpdating birthday...");
     }catch(Exception e){
       System.err.printf("\t%s\n", e.getMessage ());
       return;
     }finally {
       Sleep(1500);
     }
   }

   public static List<List<String>> GetEductionHistory(ProfNetwork esql, String authorisedUser){
     try{
       String query = String.format("SELECT instituitionname, major, degree, startdate, enddate FROM educational_details WHERE userid = '%s'", authorisedUser);
       List<List<String>> edu = esql.executeQueryAndReturnResult(query);
       return edu;
     }catch(Exception e){
       System.err.printf("\t%s\n", e.getMessage ());
       return null;
     }
   }

   public static List<List<String>> GetWorkHistory(ProfNetwork esql, String authorisedUser){
     try{
       String query = String.format("SELECT company, role, location, startdate, enddate FROM work_expr WHERE userid = '%s'", authorisedUser);
       List<List<String>> works = esql.executeQueryAndReturnResult(query);
       return works;
     }catch(Exception e){
       System.err.printf("\t%s\n", e.getMessage ());
       return null;
     }
   }

   public static void FriendList(ProfNetwork esql, String authorisedUser){
     try{
       Boolean viewFriends = true;
       List<String> connections = null;
       List<List<String>> edus = null;
       List<List<String>> works = null;
       String connectionid = null;
       String profId = authorisedUser;
       while(viewFriends){
         connections = GetConnections(esql, profId);

         ClearScreen();
         System.out.println(profId + "'s friend's List");
         System.out.println("---------------");
         Integer i = 0;
         for (String connection : connections) {
           ++i;
           System.out.printf("%d. %s\n", i, connection);
         }
         System.out.println("...................");
         System.out.printf("%d. Go back\n", i + 1);
         Integer choice = readChoice();
         if(choice == i + 1){
           // User is choosing to go back.
           viewFriends = false;
           break;
         }
         else if (choice <= i){
           // Valid selection.
           connectionid = connections.get(choice-1);
           edus = GetEductionHistory(esql, connectionid);
           works = GetWorkHistory(esql, connectionid);


           ClearScreen();
           System.out.println(connectionid + "'s profile");
           System.out.println("---------------");
           System.out.println("Education history");
           System.out.println("---------------");
           if (edus.size() == 0){
             System.out.println(connectionid + " has no education history.");
           }
           for(List<String> edu : edus){
             String place = edu.get(0);
             String major = edu.get(1);
             String degree = edu.get(2);
             String startdate = edu.get(3);
             String enddate = edu.get(4);
             String msg = connectionid + " attended " + place + " where he received a \n" + degree + " in " + major + ".";
             System.out.println(msg);
           }
           System.out.println("---------------");
           System.out.println("Work history");
           System.out.println("---------------");
           if (works.size() == 0){
              System.out.println(connectionid + " has no work history.");
           }
           for(List<String> work : works){
             String company = work.get(0);
             String role = work.get(1);
             String location = work.get(2);
             String startdate = work.get(3);
             String enddate = work.get(4);
             String msg = connectionid + " worked as a " + role + " at " + company + " in " + location + ".";
             System.out.println(msg);

           }
           // TODO get work history details and print them here
           System.out.println("---------------");
           System.out.printf("1. View %s's friends\n", connectionid);
           System.out.println("2. Send a message");
           System.out.println("3. Send connection request");
           System.out.println(".........................");
           System.out.println("4. Go back");
           switch (readChoice()){
             case 1:
               profId = connectionid;
               continue;
             case 2:
               NewMessage(esql, authorisedUser, connectionid);
               break;
             case 3:
               if(connectionid.equals(authorisedUser)){
                 System.out.println("\tYou cannot friend yourself");
               }
               else if (ConnectionExists(esql, authorisedUser, connectionid)){
                 System.out.printf("\tConnection or Request already exists between you and %s\n", connectionid);
               }
               else if (!WithinThreeConnections(esql, authorisedUser, connectionid) && NumConnections(esql, authorisedUser) > 5){
                 System.out.printf("\tYou must be within three connection levels to add %s\n", connectionid);
               }
               else {
                 String query = String.format("INSERT INTO CONNECTION_USR(userid, connectionid, status) VALUES ('%s', '%s', '0')", authorisedUser, connectionid);
                 System.out.printf("\tSending friend request to %s...\n", connectionid);
                 esql.executeUpdate(query);
               }
               Sleep(1500);
              break;
             case 4:
              break;
             default :
              System.out.println("Unrecognized choice!"); break;
           }
         }
         else{
           // Invalid selection.
           System.out.println("\tInvalid Selection");
           continue;
         }
       }
     }catch(Exception e){
       System.err.printf("\t%s\n", e.getMessage ());
       return;
     }
   }

   public static void UpdateProfile(ProfNetwork esql, String authorisedUser){
     Boolean updateProfile = true;
     while(updateProfile){
       ClearScreen();
       System.out.println("EDIT PROFILE");
       System.out.println("---------------");
       System.out.println("1. Change password");
       System.out.println("2. Change email");
       System.out.println("3. Change name");
       System.out.println("4. Change birthday");
       System.out.println(".........................");
       System.out.println("5. Go back");
       switch (readChoice()){
         case 1:
          EditPassword(esql, authorisedUser); break;
         case 2:
          EditEmail(esql, authorisedUser); break;
         case 3:
          EditName(esql, authorisedUser); break;
         case 4:
          EditBirthday(esql, authorisedUser); break;
         case 5:
          updateProfile = false;
          break;
         default : System.out.println("Unrecognized choice!"); break;
       }
     }
   }

   public static void NewMessage(ProfNetwork esql, String authorisedUser, String connectionid){
     try{
       System.out.print("\tWrite message contents here: ");
       String contents = in.readLine();

       if (contents.length() > 500){
         System.out.println("\tMessage contents must be under 500 characters");
       }
       else{
         System.out.println("\tSending message...");
         SendMessage(esql, authorisedUser, connectionid, contents);
       }
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }finally{
       Sleep(1500);
     }
   }

   public static void SendConnection(ProfNetwork esql, String authorisedUser, String connectionid){
     try{
       if (ConnectionExists(esql, authorisedUser, connectionid)){
         System.out.printf("\tConnection or Request already exists between you and %s\n", connectionid);
       }
       else if(!WithinThreeConnections(esql, authorisedUser, connectionid) && NumConnections(esql, authorisedUser) > 5){
         System.out.println("\tYou must be within 3 connection levels to add " + connectionid);
       }
       else {
         String query = String.format("INSERT INTO connection_usr(userid, connectionid, status) VALUES('%s', '%s', 0)", authorisedUser, connectionid);
         esql.executeQuery(query);
         System.out.println("\tSending connection request...");
       }
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }finally{
       Sleep(1500);
     }
   }

   public static void SearchUsers(ProfNetwork esql, String authorisedUser){
     try{
       System.out.print("\tEnter username to search for: ");
       String uname = in.readLine();

       if(uname.equals(authorisedUser)){
         System.out.println("\tThat's you!");
         Sleep(1500);
       }
       else if (UserExists(esql, uname)) {
         Boolean viewProfile = true;
         while(viewProfile){
           ClearScreen();
           System.out.println(uname + "'s PROFILE");
           System.out.println("---------------");
           System.out.println("1. Request connection");
           System.out.println("2. Send Message");
           System.out.println(".........................");
           System.out.println("3. Go back");
           switch (readChoice()){
             case 1:
              esql.SendConnection(esql, authorisedUser, uname); break;
             case 2:
              esql.NewMessage(esql, authorisedUser, uname); break;
             case 3:
              viewProfile = false;
              break;
             default : System.out.println("Unrecognized choice!"); break;
           }
         }
       }
       else {
         System.out.println("\tUser does not exist");
         Sleep(1500);
       }
     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }

   }

   // Connection status:
   // 0 : Unaccepted
   // 1 : Accepted
   // 2 : Denied
   public static void SendRequest(ProfNetwork esql, String authorisedUser){
     try{
       System.out.print("\tWhat user do you want to friend? ");
       String uname = in.readLine();

       if(uname.equals(authorisedUser)){
         System.out.println("\tYou cannot friend yourself");
       }
       else if (ConnectionExists(esql, authorisedUser, uname)){
         System.out.printf("\tConnection or Request already exists between you and %s\n", uname);
       }
       else if (!WithinThreeConnections(esql, authorisedUser, uname) && NumConnections(esql, authorisedUser) > 5){
         System.out.printf("\tYou must be within three connection levels to add %s\n", uname);
       }
       else if (UserExists(esql, uname)) {
        String query = String.format("INSERT INTO CONNECTION_USR(userid, connectionid, status) VALUES ('%s', '%s', '0')", authorisedUser, uname);
        System.out.printf("\tSending friend request to %s...\n", uname);
        esql.executeUpdate(query);
       }
       else {
        System.out.println("\tUser does not exist");
       }
     }catch(Exception e){
       System.err.printf ("\t%s\n",e.getMessage ());
       return;
     }
     finally{
       Sleep(2000);
     }
   }

   public static void AcceptDenyRequests(ProfNetwork esql, String authorisedUser){
     try{
       Boolean acceptRequests = true;
       Boolean redoQuery = true;
       List<List <String> > unnaccpetedRequests = null;
        while(acceptRequests){
          if(redoQuery){
            String query = String.format("SELECT * FROM CONNECTION_USR WHERE connectionid = '%s' AND status = '0'", authorisedUser);
            unnaccpetedRequests = esql.executeQueryAndReturnResult(query);
          }
          String connectionid = null;
          Integer i = 0;
          ClearScreen();
          System.out.println("FRIEND REQUESTS");
          System.out.println("---------------");
          System.out.println("Requests From: ");
          for (List<String> result : unnaccpetedRequests) {
            ++i;
            connectionid = result.get(0);
            System.out.printf("%d. %s\n", i, connectionid);
          }
          System.out.println("...................");
          System.out.printf("%d. Go back\n", i + 1);
          Integer choice = readChoice();
          if(choice == i + 1){
            // User is choosing to go back.
            acceptRequests = false;
            break;
          }
          else if (choice <= i){
            // Valid selection.
            ClearScreen();
            System.out.println("ACCEPT OR DENY");
            System.out.println("---------------");
            System.out.println("1. Accept Friend Request");
            System.out.println("2. Deny Friend Request");
            System.out.println(".........................");
            System.out.println("3. Go back");
            switch (readChoice()){
              case 1:
                connectionid = unnaccpetedRequests.get(choice-1).get(0);
                System.out.printf("Accepting friend request...", connectionid);
                esql.AcceptRequest(esql, authorisedUser, connectionid);
                redoQuery = true;
                Sleep(2000);
                break;
              case 2:
                connectionid = unnaccpetedRequests.get(choice-1).get(0);
                System.out.printf("Denying friend request...", connectionid);
                esql.DenyRequest(esql, authorisedUser, connectionid);
                redoQuery = true;
                Sleep(2000);
                break;
              case 3:
                break;
              default :
                System.out.println("Unrecognized choice!"); break;
            }
          }
          else{
            // Invalid selection.
            System.out.println("\tInvalid Selection");
            redoQuery = false;
            continue;
          }
        }


     }catch(Exception e){
       System.err.println (e.getMessage ());
       return;
     }
   }


// Rest of the functions definition go in here

}//end ProfNetwork
