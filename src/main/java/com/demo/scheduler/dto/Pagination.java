package com.demo.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Pagination {

    private Integer currentPage;

    private Integer currentElements;

    private Integer totalElements;

    private Integer totalPages;

}