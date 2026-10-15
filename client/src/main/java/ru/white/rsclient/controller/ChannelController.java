package ru.white.rsclient.controller;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.white.rsclient.dto.GoodsListWrapper;
import ru.white.rsclient.model.Goods;

@RestController
@RequestMapping("/api/goods")
public class ChannelController {

    private final RSocketRequester req;

    public ChannelController(RSocketRequester req) {
        this.req = req;
    }

    @PostMapping("/channel")
    public Flux<Goods> addMultiple(@RequestBody GoodsListWrapper wrapper) {
        Flux<Goods> out = Flux.fromIterable(wrapper.getGoods());
        return req.route("goodsChannel")
                .data(out)
                .retrieveFlux(Goods.class);
    }
}
