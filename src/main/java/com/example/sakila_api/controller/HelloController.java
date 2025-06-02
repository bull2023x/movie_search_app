// src/main/java/com/example/sakila_api/controller/HelloController.java
package com.example.sakila_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  @GetMapping("/hello")
  public String hello() {
    return "Hello, world!";
  }
}

