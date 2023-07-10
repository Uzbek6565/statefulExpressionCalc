package com.efimchick.ifmo.web.message;

public class ResponseBody {
    private int code;
    private String reason;
    private String body;

    public ResponseBody(final int code, final String reason, final String body) {
        this.code = code;
        this.reason = reason;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
