package ru.white.rsclient.dto;

import lombok.Data;
import ru.white.rsclient.model.Goods;

import java.util.List;

@Data
public class GoodsListWrapper {
    private List<Goods> goods;
}
