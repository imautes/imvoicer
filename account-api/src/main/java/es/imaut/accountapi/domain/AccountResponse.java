package es.imaut.accountapi.domain;

import lombok.*;

import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String type;
    private Long accountDetailsClientId;
    private Set<BankDetailsResponse> bankDetails;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountResponse that = (AccountResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(email, that.email) && Objects.equals(phone, that.phone) && Objects.equals(type, that.type) && Objects.equals(accountDetailsClientId, that.accountDetailsClientId) && Objects.equals(bankDetails, that.bankDetails);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id, name, email, phone, type, accountDetailsClientId, bankDetails);
    }
}
