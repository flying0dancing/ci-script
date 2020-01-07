package com.lombardrisk.ignis.client.core.page.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Page {
    private int number;
    private int size;
    private int totalElements;
    private int totalPages;
}
