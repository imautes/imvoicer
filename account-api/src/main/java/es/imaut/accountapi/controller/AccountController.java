package es.imaut.accountapi.controller;

import es.imaut.accountapi.domain.AccountResponse;
import es.imaut.accountapi.domain.CreateAccountRequest;
import es.imaut.accountapi.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.json.JsonMergePatch;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll() {
        return ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable Long id) {
        return ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        return ok(service.create(request));
    }

    @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<AccountResponse> update(@PathVariable Long id, @RequestBody JsonMergePatch patch) {
        return ok(service.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ok().build();
    }
}
