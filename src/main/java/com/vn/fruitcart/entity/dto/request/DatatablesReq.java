package com.vn.fruitcart.entity.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatatablesReq {
    private int draw;
    private int start;
    private int length;
    private Search search;
    private List<Order> order;
    private List<Column> columns;

    @Getter
    @Setter
    public class Search {
        private String value;
    }

    @Getter
    @Setter
    public class Order {
        private int column;
        private String dir;
    }

    @Getter
    @Setter
    public class Column {
        private String data;
        private boolean searchable;
    }
}
