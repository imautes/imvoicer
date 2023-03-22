package es.imaut.accountapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateAccountRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
    @Email
    @Size(max = 255)
    private String email;
    @Size(max = 15)
    private String phone;
    @NotBlank
    @Size(max = 31)
    private String type;
}
