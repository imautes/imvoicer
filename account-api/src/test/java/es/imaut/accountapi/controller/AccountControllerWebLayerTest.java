package es.imaut.accountapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.imaut.accountapi.RandomAccountExtension;
import es.imaut.accountapi.domain.AccountResponse;
import es.imaut.accountapi.domain.CreateAccountRequest;
import es.imaut.accountapi.exception.AccountNotFoundException;
import es.imaut.accountapi.service.AccountService;
import io.github.glytching.junit.extension.random.Random;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.json.JsonMergePatch;
import java.util.List;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { AccountController.class })
@ExtendWith({ RandomAccountExtension.class })
class AccountControllerWebLayerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private AccountService service;

    @Test
    @DisplayName("GET /accounts should return 200 OK")
    void getAccountsShouldReturn200Ok() throws Exception {
        mvc.perform(get("/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /accounts should return accounts from service")
    void getAccountsShouldReturnAccountsFromService(@Random(type = AccountResponse.class) List<AccountResponse> accounts) throws Exception {
        when(service.findAll()).thenReturn(accounts);
        var result = mvc.perform(get("/accounts")).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(accounts));
    }

    @Test
    @DisplayName("GET /accounts/{id} should return 404 Not found")
    void getAccountsIdShouldReturn404NotFound() throws Exception {
        when(service.findById(1L)).thenThrow(new AccountNotFoundException());
        mvc.perform(get("/accounts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /accounts/{id} should return 200 OK")
    void getAccountsIdShouldReturn200Ok(@Random AccountResponse response) throws Exception {
        when(service.findById(response.getId())).thenReturn(response);
        mvc.perform(get("/accounts/" + response.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /accounts/{id} should return account from service")
    void getAccountsIdShouldReturnAccountFromService(@Random AccountResponse response) throws Exception {
        when(service.findById(response.getId())).thenReturn(response);
        var result = mvc.perform(get("/accounts/" + response.getId())).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("POST /accounts should return 200 OK")
    void postAccountsShouldReturn200Ok(@Random CreateAccountRequest body) throws Exception {
        var request = post("/accounts")
                .contentType("application/json")
                .content(mapper.writeValueAsString(body));
        mvc.perform(request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /accounts should return 400 Bad Request")
    void postAccountsShouldReturn400BadRequest() throws Exception {
        var request = post("/accounts")
                .contentType("application/json")
                .content(mapper.writeValueAsString(new CreateAccountRequest()));
        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /accounts should ignore unknown fields")
    void postAccountsShouldIgnoreUnknownFields() throws Exception {
        var body = """
                {
                  "name": "John Doe",
                  "type": "Self Employed",
                  "unknown": "field"
                }""";
        var request = post("/accounts")
                .contentType("application/json")
                .content(body);
        mvc.perform(request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /accounts should return account from service")
    void postAccountsShouldReturnAccountFromService(@Random CreateAccountRequest body, @Random AccountResponse response) throws Exception {
        response.setName(body.getName());
        when(service.create(body)).thenReturn(response);
        var request = post("/accounts")
                .contentType("application/json")
                .content(mapper.writeValueAsString(body));
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("PATCH /accounts/{id} should return 404 Not found")
    void patchAccountsIdShouldReturn404NotFound() throws Exception {
        when(service.update(anyLong(), any(JsonMergePatch.class))).thenThrow(new AccountNotFoundException());
        var request = patch("/accounts/1")
                .contentType("application/merge-patch+json")
                .content("{}");
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /accounts/{id} should return 400 Bad Request")
    void patchAccountsIdShouldReturn400BadRequest() throws Exception {
        when(service.update(anyLong(), any(JsonMergePatch.class))).thenThrow(new ConstraintViolationException(emptySet()));
        var request = patch("/accounts/1")
                .contentType("application/merge-patch+json")
                .content("""
                        {
                          "name": ""
                        }
                        """);
        mvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /accounts/{id} should return 200 OK")
    void patchAccountsIdShouldReturn200Ok(@Random AccountResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/accounts/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("{}");
        mvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /accounts/{id} should ignore unknown fields")
    void patchAccountsIdShouldIgnoreUnknownFields(@Random AccountResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/accounts/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("""
                        {
                          "ignored": "field"
                        }
                        """);
        mvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /accounts/{id} should return account from service")
    void patchAccountsIdShouldReturnAccountFromService(@Random AccountResponse response) throws Exception {
        when(service.update(eq(response.getId()), any(JsonMergePatch.class))).thenReturn(response);
        var request = patch("/accounts/" + response.getId())
                .contentType("application/merge-patch+json")
                .content("{}");
        var result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualToIgnoringWhitespace(mapper.writeValueAsString(response));
    }

    @Test
    @DisplayName("DELETE /accounts/{id} should return 200 OK")
    void deleteAccountsIdShouldReturn200Ok() throws Exception {
        mvc.perform(delete("/accounts/1"))
                .andExpect(status().isOk());
    }
}
