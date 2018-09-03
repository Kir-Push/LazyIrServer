package com.push.lazyir.modules.touch;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TouchControlDto extends Dto {
    private String command;
    private String id;
    private int moveY;
    private int moveX;

    public TouchControlDto(String command, int moveY, int moveX) {
        this.command = command;
        this.moveY = moveY;
        this.moveX = moveX;
    }

    public TouchControlDto(String command) {
        this.command = command;
    }
}
