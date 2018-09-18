package com.du.htmlcanvas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Title
 * @ClassName PageController
 * @Author jsb_pbk
 * @Date 2018/9/14
 */
@Controller
public class PageController {

    @GetMapping(value = "/")
    public String index(Model model){
        return "index";
    }


    @GetMapping(value = "/paint")
    public String paint(String key,String roomNumber,String roomPwd,Model model){
        model.addAttribute("key", key);
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("roomPwd", roomPwd);
        return "paint";
    }

    @GetMapping(value = "/guess")
    public String guess(String key,String roomNumber,String roomPwd,Model model){
        model.addAttribute("key", key);
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("roomPwd", roomPwd);
        return "guess";
    }
}
