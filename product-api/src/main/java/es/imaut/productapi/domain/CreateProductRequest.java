package es.imaut.productapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateProductRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
    @Size(max = 255)
    private String description;
    @NotNull
    private BigDecimal netPrice;
    @NotBlank
    @Size(max = 3)
    private String currency;
    @NotBlank
    @Size(max = 15)
    private String unit;
}
