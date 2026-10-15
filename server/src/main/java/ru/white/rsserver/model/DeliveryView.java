package ru.white.rsserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeliveryView {
    private Long id;
    private String orderDate;
    private String darkstoreAddress;
    private String goodsName;
    private Double goodsPrice;
    private Integer qty;
}
