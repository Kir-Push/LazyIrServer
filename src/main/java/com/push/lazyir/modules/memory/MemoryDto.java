package com.push.lazyir.modules.memory;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryDto implements Dto {
    private String command;
    private CRTEntity crtEntity;
    private MemoryEntity memoryEntity;
}
