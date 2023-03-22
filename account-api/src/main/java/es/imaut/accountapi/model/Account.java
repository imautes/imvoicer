package es.imaut.accountapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "account")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotBlank
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @Email
    @Size(max = 255)
    @Column(name = "email")
    private String email;
    @Size(max = 15)
    @Column(name = "phone")
    private String phone;
    @NotBlank
    @Size(max = 31)
    @Column(name = "type")
    private String type;
    @Column(name = "account_details_client_id")
    private Long accountDetailsClientId;
    @OneToMany(fetch = EAGER, cascade = ALL, mappedBy = "account")
    private Set<BankDetails> bankDetails;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id);
    }
}
