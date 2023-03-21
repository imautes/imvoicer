package es.imaut.clientapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
}
