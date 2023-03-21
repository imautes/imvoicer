package es.imaut.productapi.domain;

import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal netPrice;
    private String currency;
    private String unit;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductResponse that = (ProductResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && (Objects.equals(netPrice, that.netPrice) || (netPrice != null && netPrice.compareTo(that.netPrice) == 0)) && Objects.equals(currency, that.currency) && Objects.equals(unit, that.unit);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id, name, description, netPrice, currency, unit);
    }
}
