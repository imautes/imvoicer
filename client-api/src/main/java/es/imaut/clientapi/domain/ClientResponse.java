package es.imaut.clientapi.domain;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    private Long id;
    private String name;
    private String vatNumber;
    private String streetAddress;
    private String postcode;
    private String city;
    private String country;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientResponse that = (ClientResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(vatNumber, that.vatNumber) && Objects.equals(streetAddress, that.streetAddress) && Objects.equals(postcode, that.postcode) && Objects.equals(city, that.city) && Objects.equals(country, that.country);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id, name, vatNumber, streetAddress, postcode, city, country);
    }
}
