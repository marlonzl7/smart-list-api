package com.smartlist.api.shared.dto;

public record ApiResponse<T> (
        boolean sucess,
        String message,
        T data
) {}
