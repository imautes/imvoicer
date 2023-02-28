package es.imaut.clientapi.controller;

import es.imaut.clientapi.domain.ClientDetails;
import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService service;

    @GetMapping
    public ResponseEntity<List<ClientDetails>> findAll() {
        return ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<ClientDetails> create(@Valid @RequestBody CreateClientRequest request) {
        return ok(service.create(request));
    }
}
