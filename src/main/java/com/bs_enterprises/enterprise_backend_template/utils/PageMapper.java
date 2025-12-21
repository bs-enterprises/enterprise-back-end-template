package com.bs_enterprises.enterprise_backend_template.utils;

import com.bs_enterprises.enterprise_backend_template.models.paginations.Pagination;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PageMapper {

    public static <T> Pagination<T> toPagination(Page<T> page) {
        Pagination<T> p = new Pagination<>();
        p.setContent(page.getContent());
        p.setPage(page.getNumber());
        p.setSize(page.getSize());
        p.setTotalElements(page.getTotalElements());
        p.setTotalPages(page.getTotalPages());
        p.setFirst(page.isFirst());
        p.setLast(page.isLast());
        return p;
    }
}
