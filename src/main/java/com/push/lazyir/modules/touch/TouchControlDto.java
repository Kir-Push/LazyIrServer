package com.push.lazyir.modules.touch;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TouchControlDto implements Dto {
    private String command;
    private int moveY;
    private int moveX;

    public TouchControlDto(String command) {
        this.command = command;
    }
}
