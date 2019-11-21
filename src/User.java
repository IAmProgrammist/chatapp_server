import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private Connection connection;
    private String name;
    private String roomId = "";

    public User() {
    }

    public User(Connection connection, String name) {
        this.connection = connection;
        this.name = name;
    }
    public Connection getConnection() {
        return connection;
    }

    public String getName() {
        return name;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(connection, user.connection) &&
                Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection, name);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
