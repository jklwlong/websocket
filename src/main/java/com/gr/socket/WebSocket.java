package com.gr.socket;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: liaoshiyao
 * @Date: 2019/1/11 11:48
 * @Description: websocket 服务类
 */

/**
 * @ServerEndpoint 这个注解有什么作用？
 * <p>
 * 这个注解用于标识作用在类上，它的主要功能是把当前类标识成一个WebSocket的服务端
 * 注解的值用户客户端连接访问的URL地址
 */

@Slf4j
@Component
@ServerEndpoint("/websocket/{name}")
public class WebSocket {

    /**
     * 与某个客户端的连接对话，需要通过它来给客户端发送消息
     */
    private Session session;
    /**
     * 标识当前连接客户端的用户名
     */
    private String name;
    /**
     * 用于存所有的连接服务的客户端，这个对象存储是安全的
     */
    private static ConcurrentHashMap<String, WebSocket> webSocketSet = new ConcurrentHashMap<>();


    @OnOpen
    public void OnOpen(Session session, @PathParam(value = "name") String name) {
        this.session = session;
        this.name = name;
        webSocketSet.put(name, this);
        log.info("[WebSocket] 连接成功，当前连接人数为：{}", webSocketSet.size());
        getzx();
    }

    @OnClose
    public void OnClose() {
        webSocketSet.remove(this.name);
        log.info("[WebSocket] 退出成功，当前连接人数为：{}", webSocketSet.size());
        getzx();
    }

    @OnMessage
    public void OnMessage(String message) {
        log.info("[WebSocket] 收到消息：{}", message);
        if (message.indexOf("TOUSER") == 0) {
            String name = message.substring(message.indexOf("TOUSER") + 6, message.indexOf(";"));
            AppointSending(name, message.substring(message.indexOf(";") + 1));
        } else {
            GroupSending(message);
        }

    }

    private void getzx() {
        StringBuilder zx = new StringBuilder();
        for (String auth : webSocketSet.keySet()) {
            if (!StringUtils.isEmpty(auth)) {
                zx.append("<p>").append(auth).append("</p>");
            }
        }
        for (String auth : webSocketSet.keySet()) {
            try {
                webSocketSet.get(auth).session.getBasicRemote().sendText(zx.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 群发
     *
     * @param message
     */
    public void GroupSending(String message) {
        JSONObject json = JSONObject.parseObject(message);
        String msg = json.getString("auth").concat("说：").concat(json.getString("msg"));
        for (String name : webSocketSet.keySet()) {
            try {
                webSocketSet.get(name).session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定发送
     *
     * @param name
     * @param message
     */
    public void AppointSending(String name, String message) {
        try {
            webSocketSet.get(name).session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
