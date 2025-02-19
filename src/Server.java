import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, User> connectionMap = new ConcurrentHashMap<>();
    private static Map<LoginNPassword, User> users = new ConcurrentHashMap<>();
    private static List<Room> rooms = Collections.synchronizedList(new ArrayList<>());
    public static Map<Date, Message> peepeepoopoo = Collections.synchronizedMap(new TreeMap<>(Collections.reverseOrder()));
    private static Integer roomsId = 0;
    private static String versionName = "1.5";


    private static class Handler extends Thread {
        Thread checker;
        public boolean connected;


        private String[] getAllRooms() {
            List<String> room = new ArrayList<>();
            for (Room m : rooms) {
                room.add(m.getName());
                if (doesRoomHavePassword(m.getName())) {
                    room.add("true");
                } else {
                    room.add("false");
                }
            }
            return room.toArray(new String[0]);
        }

        public void throwException() throws TimeToExitBruhException {
            throw new TimeToExitBruhException();
        }

        public void run() {
            User user = new User();
            ConsoleHelper.writeMessage("Подключение установлено с: " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)) {
                HardMessage msg = new HardMessage();
                String nickname;
                Message tmpaMessage = new Message();
                tmpaMessage.setType(MessageType.CONNECTED);
                tmpaMessage.setSender(versionName);
                tmpaMessage.setData("*Убран эльфийский язык, теперь RChat умеет говорить по-русски!" + "\n" + "*Мелкие устранения ошибок!" + "\n" + "*Подробнее на сайте");
                connection.send(tmpaMessage);
                LoginNPassword userln = null;
                LoginNPassword userre = null;
                LoginNPassword roomlo = null;
                LoginNPassword roomcr = null;
                Integer roomId = null;
                while (true) {
                    msg.setType(MessageType.IGNORE);
                    try {
                        msg = (HardMessage) connection.receive();
                    } catch (Exception e) {
                        throw new TimeToExitBruhException();
                    }
                    if (msg.getType() == MessageType.LOGIN_ROOM_IN_CHECK) {
                        connection.sendRooms(rooms);
                    } else if (msg.getType() == MessageType.CREATE_ROOM_IN_CHECK) {
                        connection.sendRooms(rooms);
                    } else if (msg.getType() == MessageType.HARD_MESSAGE_WITH_ARRAY_OF_LOGIN_USER) {
                        userre = null;
                        userln = new LoginNPassword();
                        userln.setLogin(msg.getStuff()[0]);
                        userln.setPassword(msg.getStuff()[1]);
                        User userlol = returnAccountWithPasswordAndLogin(userln.getLogin(), userln.getPassword());
                        if (userlol == null) {
                            Message tmpMessage = new Message();
                            tmpMessage.setType(MessageType.CHECK_USER_LOGIN_NO);
                            connection.send(tmpMessage);
                        } else {
                            Message tmpMessage = new Message();
                            tmpMessage.setType(MessageType.CHECK_USER_LOGIN_YES);
                            tmpMessage.setData(users.get(userln).getName());
                            connection.send(tmpMessage);
                        }
                    } else if (msg.getType() == MessageType.HARD_MESSAGE_WITH_ARRAY_OF_REGISTER_USER) {
                        userln = null;
                        userre = new LoginNPassword();
                        userre.setLogin(msg.getStuff()[0]);
                        userre.setPassword(msg.getStuff()[1]);
                        nickname = msg.getStuff()[2];
                        User userlol = returnAccountWithPasswordAndLogin(userre.getLogin(), userre.getPassword());
                        boolean doesAccountExist = doesAccountExist(userre.getLogin(), nickname);
                        if (userlol == null && !doesAccountExist) {
                            users.put(userre, new User(null, nickname));
                            Message tmpMessage = new Message();
                            tmpMessage.setType(MessageType.CHECK_USER_REGISTER_YES);
                            tmpMessage.setData(users.get(userre).getName());
                            connection.send(tmpMessage);
                            MySQLConnUtils.createUser(userre.getLogin(), userre.getPassword(), nickname);
                        } else {
                            Message tmpMessage = new Message();
                            tmpMessage.setType(MessageType.CHECK_USER_REGISTER_NO);
                            connection.send(tmpMessage);
                        }
                    } else if (msg.getType() == MessageType.HARD_MESSAGE_WITH_ARRAY_OF_LOGIN_ROOM) {
                        roomcr = null;
                        roomlo = new LoginNPassword();
                        roomlo.setLogin(msg.getStuff()[0]);
                        roomlo.setPassword(msg.getStuff()[1]);
                        if (doesRoomHavePassword(roomlo.getLogin())) {
                            Integer a = getRoomByLoginAndPassword(roomlo.getLogin(), roomlo.getPassword());
                            if (a == null) {
                                Message tmpMessage = new Message();
                                tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_DOESNT_EXIST);
                                connection.send(tmpMessage);
                                roomlo = null;
                            } else {
                                Message tmpMessage = new Message();
                                tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_EXISTS);
                                tmpMessage.setData(roomlo.getLogin());
                                for (Room m : rooms) {
                                    if (m.getName().equals(roomlo.getLogin())) {
                                        roomId = m.getRoomId();
                                    }
                                }
                                connection.send(tmpMessage);
                                break;
                            }
                        } else if (!doesRoomHavePassword(roomlo.getLogin())) {
                            if (!roomlo.getPassword().equalsIgnoreCase("")) {
                                Message tmpMessage = new Message();
                                tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_DOESNT_EXIST);
                                connection.send(tmpMessage);
                                roomlo = null;
                            } else {
                                Integer a = getRoomIdByLogin(roomlo.getLogin());
                                if (a == null) {
                                    Message tmpMessage = new Message();
                                    tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_DOESNT_EXIST);
                                    connection.send(tmpMessage);
                                    roomlo = null;
                                } else {
                                    Message tmpMessage = new Message();
                                    tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_EXISTS);
                                    tmpMessage.setData(roomlo.getLogin());
                                    for (Room m : rooms) {
                                        if (m.getName().equals(roomlo.getLogin())) {
                                            roomId = m.getRoomId();
                                        }
                                    }
                                    connection.send(tmpMessage);
                                    break;
                                }
                            }
                        }
                    } else if (msg.getType() == MessageType.HARD_MESSAGE_WITH_ARRAY_OF_REGISTER_ROOM) {
                        roomlo = null;
                        roomcr = new LoginNPassword();
                        roomcr.setLogin(msg.getStuff()[0]);
                        roomcr.setPassword(msg.getStuff()[1]);
                        if (roomcr.getPassword().equals("") && !doesRoomExist(roomcr.getLogin())) {
                            Message tmpMessage = new Message();
                            tmpMessage.setType(MessageType.CREATE_ROOM_OKAY);
                            tmpMessage.setData(roomcr.getLogin());
                            rooms.add(new Room(roomcr.getLogin(), roomcr.getPassword(), ++roomsId));
                            MySQLConnUtils.createRoom(roomcr.getLogin(), roomcr.getPassword(), String.valueOf(roomsId));
                            for (Room m : rooms) {
                                if (m.getName().equals(roomcr.getLogin())) {
                                    roomId = m.getRoomId();
                                }
                            }
                            connection.send(tmpMessage);
                            break;
                        } else if (!roomcr.getPassword().equals("") && !doesRoomExist(roomcr.getLogin())) {
                            Message tmpMessage = new Message();
                            tmpMessage.setType(MessageType.CREATE_ROOM_OKAY);
                            tmpMessage.setData(roomcr.getLogin());
                            rooms.add(new Room(roomcr.getLogin(), roomcr.getPassword(), ++roomsId));
                            MySQLConnUtils.createRoom(roomcr.getLogin(), roomcr.getPassword(), String.valueOf(roomsId));
                            for (Room m : rooms) {
                                if (m.getName().equals(roomcr.getLogin())) {
                                    roomId = m.getRoomId();
                                }
                            }
                            connection.send(tmpMessage);
                            break;
                        } else {
                            Message tmpMessage = new Message();
                            tmpMessage.setType(MessageType.ROOM_CREATE_EXISTS);
                            connection.send(tmpMessage);
                            roomcr = null;
                        }
                    }
                }
                if (userln == null) {
                    user = users.get(userre);
                    ConsoleHelper.writeMessage(String.format("Зарегистрирован новый пользователь с логином: '%s' и паролем: '%s'", userre.getLogin(), userre.getPassword()));
                } else {
                    user = users.get(userln);
                    ConsoleHelper.writeMessage(String.format("В аккаунт вошел пользователь с логином: '%s' и паролем: '%s'", userln.getLogin(), userln.getPassword()));
                }

                if (roomlo == null) {
                    ConsoleHelper.writeMessage(String.format("Зарегистрирована новая комната с именем: '%s' и паролем: '%s'", roomcr.getLogin(), roomcr.getPassword()));
                } else {
                    ConsoleHelper.writeMessage(String.format("В комнату вошел пользователь с именем: '%s' и паролем: '%s'", roomlo.getLogin(), roomlo.getPassword()));
                }
                while (true) {
                    user.setRoomId(String.valueOf(roomId));
                    user.setConnection(connection);
                    connection.sendHistory(peepeepoopoo, 250, roomId);
                    checker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!isInterrupted()) {
                                try {
                                    Message msg = new Message();
                                    msg.setType(MessageType.CHECK_CONN);
                                    try {
                                        connection.send(msg);
                                    } catch (TimeToExitBruhException e) {
                                        break;
                                    }
                                    Thread.sleep(30000);
                                    if (!connected) {
                                        connection.throwException();
                                    }
                                } catch (Exception e) {
                                    connection.throwException();
                                }
                            }
                        }
                    });
                    checker.start();
                    connectionMap.put(user.getName(), user);
                    notifyUsers(connection, user);
                    HardMessage users = new HardMessage();
                    users.setStuff(getUsersList(user));
                    users.setType(MessageType.USERS_LIST);
                    connection.send(users);
                    serverMainLoop(connection, user);
                    ConsoleHelper.writeMessage("СерверМэйнЛуп загнувся.");
                    checker.stop();
                    connectionMap.remove(user.getName());
                    Message tmppMessage = new Message();
                    tmppMessage.setType(MessageType.USER_REMOVED);
                    tmppMessage.setSender(user.getName());
                    tmppMessage.setRoomId(user.getRoomId());
                    peepeepoopoo.put(new Date(), tmppMessage);
                    removeUser(user.getName(), roomId);
                    sendBroadcastMessage(tmppMessage);
                    while (true) {
                        msg.setType(MessageType.IGNORE);
                        try {
                            msg = (HardMessage) connection.receive();
                        } catch (Exception e) {
                            throw new TimeToExitBruhException();
                        }
                        if (msg.getType() == MessageType.LOGIN_ROOM_IN_CHECK) {
                            connection.sendRooms(rooms);
                        } else if (msg.getType() == MessageType.CREATE_ROOM_IN_CHECK) {
                            connection.sendRooms(rooms);
                        } else if (msg.getType() == MessageType.HARD_MESSAGE_WITH_ARRAY_OF_LOGIN_ROOM) {
                            roomcr = null;
                            roomlo = new LoginNPassword();
                            roomlo.setLogin(msg.getStuff()[0]);
                            roomlo.setPassword(msg.getStuff()[1]);
                            if (doesRoomHavePassword(roomlo.getLogin())) {
                                Integer a = getRoomByLoginAndPassword(roomlo.getLogin(), roomlo.getPassword());
                                if (a == null) {
                                    Message tmpMessage = new Message();
                                    tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_DOESNT_EXIST);
                                    connection.send(tmpMessage);
                                    roomlo = null;
                                } else {
                                    Message tmpMessage = new Message();
                                    tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_EXISTS);
                                    tmpMessage.setData(roomlo.getLogin());
                                    for (Room m : rooms) {
                                        if (m.getName().equals(roomlo.getLogin())) {
                                            roomId = m.getRoomId();
                                        }
                                    }
                                    connection.send(tmpMessage);
                                    break;
                                }
                            } else if (!doesRoomHavePassword(roomlo.getLogin())) {
                                if (!roomlo.getPassword().equalsIgnoreCase("")) {
                                    Message tmpMessage = new Message();
                                    tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_DOESNT_EXIST);
                                    connection.send(tmpMessage);
                                    roomlo = null;
                                } else {
                                    Integer a = getRoomIdByLogin(roomlo.getLogin());
                                    if (a == null) {
                                        Message tmpMessage = new Message();
                                        tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_DOESNT_EXIST);
                                        connection.send(tmpMessage);
                                        roomlo = null;
                                    } else {
                                        Message tmpMessage = new Message();
                                        tmpMessage.setType(MessageType.CHECK_ROOM_LOGIN_EXISTS);
                                        tmpMessage.setData(roomlo.getLogin());
                                        for (Room m : rooms) {
                                            if (m.getName().equals(roomlo.getLogin())) {
                                                roomId = m.getRoomId();
                                            }
                                        }
                                        connection.send(tmpMessage);
                                        break;
                                    }
                                }
                            }
                        } else if (msg.getType() == MessageType.HARD_MESSAGE_WITH_ARRAY_OF_REGISTER_ROOM) {
                            roomlo = null;
                            roomcr = new LoginNPassword();
                            roomcr.setLogin(msg.getStuff()[0]);
                            roomcr.setPassword(msg.getStuff()[1]);
                            if (roomcr.getPassword().equals("") && !doesRoomExist(roomcr.getLogin())) {
                                Message tmpMessage = new Message();
                                tmpMessage.setType(MessageType.CREATE_ROOM_OKAY);
                                tmpMessage.setData(roomcr.getLogin());
                                rooms.add(new Room(roomcr.getLogin(), roomcr.getPassword(), ++roomsId));
                                MySQLConnUtils.createRoom(roomcr.getLogin(), roomcr.getPassword(), String.valueOf(roomsId));
                                for (Room m : rooms) {
                                    if (m.getName().equals(roomcr.getLogin())) {
                                        roomId = m.getRoomId();
                                    }
                                }
                                connection.send(tmpMessage);
                                break;
                            } else if (!roomcr.getPassword().equals("") && !doesRoomExist(roomcr.getLogin())) {
                                Message tmpMessage = new Message();
                                tmpMessage.setType(MessageType.CREATE_ROOM_OKAY);
                                tmpMessage.setData(roomcr.getLogin());
                                rooms.add(new Room(roomcr.getLogin(), roomcr.getPassword(), ++roomsId));
                                MySQLConnUtils.createRoom(roomcr.getLogin(), roomcr.getPassword(), String.valueOf(roomsId));
                                for (Room m : rooms) {
                                    if (m.getName().equals(roomcr.getLogin())) {
                                        roomId = m.getRoomId();
                                    }
                                }
                                connection.send(tmpMessage);
                                break;
                            } else {
                                Message tmpMessage = new Message();
                                tmpMessage.setType(MessageType.ROOM_CREATE_EXISTS);
                                connection.send(tmpMessage);
                                roomcr = null;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    checker.stop();
                }catch (Exception e1){

                }
                connectionMap.remove(user.getName());
                Message tmppMessage = new Message();
                tmppMessage.setType(MessageType.USER_REMOVED);
                tmppMessage.setSender(user.getName());
                tmppMessage.setRoomId(user.getRoomId());
                try {
                    removeUser(user.getName(), Integer.valueOf(user.getRoomId()));
                    peepeepoopoo.put(new Date(), tmppMessage);
                    sendBroadcastMessage(tmppMessage);
                    ConsoleHelper.writeMessage("Пользователь '" + user.getName() + "' отключился.");
                } catch (Exception e1) {
                    ConsoleHelper.writeMessage("Пользователь отключился.");
                }
                this.stop();
            } catch (ClassNotFoundException e) {
                try {
                    checker.stop();
                }catch (Exception e1){

                }
                ConsoleHelper.writeMessage("Класс не найден в Хэндлере");
            } catch (TimeToExitBruhException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    checker.stop();
                }catch (Exception e1){

                }
                connectionMap.remove(user.getName());
                Message tmppMessage = new Message();
                tmppMessage.setType(MessageType.USER_REMOVED);
                tmppMessage.setSender(user.getName());
                tmppMessage.setRoomId(user.getRoomId());
                try {
                    removeUser(user.getName(), Integer.valueOf(user.getRoomId()));
                    peepeepoopoo.put(new Date(), tmppMessage);
                    sendBroadcastMessage(tmppMessage);
                    ConsoleHelper.writeMessage("Пользователь '" + user.getName() + "' отключился.");
                } catch (Exception e1) {
                    ConsoleHelper.writeMessage("Пользователь отключился.");
                }
                this.stop();
            } catch (NullPointerException e) {
                try {
                    checker.stop();
                }catch (Exception e1){

                }
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    connectionMap.remove(user.getName());
                    Message tmppMessage = new Message();
                    tmppMessage.setType(MessageType.USER_REMOVED);
                    tmppMessage.setSender(user.getName());
                    tmppMessage.setRoomId(user.getRoomId());
                    try {
                        removeUser(user.getName(), Integer.valueOf(user.getRoomId()));
                        peepeepoopoo.put(new Date(), tmppMessage);
                        sendBroadcastMessage(tmppMessage);
                        ConsoleHelper.writeMessage("Пользователь '" + user.getName() + "' отключился.");
                    } catch (Exception e1) {
                        ConsoleHelper.writeMessage("Пользователь отключился.");
                    }
                }catch (NullPointerException e1){
                    ConsoleHelper.writeMessage("Пропинговано соединение");
                }
                this.stop();
            }
        }

        private static void removeUser(String name, Integer roomId) {
            Room room = null;
            for (Room m : rooms) {
                if (m.getRoomId() == roomId) {
                    room = m;
                }
            }
            try {
                room.removeUser(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addUser(String name, Integer roomId) {
            Room room = null;
            for (Room m : rooms) {
                if (m.getRoomId() == roomId) {
                    room = m;
                }
            }
            try {
                room.addUser(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String[] getUsersList(User user) {
            Integer a = Integer.valueOf(user.getRoomId());
            Room room = null;
            for (Room m : rooms) {
                if (a == m.getRoomId()) {
                    room = m;
                    break;
                }
            }
            return room.getUsers().toArray(new String[0]);
        }

        private Integer getRoomByLoginAndPassword(String login, String password) {
            for (Room m : rooms) {
                if (m.getName().equalsIgnoreCase(login) && m.getPassword().equals(password)) {
                    return m.getRoomId();
                }
            }
            return null;
        }

        private Integer getRoomIdByLogin(String login) {
            for (Room m : rooms) {
                if (m.getName().equalsIgnoreCase(login)) {
                    return m.getRoomId();
                }
            }
            return null;
        }

        private boolean doesRoomHavePassword(String login) {
            for (Room m : rooms) {
                if (m.getName().equals(login) && !m.getPassword().equals("")) {
                    return true;
                }
            }
            return false;
        }

        private boolean doesRoomExist(String login) {
            for (Room m : rooms) {
                if (m.getName().equalsIgnoreCase(login)) {
                    return true;
                }
            }
            return false;
        }


        private boolean doesAccountExist(String login, String username) {
            for (Map.Entry<LoginNPassword, User> user : users.entrySet()) {
                if (user.getKey().getLogin().equals(login) || user.getValue().getName().equals(username)) {
                    return true;
                }
            }
            return false;
        }

        private User returnAccountWithPasswordAndLogin(String login, String password) {
            LoginNPassword LNP = new LoginNPassword(login, password);
            for (Map.Entry<LoginNPassword, User> user : users.entrySet()) {
                if (LNP.equals(user.getKey())) {
                    return user.getValue();
                }
            }
            return null;
        }


        private void serverMainLoop(Connection connection, User user) throws IOException, ClassNotFoundException, TimeToExitBruhException {
            while (true) {
                Message message = new Message();
                try {
                    message = connection.receive();
                } catch (Exception e) {
                    throw new TimeToExitBruhException();
                }
                if (message.getType() == MessageType.TEXT) {
                    Message fmessage = new Message();
                    fmessage.setType(MessageType.TEXT);
                    fmessage.setData(message.getData());
                    fmessage.setRoomId(user.getRoomId());
                    fmessage.setSender(user.getName());
                    peepeepoopoo.put(new Date(), fmessage);
                    sendBroadcastMessage(fmessage);
                } else if (message.getType() == MessageType.EXIT_PROGRAM) {
                    Message tmpMessage = new Message();
                    tmpMessage.setType(MessageType.YES_YOU_CAN);
                    tmpMessage.setRoomId(user.getRoomId());
                    connection.send(tmpMessage);
                    throw new TimeToExitBruhException();
                } else if (message.getType() == MessageType.I_WANNA_RELOGIN) {
                    Message tmpMessage = new Message();
                    tmpMessage.setType(MessageType.RELOGIN_ROOM);
                    tmpMessage.setRoomId(user.getRoomId());
                    connection.send(tmpMessage);
                    break;
                } else if (message.getType().equals(MessageType.CONN_CONN)) {
                    connected = true;
                }
            }
        }

        private void notifyUsers(Connection connection, User user) throws IOException, TimeToExitBruhException {
            Message tmpMessage = new Message();
            for (Map.Entry<String, User> lol : connectionMap.entrySet()) {
                if (lol.getValue().getRoomId().equals(user.getRoomId())) {
                    tmpMessage = new Message();
                    tmpMessage.setType(MessageType.USER_ADDED);
                    tmpMessage.setSender(user.getName());
                    tmpMessage.setRoomId(user.getRoomId());
                    lol.getValue().getConnection().send(tmpMessage);
                }
            }
            addUser(user.getName(), Integer.valueOf(user.getRoomId()));
            peepeepoopoo.put(new Date(), tmpMessage);
        }

        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


    }

    public static void sendBroadcastMessage(Message message) {
        try {
            for (User user : connectionMap.values()) {
                if (user.getRoomId().equals(message.getRoomId())) {
                    try {
                        user.getConnection().send(message);
                    } catch (TimeToExitBruhException e) {
                        connectionMap.remove(user);
                    }
                }
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Произошла ошибка при попытке отправить сообщение.");
        }
    }


    public static void main(String[] args) throws IOException {
        users = MySQLConnUtils.getUsers();
        rooms = MySQLConnUtils.getRooms();
        rooms = rooms;
        roomsId = rooms.size() - 1;
        int serverPort = 2156;
        InetAddress addr = InetAddress.getLocalHost();
        String myLANIP = addr.getHostAddress();
        ConsoleHelper.writeMessage(myLANIP + " - ваш внешний IP-адрес.");
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Произошла ошибка на сервере.");
            e.printStackTrace();
        }
    }

    private static String getCurrentIP() {
        String result = null;
        try {
            BufferedReader reader = null;
            try {
                URL url = new URL("http://myip.by/");
                InputStream inputStream = null;
                inputStream = url.openStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder allText = new StringBuilder();
                char[] buff = new char[1024];

                int count = 0;
                while ((count = reader.read(buff)) != -1) {
                    allText.append(buff, 0, count);
                }
// Строка содержащая IP имеет следующий вид
// <a href="whois.php?127.0.0.1">whois 127.0.0.1</a>
                Integer indStart = allText.indexOf("\">whois ");
                Integer indEnd = allText.indexOf("</a>", indStart);

                String ipAddress = new String(allText.substring(indStart + 8, indEnd));
                if (ipAddress.split("\\.").length == 4) { // минимальная (неполная)
                    //проверка что выбранный текст является ip адресом.
                    result = ipAddress;
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
