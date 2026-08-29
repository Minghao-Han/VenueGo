package com.ticketing.order;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MetricsController {

    @GetMapping("/metrics")
    public ResponseEntity<Void> metrics() {
        return ResponseEntity.status(302).header("Location", "/actuator/prometheus").build();
    }
}
