package ru.white.rsclient.controller;

import org.reactivestreams.Publisher;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/darkstores")
public class FireForgetController {

    private final RSocketRequester req;

    public FireForgetController(RSocketRequester req) {
        this.req = req;
    }

    @DeleteMapping("/{id}")
    public Publisher<Void> deleteDarkstore(@PathVariable Long id) {
        return req.route("deleteDarkstore").data(id).send();
    }
}
