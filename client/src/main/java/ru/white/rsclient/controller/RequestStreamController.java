package ru.white.rsclient.controller;

import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.white.rsclient.model.DeliveryView;

@RestController
@RequestMapping("/api/deliveries")
public class RequestStreamController {

    private final RSocketRequester req;

    public RequestStreamController(RSocketRequester req) {
        this.req = req;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DeliveryView> getDeliveries() {
        return req.route("getDeliveries")
                .retrieveFlux(DeliveryView.class)
                .doOnSubscribe(s -> System.out.println("stream started"))
                .filter(v -> v.getQty() > 0)
                .map(v -> { v.setGoodsName(v.getGoodsName().toUpperCase()); return v; })
                .doOnNext(v -> System.out.println("received " + v))
                .doOnComplete(() -> System.out.println("stream finished"));
    }
}
