package ru.white.rsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.white.rsserver.model.Goods;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {
}
