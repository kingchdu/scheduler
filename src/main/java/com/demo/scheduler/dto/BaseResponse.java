package com.demo.scheduler.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class BaseResponse<T> {
    private Integer status;

    private String message;

    private T data;

    private Integer errorCode = 0;

    private Pagination pagination;

    public static BaseResponse success() {
        return new BaseResponse(HttpStatus.OK);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse(HttpStatus.OK, data);
    }

    public BaseResponse() {
        this.status = HttpStatus.OK.value();
        this.data = null;
        this.message = HttpStatus.OK.getReasonPhrase();
        this.pagination = null;
    }

    public BaseResponse(HttpStatus status) {
        this.status = status.value();
        this.data = null;
        this.message = status.getReasonPhrase();
    }

    public BaseResponse(T data) {
        status = HttpStatus.OK.value();
        this.data = data;
        this.message = HttpStatus.OK.getReasonPhrase();

        if (data instanceof Page) {
            convert((Page) data);
        } else {
            this.pagination = null;
        }
    }

    public BaseResponse(HttpStatus status, T data) {
        this(data);
        this.status = status.value();
        this.message = status.getReasonPhrase();
    }

    public BaseResponse(HttpStatus status, String message, Integer errorCode) {
        this.status = status.value();
        this.message = message;
        this.errorCode = errorCode;
    }

    private void convert(Page page) {
        this.data = (T) page.getContent();
        Pagination pg = Pagination.builder()
                                .currentPage(page.getNumber())
                                .currentElements(page.getNumberOfElements())
                                .totalPages(page.getTotalPages())
                                .totalElements((int)page.getTotalElements())
                                .build();

        this.pagination = pg;
    }

}