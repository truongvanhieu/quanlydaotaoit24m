package com.example.quanlydaotao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.quanlydaotao.n7.controller.CalendarPageController;

@SpringBootTest
@ActiveProfiles("test")
class QuanlydaotaoApplicationTests {

	@Autowired
	private CalendarPageController calendarPageController;

	@Test
	void contextLoads() {
	}

	@Test
	void calendarPageKeepsItsOriginalUrl() {
		assertEquals("forward:/n7/calendar.html", calendarPageController.calendar());
	}

}
