package com.phoenixtask.controlplane.interfaces.dto;

import java.util.List;

public record PublicApiKeyListResponse(List<PublicApiKeyResponse> keys) {}
