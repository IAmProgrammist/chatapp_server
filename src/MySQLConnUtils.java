import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLConnUtils {

    // Connect to MySQL
    public static Connection getMySQLConnection() throws SQLException, ClassNotFoundException {

        String hostName = "localhost";
        String dbName = "rovchat?useUnicode=true&characterEncoding=utf-8";
        String userName = "root";
        String password = "RChat2019";
        //"rvn156258" for local tests
        //"RChat2019" for global release
        return getMySQLConnection(hostName, dbName, userName, password);
    }

    public static Connection getMySQLConnection(String hostName, String dbName, String userName, String password) throws SQLException, ClassNotFoundException {

        Class.forName("com.mysql.jdbc.Driver");
        String connectionURL = "jdbc:mysql://" + hostName + ":3306/" + dbName;
        Connection conn = DriverManager.getConnection(connectionURL, userName, password); //Connection conn = DriverManager.getConnection(connectionURL, userName, password);
        return conn;
    }

    public static Map<LoginNPassword, User> getUsers() {
        try {
            Map<LoginNPassword, User> mapa = new ConcurrentHashMap<>();
            Connection connection = MySQLConnUtils.getMySQLConnection();
            Statement statement = connection.createStatement();
            String sql = "Select login, password, nickname from users";
            ResultSet rsUsers = statement.executeQuery(sql);
            ConsoleHelper.writeMessage("Users: ");
            while (rsUsers.next()) {
                LoginNPassword lnp = new LoginNPassword();
                User user = new User();
                lnp.setLogin(rsUsers.getString("login"));
                lnp.setPassword(rsUsers.getString("password"));
                user.setName(rsUsers.getString("nickname"));
                ConsoleHelper.writeMessage("Login: '" + lnp.getLogin() + "'; Password: '" + lnp.getPassword() + "'; Nickname: '" + user.getName() + "'.");
                mapa.put(lnp, user);
            }
            connection.close();
            ConsoleHelper.writeMessage("Everything seems okay!");
            return mapa;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Room> getRooms() {
        try {
            List<Room> rooms = Collections.synchronizedList(new ArrayList<>());
            // Get Connection
            Connection connection = MySQLConnUtils.getMySQLConnection();
            // Create statement
            Statement statement = connection.createStatement();
            String sql = "Select name, password, roomid from rooms";
            // Execute SQL statement returns a ResultSet object.
            ResultSet rsRooms = statement.executeQuery(sql);
            ConsoleHelper.writeMessage("Rooms: ");
            while (rsRooms.next()) {
                LoginNPassword lnp = new LoginNPassword();
                Integer a = 0;
                String login = rsRooms.getString("name");
                lnp.setLogin(login);
                String password;
                try {
                    password = rsRooms.getString("password");
                }catch (NullPointerException e){
                    password = null;
                }
                if(password == null) {
                    lnp.setPassword("");
                }else{
                    lnp.setPassword(password);
                }
                a = rsRooms.getInt("roomid");
                ConsoleHelper.writeMessage("Name: '" + lnp.getLogin() + "'; Password: '" + lnp.getPassword() + "'; RoomId: '" + a + "'.");
                rooms.add(new Room(lnp.getLogin(), lnp.getPassword(), a));
            }
            connection.close();
            return rooms;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static void createUser(String login, String password, String nickname)  {
        try {
            Connection connection = MySQLConnUtils.getMySQLConnection();
            Statement statement = connection.createStatement();
            String sql = "INSERT INTO users (login, password, nickname) VALUES ('" + login + "','" + password + "','" + nickname + "')";
            int rowCount = statement.executeUpdate(sql);
            ConsoleHelper.writeMessage("Row Count affected = " + rowCount);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void createRoom(String name, String password, String roomid) {
        try {
            Connection connection = MySQLConnUtils.getMySQLConnection();
            Statement statement = connection.createStatement();
            String sql = "INSERT INTO rooms (name, password, roomid) VALUES ('" + name + "','" + password + "','" + roomid + "')";
            int rowCount = statement.executeUpdate(sql);
            ConsoleHelper.writeMessage("Row Count affected = " + rowCount);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
