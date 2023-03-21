package es.imaut.clientapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateClientRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotBlank
    @Size(max = 31)
    private String vatNumber;
    @NotBlank
    @Size(max = 255)
    private String streetAddress;
    @NotBlank
    @Size(max = 15)
    private String postcode;
    @NotBlank
    @Size(max = 127)
    private String city;
    @NotBlank
    @Size(max = 127)
    private String country;
}
