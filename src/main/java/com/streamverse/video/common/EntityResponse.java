package com.streamverse.video.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * Uniform response envelope for every endpoint.
 */
@Getter
@Setter
public class EntityResponse {

    private String status;
    private int code;
    private Object data;
    private String message;
    private MetaData metaData;

    public EntityResponse(final int code, final String status, final Object data, final String message) {
        this.code = code;
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public EntityResponse(final int code, final String status, final Object data, final String message,
                          final MetaData metaData) {
        this.code = code;
        this.status = status;
        this.data = data;
        this.message = message;
        this.metaData = metaData;
    }

    public static EntityResponse ok(final String message, final Object data) {
        return new EntityResponse(HttpStatus.OK.value(), "true", data, message);
    }

    public static EntityResponse created(final String message, final Object data) {
        return new EntityResponse(HttpStatus.CREATED.value(), "true", data, message);
    }

    public static EntityResponse ok(final String message, final Object data, final MetaData metaData) {
        return new EntityResponse(HttpStatus.OK.value(), "true", data, message, metaData);
    }

    public static EntityResponse error(final int code, final String message, final Object data) {
        return new EntityResponse(code, "false", data, message);
    }
}
