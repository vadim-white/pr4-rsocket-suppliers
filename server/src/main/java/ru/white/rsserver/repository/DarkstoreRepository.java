package ru.white.rsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.white.rsserver.model.Darkstore;

@Repository
public interface DarkstoreRepository extends JpaRepository<Darkstore, Long> {
}
