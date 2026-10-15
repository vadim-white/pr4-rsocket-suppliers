package ru.white.rsclient.controller;

import io.rsocket.exceptions.ApplicationErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.white.rsclient.model.Goods;

@RestController
@RequestMapping("/api/goods")
public class RequestResponseController {

    private final RSocketRequester req;

    public RequestResponseController(RSocketRequester req) {
        this.req = req;
    }

    @GetMapping("/{id}")
    public Mono<Goods> getGoods(@PathVariable Long id) {
        return req.route("getGoods")
                .data(id)
                .retrieveMono(Goods.class)
                .onErrorMap(ApplicationErrorException.class,
                        e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }
}
