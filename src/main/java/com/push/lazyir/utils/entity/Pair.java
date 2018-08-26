package com.push.lazyir.utils.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<K,V> {

    private final K left;
    private final V right;
}
