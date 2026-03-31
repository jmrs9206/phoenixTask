package com.phoenixtask.shared.interfaces;

import java.util.List;

public record PageResponse<T>(
    List<T> items,
    int page,
    int pageSize,
    long total,
    int totalPages
) {

  public static <T> PageResponse<T> of(List<T> items, PageRequest request, long total) {
    int pages = request.pageSize() <= 0 ? 1 : (int) Math.ceil((double) total / request.pageSize());
    return new PageResponse<>(items, request.page(), request.pageSize(), total, pages);
  }
}
