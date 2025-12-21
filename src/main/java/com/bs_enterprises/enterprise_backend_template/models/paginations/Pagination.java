package com.bs_enterprises.enterprise_backend_template.models.paginations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination<T> {
    private List<T> content;
    private int page;           // zero-based page index
    private int size;           // page size
    private long totalElements; // total items
    private int totalPages;     // total pages
    private boolean last;
    private boolean first;

}
