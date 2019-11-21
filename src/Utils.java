import com.javarush.task.task30.task3008.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Utils {
    public static String[] createRoomHistory(int kolvo, int roomId, Map<Date, Message> map) {
        List<String> msgs = new ArrayList<>();
        if (map.size() == 0) {
            return new String[0];
        } else {
            for (Map.Entry<Date, Message> kek : map.entrySet()) {
                if (msgs.size() != kolvo * 4) {
                    if (roomId == Integer.parseInt(kek.getValue().getRoomId())) {
                        msgs.add(String.valueOf(kek.getValue().getType()));
                        msgs.add(kek.getValue().getSender());
                        msgs.add(String.valueOf(kek.getKey().getTime()));
                    }
                }
            }
            return msgs.toArray(new String[0]);
        }
    }
}

