package com.example.service;

import com.example.pojos.Pojo1;

public class Service1 {

	public Pojo1 echo(String input) {
		return new Pojo1("ECHO", input);
	}
}
