package com.example.ds2022_30642_steau_iuliu_andrei_2_rabbit.controller;

import com.example.ds2022_30642_steau_iuliu_andrei_2_rabbit.sender.RabbitSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class RabbitController implements CommandLineRunner {
    private final RabbitSender rabbitMqSender;
    @Autowired
    public RabbitController(RabbitSender rabbitSender) {
        this.rabbitMqSender = rabbitSender;
    }
    @Value("${app.message}")
    private String message;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(message);
        rabbitMqSender.send();
    }

}