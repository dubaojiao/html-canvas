package com.du.htmlcanvas.controller;

import com.du.htmlcanvas.service.WebSocketServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title
 * @ClassName RoomController
 * @Author jsb_pbk
 * @Date 2018/9/17
 */
@RestController
public class RoomController {
    @GetMapping(value = "checkRoomNumber")
    public Map<String,Object> checkRoomNumber(String roomNumber){
        Map<String,Object> map = new HashMap<>(2);
        if(WebSocketServer.checkRoomNumber(roomNumber)){
            map.put("state",1);
            map.put("msg","创建成功");
        }else {
            map.put("state",2);
            map.put("msg","房号冲突");
        }
        return map;
    }

    @GetMapping(value = "checkRoomNumberAndPwd")
    public Map<String,Object> checkRoomNumberAndPwd(String roomNumber,String roomPwd){
        Map<String,Object> map = new HashMap<>(2);
        if(WebSocketServer.checkRoomNumberAndPwd(roomNumber,roomPwd)){
            map.put("state",1);
        }else {
            map.put("state",2);
            map.put("msg","房号或者密码错误");
        }
        return map;
    }
}
