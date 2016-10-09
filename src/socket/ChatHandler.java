package socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yongboy.socketio.server.IOHandlerAbs;
import com.yongboy.socketio.server.transport.GenericIO;
import com.yongboy.socketio.server.transport.IOClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;

/**
 * Desc:
 * Mail: hehaiyang@terminus.io
 * Date: 2016/10/9
 */

public class ChatHandler extends IOHandlerAbs {
    private Logger log = Logger.getLogger(this.getClass());
    private ConcurrentMap<String, String> nicknames = new ConcurrentHashMap();

    public ChatHandler() {
    }

    public void OnConnect(IOClient client) {
        this.log.debug("A user connected :: " + client.getSessionID());
    }

    public void OnDisconnect(IOClient client) {
        this.log.debug("A user disconnected :: " + client.getSessionID() + " :: hope it was fun");
        GenericIO genericIO = (GenericIO)client;
        Object nickNameObj = genericIO.attr.get("nickName");
        if(nickNameObj != null) {
            String nickName = nickNameObj.toString();
            this.nicknames.remove(nickName);
            this.emit("announcement", nickName + "  disconnected");
            this.emit("nicknames", (Map)this.nicknames);
        }
    }

    public void OnMessage(IOClient client, String oriMessage) {
        this.log.debug("Got a message :: " + oriMessage + " :: echoing it back to :: " + client.getSessionID());
        String jsonString = oriMessage.substring(oriMessage.indexOf(123));
        jsonString = jsonString.replaceAll("\\\\", "");
        this.log.debug("jsonString " + jsonString);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        String eventName = jsonObject.get("name").toString();
        String nickName;
        if(eventName.equals("nickname")) {
            JSONArray genericIO1 = jsonObject.getJSONArray("args");
            nickName = genericIO1.getString(0);
            if(this.nicknames.containsKey(nickName)) {
                this.handleAckNoticName(client, oriMessage, Boolean.valueOf(true));
            } else {
                this.handleAckNoticName(client, oriMessage, Boolean.valueOf(false));
                this.nicknames.put(nickName, nickName);
                GenericIO argsArray1 = (GenericIO)client;
                argsArray1.attr.put("nickName", nickName);
                this.emit("announcement", nickName + " connected");
                this.emit("nicknames", (Map)this.nicknames);
            }
        } else {
            if(eventName.equals("user message")) {
                GenericIO genericIO = (GenericIO)client;
                nickName = genericIO.attr.get("nickName").toString();
                JSONArray argsArray = jsonObject.getJSONArray("args");
                String message = argsArray.getString(0);
                this.emit(client, eventName, nickName, message);
            }

        }
    }

    private void handleAckNoticName(IOClient client, String oriMessage, Object obj) {
        boolean aplus = oriMessage.matches("\\d:\\d{1,}\\+::.*?");
        if(aplus) {
            String aPlusStr = oriMessage.substring(2, oriMessage.indexOf(43) + 1);
            this.ackNotify(client, aPlusStr, obj);
        }

    }

    public void OnShutdown() {
        this.log.debug("shutdown now ~~~");
    }

    private void emit(String eventName, Map<String, String> nicknames) {
        String content = String.format("5:::{\"name\":\"%s\",\"args\":[%s]}", new Object[]{eventName, JSON.toJSONString(nicknames)});
        super.broadcast(content);
    }

    private void emit(String eventName, String message) {
        String content = String.format("5:::{\"name\":\"%s\",\"args\":[\"%s\"]}", new Object[]{eventName, message});
        super.broadcast(content);
    }

    private void emit(IOClient client, String eventName, String message, String message2) {
        String content = String.format("5:::{\"name\":\"%s\",\"args\":[\"%s\",\"%s\"]}", new Object[]{eventName, message, message2});
        super.broadcast(client, content);
    }
}

