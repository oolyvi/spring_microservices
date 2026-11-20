package com.atm.notificationservice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationRoutingService routingService;

    public NotificationController(NotificationRoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody Notification request) {
        /*
        http://localhost:8087/notification/send

        {
            "type": "EMAIL",
            "recipient": "recipient@gmail.com",
            "title": "Title",
            "message": "Perfect message!"
        }
         */
        try {
            routingService.routeNotification(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
        return ResponseEntity.ok("[ATM_DEBUG]: Notification was sent!");
    }
}