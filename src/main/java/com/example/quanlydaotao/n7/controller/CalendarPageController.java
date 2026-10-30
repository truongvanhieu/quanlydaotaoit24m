package com.example.quanlydaotao.n7.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalendarPageController {

    @GetMapping("/calendar.html")
    public String calendar() {
        return "forward:/n7/calendar.html";
    }
}
