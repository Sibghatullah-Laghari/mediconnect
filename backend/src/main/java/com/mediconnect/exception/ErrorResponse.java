// ErrorResponse is just a data carrier
// Record is perfect here — simple, immutable

package com.mediconnect.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,           // HTTP status code: 404, 400, 500
        String message,       // what went wrong
        String path,          // which URL caused it
        LocalDateTime timestamp  // when it happened
) {}