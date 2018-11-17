package com.push.lazyir.modules.touch;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyboardDto extends Dto {
    private String command;
    private char keycode;
    private String symbol;

    public KeyboardDto(String command, char keycode) {
        this.command = command;
        this.keycode = keycode;
    }

    public KeyboardDto(String command, String symbol) {
        this.command = command;
        this.symbol = symbol;
    }


}
