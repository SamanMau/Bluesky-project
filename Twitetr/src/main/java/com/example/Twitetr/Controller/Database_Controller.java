package com.example.Twitetr.Controller;
import java.sql.*;

import io.github.cdimascio.dotenv.Dotenv;


public class Database_Controller{
    private BlueSky_Controller controller;

    public Database_Controller(BlueSky_Controller controller){
        this.controller = controller;
    }

    public boolean checkIfUserExists(String username, String password){
        String name = getUsername();
        String sqlpassword = getpassword();
        int number = -1;

        try (Connection con = DriverManager.getConnection(
            "jdbc:postgresql://pgserver.mau.se:5432/" + name, name, sqlpassword);

        CallableStatement callableStatement = con.prepareCall("{ ? = call checkIfExists(?) }")) {
        callableStatement.registerOutParameter(1, Types.INTEGER);
        callableStatement.setString(2, username);
        callableStatement.execute();

        number = callableStatement.getInt(1);

        if(number == 1){
            System.out.println("database_controller: jag är inne här mannen");
            return true;
        } else if(number == 0){
            return false;
        }

        callableStatement.close();
        con.close();
        return false;

    } catch (SQLException e) {
        e.printStackTrace();
    }
        
        return false;
    }

    public void signUpUser(String username, String password){
        String name = getUsername();
        String sqlpassword = getpassword();

        try (Connection con = DriverManager.getConnection(
                "jdbc:postgresql://pgserver.mau.se:5432/"+name, name, sqlpassword);

            CallableStatement callableStatement = con.prepareCall("call registerUser(?, ?)")) {
            callableStatement.setString(1, username);
            callableStatement.setString(2, password);
            callableStatement.executeUpdate();
            callableStatement.close();
            con.close();

            

        } catch (SQLException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }


    public String getURL(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String userName = dotenv.get("SQL_URL");
        return userName;
    }

    public String getUsername(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String userName = dotenv.get("SQL_NAME");
        return userName;
    }

    public String getpassword(){
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .filename(".env")
                .load();

        String userName = dotenv.get("SQL_PASSWORD");
        return userName;
    }

}