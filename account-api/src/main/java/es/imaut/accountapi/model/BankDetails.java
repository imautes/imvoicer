package es.imaut.accountapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "bank_details")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetails {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotEmpty
    @Size(max = 255)
    @Column(name = "account_name")
    private String accountName;
    @NotEmpty
    @Size(max = 24)
    @Column(name = "iban")
    private String iban;
    @NotEmpty
    @Size(max = 8)
    @Column(name = "bic")
    private String bic;
    @NotEmpty
    @Size(max = 255)
    @Column(name = "bank_name")
    private String bankName;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ToString.Exclude
    @JsonIgnore
    private Account account;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankDetails that = (BankDetails) o;
        return Objects.equals(id, that.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id);
    }
}
