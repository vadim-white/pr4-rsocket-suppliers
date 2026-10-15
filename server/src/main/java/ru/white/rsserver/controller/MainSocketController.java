package ru.white.rsserver.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.white.rsserver.model.*;
import ru.white.rsserver.repository.DarkstoreRepository;
import ru.white.rsserver.repository.DeliveryRepository;
import ru.white.rsserver.repository.GoodsRepository;

import java.time.Duration;

@Controller
public class MainSocketController {

    private final GoodsRepository goodsRepo;
    private final DarkstoreRepository darkstoreRepo;
    private final DeliveryRepository deliveryRepo;

    public MainSocketController(GoodsRepository goodsRepo, DarkstoreRepository darkstoreRepo,
                                 DeliveryRepository deliveryRepo) {
        this.goodsRepo = goodsRepo;
        this.darkstoreRepo = darkstoreRepo;
        this.deliveryRepo = deliveryRepo;
    }

    @MessageMapping("getGoods")
    public Mono<Goods> getGoods(Long id) {
        return Mono.justOrEmpty(goodsRepo.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("goods", id)));
    }

    @MessageMapping("getDeliveries")
    public Flux<DeliveryView> getDeliveries() {
        return Flux.fromIterable(deliveryRepo.findAll())
                .delayElements(Duration.ofMillis(700))
                .map(d -> {
                    Goods g = goodsRepo.findById(d.getGoodsId()).orElse(null);
                    Darkstore s = darkstoreRepo.findById(d.getDarkstoreId()).orElse(null);
                    return new DeliveryView(d.getId(), d.getOrderDate(),
                            s != null ? s.getAddress() : "?",
                            g != null ? g.getName() : "?",
                            g != null ? g.getPrice() : 0.0,
                            d.getQty());
                });
    }

    @MessageMapping("deleteDarkstore")
    public Mono<Void> deleteDarkstore(Long id) {
        if (darkstoreRepo.existsById(id)) {
            darkstoreRepo.deleteById(id);
        } else {
            System.out.println("fire-and-forget: darkstore " + id + " did not exist, nothing to delete");
        }
        return Mono.empty();
    }

    @MessageMapping("goodsChannel")
    public Flux<Goods> goodsChannel(Flux<Goods> in) {
        return in.doOnNext(g -> System.out.println("channel: received " + g))
                .map(goodsRepo::save)
                .doOnNext(g -> System.out.println("channel: saved " + g));
    }
}
