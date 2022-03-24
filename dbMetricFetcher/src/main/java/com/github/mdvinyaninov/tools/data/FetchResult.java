package com.github.mdvinyaninov.tools.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class FetchResult {
    @Getter
    @Setter
    private long elapsedTime;
    @Getter
    @Setter
    private List<Object[]> data;
}
