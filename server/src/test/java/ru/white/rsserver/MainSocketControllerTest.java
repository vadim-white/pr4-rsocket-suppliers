package ru.white.rsserver;

import io.rsocket.exceptions.ApplicationErrorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.white.rsserver.model.DeliveryView;
import ru.white.rsserver.model.Goods;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MainSocketControllerTest {

    @Autowired
    private RSocketRequester.Builder builder;

    private RSocketRequester req;

    @BeforeEach
    void setup() {
        req = builder.tcp("localhost", 5200);
    }

    @AfterEach
    void cleanup() {
        req.dispose();
    }

    @Test
    void getGoods_found() {
        Mono<Goods> result = req.route("getGoods").data(1L).retrieveMono(Goods.class);
        assertNotNull(result.block());
    }

    @Test
    void getGoods_notFound_raisesError() {
        Mono<Goods> result = req.route("getGoods").data(999L).retrieveMono(Goods.class);
        assertThrows(ApplicationErrorException.class, result::block);
    }

    @Test
    void getDeliveries_streamsJoinedView() {
        Flux<DeliveryView> result = req.route("getDeliveries").retrieveFlux(DeliveryView.class);
        assertTrue(result.collectList().block().size() >= 3);
    }

    @Test
    void goodsChannel_savesEach() {
        Goods g1 = new Goods(null, "Тест1", 1000.0);
        Goods g2 = new Goods(null, "Тест2", 2000.0);
        Flux<Goods> result = req.route("goodsChannel")
                .data(Flux.just(g1, g2))
                .retrieveFlux(Goods.class);
        assertEquals(2, result.collectList().block().size());
    }
}