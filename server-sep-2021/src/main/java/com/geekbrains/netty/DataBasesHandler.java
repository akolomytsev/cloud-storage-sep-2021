package com.geekbrains.netty;

// Класс управления базой данных
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBasesHandler {

    private static final String CONNECT_BASES = "jdbc:postgresql://localhost:5432/cloudstoragedb";
    private static final String user = "postgres";
    private static final String password = "123123";
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement insertSet; // переменная для вставки в базу данных нового пользователя
    private static PreparedStatement selectSet;// переменная для выбора в базе данных
    private static PreparedStatement selNickSet;// переменная для выбора
    //private static PreparedStatement upNick;// переменная для обновления имени пользователя
   // private static PreparedStatement findNick;// переменная для получения номера клиента
    private ResultSet rs;
    private ResultSet nickRS;

    private static void prepareAllStatements() throws SQLException {
        insertSet = connection.prepareStatement("INSERT INTO csdb.users (login, password) VALUES (?, ?);"); // запрос для добавления нового пользователя в бд
        selectSet = connection.prepareStatement("SELECT login FROM csdb.users WHERE login=? AND password=?;"); // запрос для выбора в бд логина и пароля по логину
        selNickSet = connection.prepareStatement("SELECT login FROM csdb.users WHERE login=? AND password=?;"); // задать ник = логину
       // upNick = connection.prepareStatement("UPDATE csdb.users SET nickname=? WHERE nickname=?;"); //
        //findNick = connection.prepareStatement("SELECT nickname FROM csdb.users WHERE id=?;"); // запрос порядкового номера клиента
    }

    //подключение к базе
    public boolean dbConnect() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(CONNECT_BASES, user, password);
        statement = connection.createStatement();
        rs = statement.executeQuery("SELECT * FROM csdb");
        prepareAllStatements();
        return true;
    }

// регистрация нового пользователя (если пользователь существует - лож)
    public boolean registration(String login, String password) throws SQLException {
        try {
            selectSet.setString(1, login);
            selectSet.setString(2, password);
            selectSet.executeQuery();
            rs = selectSet.executeQuery();
            rs.next();
            if (login.equals(rs.getString("login")))
                return false;
        } catch (SQLException e){
            e.printStackTrace();
        }

        insertSet.setString(1, login);
        insertSet.setString(2, password);
        insertSet.executeUpdate();
        return true;
    }

    //вход в свою учетную запись по login and password
    public String getLoginAndPassword(String login, String password) throws SQLException {
        selNickSet.setString(1, login);
        selNickSet.setString(2, password);
        selNickSet.executeQuery();
        nickRS = selNickSet.executeQuery();// как поставить логин?
        nickRS.next();
        return nickRS.getString("login");
    }

//    public static void main(String[] args) throws Exception {
//        Class.forName("org.postgresql.Driver");
//        Connection connection = DriverManager.getConnection(CONNECT_BASES, user, password);
//        Statement  stmt = connection.createStatement();
//        ResultSet rs = stmt.executeQuery("SELECT * FROM csdb");
//        while (rs.next()){
//            System.out.print(rs.getLong("id") + " ");
//            System.out.print(rs.getString("login") + " ");
//            System.out.print(rs.getString("password"));
//        }
//
//        rs.close();
//        stmt.close();
//        connection.close();
//    }


    // закрытие соединения с базой
    public void disconnect() {
        try {
            insertSet.close();
            selectSet.close();
            selNickSet.close();
//            upNick.close();
//            findNick.close();
            connection.close();
            statement.close();
            System.out.println("Disconnected from database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
