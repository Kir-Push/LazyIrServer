package com.push.lazyir.modules.memory;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MemoryEntity {
    private long mainMem;
    private long mainMemFree;
    private List<MemPair> extMem;
}
