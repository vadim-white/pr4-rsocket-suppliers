package ru.white.rsserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.white.rsserver.model.Darkstore;
import ru.white.rsserver.model.Delivery;
import ru.white.rsserver.model.Goods;
import ru.white.rsserver.repository.DarkstoreRepository;
import ru.white.rsserver.repository.DeliveryRepository;
import ru.white.rsserver.repository.GoodsRepository;

@SpringBootApplication
public class RsServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RsServerApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(DarkstoreRepository darkstoreRepo, GoodsRepository goodsRepo,
                            DeliveryRepository deliveryRepo) {
        return args -> {
            if (goodsRepo.count() > 0) {
                return;
            }
            Darkstore d1 = darkstoreRepo.save(new Darkstore(null, "Москва, ул. Тверская, 10"));
            Darkstore d2 = darkstoreRepo.save(new Darkstore(null, "Москва, ул. Арбат, 25"));

            Goods g1 = goodsRepo.save(new Goods(null, "Ноутбук", 85000.0));
            Goods g2 = goodsRepo.save(new Goods(null, "Монитор", 42000.0));
            Goods g3 = goodsRepo.save(new Goods(null, "Клавиатура", 6700.0));

            deliveryRepo.save(new Delivery(null, "2026-10-01", d1.getId(), g1.getId(), 2));
            deliveryRepo.save(new Delivery(null, "2026-10-02", d1.getId(), g2.getId(), 1));
            deliveryRepo.save(new Delivery(null, "2026-10-03", d2.getId(), g3.getId(), 5));
        };
    }
}
