import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Room {
    private String name;
    private String password;
    private Integer roomId;
    List<String> users = Collections.synchronizedList(new ArrayList<>());

    public Room(String name, String password, Integer roomId) {
        this.name = name;
        this.password = password;
        this.roomId = roomId;
    }

    public List<String> getUsers() {
        return users;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Integer getUsersNumber(){
        return users.size();
    }
    public void addUser(String name) throws Exception {
        if(users.contains(name)){
            ConsoleHelper.writeMessage("User already in room " + name);
        }else {
            users.add(name);
        }
    }
    public void removeUser (String name) throws Exception {
        if(!users.contains(name)){
            ConsoleHelper.writeMessage("There is no such user in room " + name);
        }else {
            users.remove(name);
        }
    }
    public JSONObject createJSON(){
        JSONObject messageJSON = new JSONObject();
        messageJSON.put("name", name);
        if(password.equals("")){
            messageJSON.put("lock", "false");
        }else {
            messageJSON.put("lock", "true");
        }
        messageJSON.put("usersnum", getUsersNumber());
        return messageJSON;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(name, room.name) &&
                Objects.equals(password, room.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, password);
    }
}
