import com.javarush.task.task30.task3008.HardMessage;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;


public class Connection implements Closeable {
    public final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        writer = new PrintWriter(socket.getOutputStream(), true);
        writer.flush();
        InputStream in1 = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(in1));
    }

    public void send(Message message) throws IOException{
        try {
            if (!(message.getType() == MessageType.HARD_MESSAGE_WITH_ARRAY_OF_ROOMS || message.getType() == MessageType.USERS_LIST || message.getType() == MessageType.HISTORY)) {
                JSONObject messageJSON = new JSONObject();
                messageJSON.put("type", message.getType().toString());
                if(message.getData() != null) {
                    messageJSON.put("data", message.getData());
                }
                if(message.getRoomId() != null) {
                    messageJSON.put("roomid", message.getRoomId());
                }
                if(message.getSender() != null){
                    messageJSON.put("sender", message.getSender());
                }
                ConsoleHelper.writeMessage("Sending: " + messageJSON.toString());
                writer.println(messageJSON.toString());
            } else {
                JSONObject messageJSON = new JSONObject();
                messageJSON.put("type", message.getType().toString());
                if(message.getData() != null) {
                    messageJSON.put("data", message.getData());
                }
                if(message.getRoomId() != null) {
                    messageJSON.put("roomid", message.getRoomId());
                }
                if(message.getSender() != null){
                    messageJSON.put("sender", message.getSender());
                }
                JSONArray jsonArray = new JSONArray();
                for (String j : ((HardMessage) message).getStuff()) {
                    jsonArray.put(j);
                }
                messageJSON.put("array", jsonArray);
                ConsoleHelper.writeMessage("Sending: " + messageJSON.toString());
                writer.println(messageJSON.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendRooms(List<Room> rooms){
        synchronized (writer) {
            JSONArray jsonArray = new JSONArray();
            for (Room m : rooms) {
                jsonArray.put(m.createJSON());
            }
            JSONObject obj = new JSONObject();
            obj.put("type", MessageType.HARD_MESSAGE_WITH_ARRAY_OF_ROOMS);
            obj.put("array", jsonArray);
            String res = obj.toString();
            ConsoleHelper.writeMessage("Sending: " + res);
            writer.println(res);
        }
    }
    public void sendHistory(Map<Date, Message> messages, int number, int roomId){
        synchronized (writer){
            List<JSONObject> h = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            int i = 1;
            for(Map.Entry<Date, Message> j: messages.entrySet()){
                if(Integer.parseInt(j.getValue().getRoomId()) == roomId) {
                    h.add(j.getValue().createJSON(j.getKey()));
                    i++;
                }
                if(i == number){
                    break;
                }
            }
            Collections.reverse(h);
            for(JSONObject a: h){
                jsonArray.put(a);
            }
            JSONObject root = new JSONObject();
            root.put("array", jsonArray);
            root.put("type", MessageType.HISTORY);
            String res = root.toString();
            ConsoleHelper.writeMessage("Sending: " + res);
            writer.println(res);
        }
    }

    public Message receive() throws IOException{
        String result = "";
        synchronized (reader) {
            try {

                String userInput = "";
                while (true) {
                    if (socket.isConnected() && !(userInput = reader.readLine()).equals("\\n")) {
                        result += userInput;
                        break;
                    }else if(userInput.equals("\\n")){
                        ;;
                    } else {
                        Thread.sleep(1);
                    }
                }
                ConsoleHelper.writeMessage("Received: " + result);
                JSONObject json = new JSONObject(result);

                if(json.has("array")){
                    HardMessage hardMessage = new HardMessage();
                    List<String> lol = new ArrayList<>();
                    JSONArray array = (JSONArray) json.get("array");
                    for(int i = 0; i < array.length(); i++){
                        lol.add((String) array.get(i));
                    }
                    hardMessage.setType(MessageType.valueOf((String) json.get("type")));
                    hardMessage.setStuff(lol.toArray(new String[0]));
                    if(json.has("data")){
                        hardMessage.setData((String) json.get("data"));
                    }
                    if(json.has("roomid")){
                        hardMessage.setRoomId((String) json.get("roomid"));
                    }
                    if(json.has("sender")){
                        hardMessage.setSender((String) json.get("sender"));
                    }
                    return hardMessage;
                }else {
                    HardMessage message = new HardMessage();
                    message.setType(MessageType.valueOf((String) json.get("type")));
                    if(json.has("data")){
                        message.setData((String) json.get("data"));
                    }
                    if(json.has("roomid")){
                        message.setRoomId((String) json.get("roomid"));
                    }
                    if(json.has("sender")){
                        message.setSender((String) json.get("sender"));
                    }
                    return message;
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public SocketAddress getRemoteSocketAddress(){
        return this.socket.getRemoteSocketAddress();
    }

    public void close() throws IOException{
        writer.close();
        reader.close();
        this.socket.close();
    }
}