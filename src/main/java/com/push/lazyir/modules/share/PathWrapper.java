package com.push.lazyir.modules.share;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PathWrapper {
    private List<String> paths;
}
