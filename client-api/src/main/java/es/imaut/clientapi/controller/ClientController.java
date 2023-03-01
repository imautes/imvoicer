package es.imaut.clientapi.controller;

import es.imaut.clientapi.domain.ClientResponse;
import es.imaut.clientapi.domain.CreateClientRequest;
import es.imaut.clientapi.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.json.JsonMergePatch;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService service;

    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAll() {
        return ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        return ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        return ok(service.create(request));
    }

    @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id, @RequestBody JsonMergePatch patch) {
        return ok(service.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ok().build();
    }
}
