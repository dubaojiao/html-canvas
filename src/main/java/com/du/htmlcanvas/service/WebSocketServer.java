package com.du.htmlcanvas.service;

import com.du.htmlcanvas.util.JSONUtil;
import com.du.htmlcanvas.util.TextUtil;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/websocket/{key}")
@Component
public class WebSocketServer {
    static   Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    private static Map<String,WebSocketServer> webSocketMap = new ConcurrentHashMap<String,WebSocketServer>();

    private static final String SP = "&&";
    private static final String ADD = "add";
    private static final String JOIN  = "join";

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private String oneKey;
    private String towKey;
    private String roomNumber;
    private String roomPwd;
    private String answer;
    private List<String> msg = new ArrayList<>();

    public static boolean checkRoomNumber(String roomNumber){
        for(Map.Entry<String,WebSocketServer> m : webSocketMap.entrySet()){
            if(m.getValue().roomNumber.equals(roomNumber)){
                return false;
            }
        }
        return true;
    }

    public static boolean checkRoomNumberAndPwd(String roomNumber,String pwd){
        for(Map.Entry<String,WebSocketServer> m : webSocketMap.entrySet()){
            if(m.getValue().roomNumber.equals(roomNumber)){
                if(m.getValue().roomPwd.equals(pwd)){
                    return true;
                }else {
                    return false;
                }
            }
        }
        return false;
    }


      /**连接建立成功调用的方法*/
      @OnOpen
     public void onOpen(@PathParam("key") String key, Session session) {
          if(key == null || key == ""){
            throw new RuntimeException("链接异常");
          }
        String[] msgs = key.split(SP);
          if(msgs.length == 2){
                if(ADD.equals(msgs[0])){
                    System.out.println("add ... " + key);
                    String[] krr = msgs[1].split("\\|");
                    this.oneKey = krr[0];
                    this.session =session;
                    this.roomNumber = krr[1];
                    this.roomPwd = krr[2];
                    this.msg = new ArrayList<>();
                    String m = this.oneKey+"加入房间，等待中...【"+TextUtil.date(new Date())+"】";
                    this.msg.add(m);
                    webSocketMap.put(this.oneKey,this);
                    try {
                        sendInfoByKeyAddOrJion(this.oneKey, JSONUtil.toJson(this.msg));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addOnlineCount();
                    return;
                } else if(JOIN.equals(msgs[0])){
                    System.out.println("join ... " + key);
                    String[] krr = msgs[1].split("\\|");
                    this.oneKey = krr[0];
                    this.session =session;
                    this.roomNumber = krr[1];
                    this.roomPwd = krr[2];

                    for(Map.Entry<String,WebSocketServer> m : webSocketMap.entrySet()){
                        if(m.getValue().roomNumber.equals(this.roomNumber) && m.getValue().roomPwd.equals(this.roomPwd)){
                            this.towKey = m.getValue().oneKey;
                            m.getValue().towKey = this.oneKey;
                            this.msg.addAll(m.getValue().msg);
                            this.msg.add(this.oneKey+"加入房间，可以开始游戏...【"+TextUtil.date(new Date())+"】");
                            webSocketMap.put(this.oneKey,this);
                            addOnlineCount();
                            try {
                                sendInfoByKeyAddOrJion(this.oneKey,  JSONUtil.toJson(this.msg));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                sendInfoByKeyAddOrJion(this.towKey, JSONUtil.toJson(this.msg));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                    throw new RuntimeException("链接异常");
                }
          }else {
              throw new RuntimeException("链接异常");
          }
          System.out.println("Connected ... " + key);
      }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        //从set中删除
        webSocketMap.remove(this.oneKey);
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端的消息:" + message);
        if("text".equals(message)){
            try {
                this.answer = TextUtil.getText();
                session.getBasicRemote().sendText(message+"&&"+ this.answer );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else  if(message.contains("paths")){
            try {
                webSocketMap.get(this.towKey).session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //群发消息
            /*webSocketMap.forEach((k,v) -> {
                try {
                    v.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });*/
        }else if(message.contains("answer")){
            String answer = message.split(SP)[1];
            String m;
            if(answer.equals(webSocketMap.get(this.towKey).answer)){
                 m = answer+"--------"+webSocketMap.get(this.towKey).answer+"猜中";
            }else {
                 m = answer+"--------"+"猜错";
            }
            this.msg.add(m);
            webSocketMap.get(this.towKey).msg.add(m);
            try {
                sendInfoByKeyAddOrJion(this.oneKey, JSONUtil.toJson(this.msg));
                sendInfoByKeyAddOrJion(this.towKey, JSONUtil.toJson(webSocketMap.get(this.towKey).msg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if("close".equals(message)){
            try {
                if(webSocketMap.get(this.towKey) != null){
                    webSocketMap.get(this.towKey).session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }


   public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     * */
    public static void sendInfoByKey(String key,String message) throws IOException {
        log.info(message);
       WebSocketServer webSocketServer =   webSocketMap.get(key);
       if(webSocketServer == null){
            return ;
       }
        try {
            webSocketServer.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*webSocketMap.forEach((k,v) -> {
            try {
                v.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });*/
    }

    /**
     * 群发自定义消息
     * */
    public static void sendInfoByKeyAddOrJion(String key,String message) throws IOException {
        log.info(message);
        WebSocketServer webSocketServer =   webSocketMap.get(key);
        if(webSocketServer == null){
            return ;
        }
        try {
            webSocketServer.sendMessage("AJ&&"+message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*webSocketMap.forEach((k,v) -> {
            try {
                v.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });*/
    }


    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message) throws IOException {
        log.info(message);
        webSocketMap.forEach((k,v) -> {
            try {
                v.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
