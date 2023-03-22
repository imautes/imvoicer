package es.imaut.accountapi.domain;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsResponse {
    private Long id;
    private String accountName;
    private String iban;
    private String bic;
    private String bankName;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankDetailsResponse that = (BankDetailsResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(accountName, that.accountName) && Objects.equals(iban, that.iban) && Objects.equals(bic, that.bic) && Objects.equals(bankName, that.bankName);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(id, accountName, iban, bic, bankName);
    }
}
