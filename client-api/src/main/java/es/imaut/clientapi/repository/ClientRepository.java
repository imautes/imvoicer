package es.imaut.clientapi.repository;

import es.imaut.clientapi.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
