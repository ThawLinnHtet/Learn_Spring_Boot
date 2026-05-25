package com.example.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProxyController {
    private final Service2Client service2Client;

    public ProxyController(Service2Client service2Client) {
        this.service2Client = service2Client;
    }

    @GetMapping("/proxy")
    public String proxy(){
        return service2Client.fetchData();
    }
}
