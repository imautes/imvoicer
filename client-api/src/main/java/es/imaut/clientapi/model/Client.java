package es.imaut.clientapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "client")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotBlank
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @NotBlank
    @Size(max = 31)
    @Column(name = "vat_number")
    private String vatNumber;
    @NotBlank
    @Size(max = 255)
    @Column(name = "street_address")
    private String streetAddress;
    @NotBlank
    @Size(max = 15)
    @Column(name = "postcode")
    private String postcode;
    @NotBlank
    @Size(max = 127)
    @Column(name = "city")
    private String city;
    @NotBlank
    @Size(max = 127)
    @Column(name = "country")
    private String country;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Client client = (Client) o;
        return id != null && Objects.equals(id, client.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return getClass().hashCode();
    }
}
