package com.example.ds2022_30642_steau_iuliu_andrei_2_rabbit.sender;


import com.example.ds2022_30642_steau_iuliu_andrei_2_rabbit.entity.EnergyConsumption;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RabbitSender {
    private final File file = new File("sensor.csv");
    private final JSONParser jsonParser = new JSONParser();
    private final JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("config.json"));
    private final RabbitTemplate rabbitTemplate;
    @Autowired
    public RabbitSender(RabbitTemplate rabbitTemplate) throws IOException, ParseException {
        this.rabbitTemplate = rabbitTemplate;
    }
    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.routingkey}")
    private String routingkey;

    public synchronized void send() {
        JSONArray jsonArray = (JSONArray) jsonObject.get("devices");
        for (Object o : jsonArray) {
            JSONObject x = (JSONObject)o;
                Thread thread = new Thread(() -> execute(Long.parseLong((String) x.get("deviceId"))));
                thread.start();

        }
    }


    private void execute(Long deviceId) {
            try {
                Scanner scan = new Scanner(file);
                AtomicInteger k= new AtomicInteger(0);
                while(scan.hasNextLine()) {
                    Float value = scan.nextFloat();
                    EnergyConsumption energyConsumption = new EnergyConsumption();

                    energyConsumption.setDeviceId(deviceId);
                    energyConsumption.setTimestamp(Timestamp.from(Instant.now()));
                    energyConsumption.getTimestamp().setMinutes(energyConsumption.getTimestamp().getMinutes() + k.get()*10);
                    energyConsumption.setEnergyUsed(value);

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

                    String data = objectMapper.writeValueAsString(energyConsumption);
                    rabbitTemplate.convertAndSend(exchange,routingkey,data);
                    k.addAndGet(1);
                    Thread.sleep(5000); // 600 000 milliseconds for 10 minutes
                }



            } catch (FileNotFoundException | JsonProcessingException | InterruptedException e) {
                throw new RuntimeException(e);
            }


    }
}