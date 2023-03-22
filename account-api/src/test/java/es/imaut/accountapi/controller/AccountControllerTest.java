package es.imaut.accountapi.controller;

import es.imaut.accountapi.domain.AccountResponse;
import es.imaut.accountapi.domain.CreateAccountRequest;
import es.imaut.accountapi.exception.AccountNotFoundException;
import es.imaut.accountapi.service.AccountService;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.json.JsonMergePatch;
import java.util.List;

import static java.util.Collections.emptySet;
import static javax.json.Json.createMergePatch;
import static javax.json.Json.createObjectBuilder;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({ MockitoExtension.class, RandomBeansExtension.class })
class AccountControllerTest {
    @Mock
    private AccountService service;
    @InjectMocks
    private AccountController controller;

    @Test
    @DisplayName("Find all should call service")
    void findAllShouldCallService() {
        controller.findAll();
        verify(service).findAll();
    }

    @Test
    @DisplayName("Find all should return 200 OK")
    void findAllShouldReturn200Ok() {
        var result = controller.findAll();
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }

    @Test
    @DisplayName("Find all should return accounts from service")
    void findAllShouldReturnAccountsFromService(@Random(type = AccountResponse.class) List<AccountResponse> accounts) {
        when(service.findAll()).thenReturn(accounts);
        var result = controller.findAll();
        assertThat(result.getBody()).asList().hasSameSizeAs(accounts)
                .usingRecursiveFieldByFieldElementComparator().isEqualTo(accounts);
    }

    @Test
    @DisplayName("Find by id should call service")
    void findByIdShouldCallService() {
        controller.findById(1L);
        verify(service).findById(1L);
    }

    @Test
    @DisplayName("Find by id should throw account not found exception")
    void findByIdShouldThrowAccountNotFoundException() {
        when(service.findById(1L)).thenThrow(new AccountNotFoundException());
        assertThatThrownBy(() -> controller.findById(1L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Find by id should return 200 OK")
    void findByIdShouldReturn200Ok(@Random AccountResponse response) {
        when(service.findById(response.getId())).thenReturn(response);
        var result = controller.findById(response.getId());
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }

    @Test
    @DisplayName("Find by id should return account from service")
    void findByIdShouldReturnAccountFromService(@Random AccountResponse response) {
        when(service.findById(response.getId())).thenReturn(response);
        var result = controller.findById(response.getId());
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Create should call service")
    void createShouldCallService(@Random CreateAccountRequest request) {
        controller.create(request);
        verify(service).create(request);
    }

    @Test
    @DisplayName("Create should return 200 OK")
    void createShouldReturn200Ok(@Random CreateAccountRequest request) {
        var result = controller.create(request);
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }

    @Test
    @DisplayName("Create should return account from service")
    void createShouldReturnAccountFromService(@Random CreateAccountRequest request, @Random AccountResponse details) {
        details.setName(request.getName());
        when(service.create(argThat(request::equals)))
                .thenReturn(details);
        var result = controller.create(request);
        assertThat(result.getBody())
                .usingRecursiveComparison().isEqualTo(details);
    }

    @Test
    @DisplayName("Update should call service")
    void updateShouldCallService() {
        var patch = createMergePatch(createObjectBuilder().build());
        controller.update(1L, patch);
        verify(service).update(1L, patch);
    }

    @Test
    @DisplayName("Update should throw account not found exception")
    void updateShouldThrowAccountNotFoundException() {
        when(service.update(anyLong(), any(JsonMergePatch.class)))
                .thenThrow(new AccountNotFoundException());
        assertThatThrownBy(() -> controller.update(1L, createMergePatch(createObjectBuilder().build())))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Update should throw constraint violation exception")
    void updateShouldThrowAccountConstraintViolationException() {
        when(service.update(anyLong(), any(JsonMergePatch.class)))
                .thenThrow(new ConstraintViolationException(emptySet()));
        assertThatThrownBy(() -> controller.update(1L, createMergePatch(createObjectBuilder().add("name", "").build())))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Update should return 200 OK")
    void updateShouldReturn200Ok() {
        var patch = createMergePatch(createObjectBuilder().build());
        var result = controller.update(1L, patch);
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }

    @Test
    @DisplayName("Update should return account from service")
    void updateShouldReturnAccountFromService(@Random AccountResponse response) {
        when(service.update(anyLong(), any(JsonMergePatch.class)))
                .thenReturn(response);
        var result = controller.update(1L, createMergePatch(createObjectBuilder().build()));
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Delete should call service")
    void deleteShouldCallService() {
        controller.delete(1L);
        verify(service).delete(1L);
    }

    @Test
    @DisplayName("Delete should return 200 OK")
    void deleteShouldReturn200Ok() {
        var result = controller.delete(1L);
        assertThat(result).hasFieldOrPropertyWithValue("status", OK);
    }
}
