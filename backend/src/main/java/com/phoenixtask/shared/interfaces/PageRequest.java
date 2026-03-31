package com.phoenixtask.shared.interfaces;

public record PageRequest(int page, int pageSize, int offset) {

  public static PageRequest of(Integer page, Integer pageSize) {
    return of(page, pageSize, 100);
  }

  public static PageRequest of(Integer page, Integer pageSize, int maxPageSize) {
    int resolvedPage = page == null || page < 1 ? 1 : page;
    int resolvedSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
    int boundedSize = Math.min(resolvedSize, Math.max(1, maxPageSize));
    int offset = Math.max(0, (resolvedPage - 1) * boundedSize);
    return new PageRequest(resolvedPage, boundedSize, offset);
  }
}
