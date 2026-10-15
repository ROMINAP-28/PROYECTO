package com.travelink.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServletHttpExchangeAdapter extends HttpExchange {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Headers requestHeaders;
    private final Headers responseHeaders;
    private final Map<String, Object> attributes = new HashMap<>();
    private int responseCode = -1;

    public ServletHttpExchangeAdapter(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.requestHeaders = new Headers();
        this.responseHeaders = new Headers();

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                Enumeration<String> values = request.getHeaders(header);
                while (values.hasMoreElements()) {
                    requestHeaders.add(header, values.nextElement());
                }
            }
        }
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public URI getRequestURI() {
        String qs = request.getQueryString();
        String uriStr = request.getRequestURI() + (qs != null && !qs.isEmpty() ? "?" + qs : "");
        return URI.create(uriStr);
    }

    @Override
    public String getRequestMethod() {
        return request.getMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return null;
    }

    @Override
    public void close() {
        try {
            response.getOutputStream().flush();
        } catch (IOException ignored) {}
    }

    @Override
    public InputStream getRequestBody() {
        try {
            return request.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OutputStream getResponseBody() {
        try {
            return response.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        this.responseCode = rCode;
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                response.addHeader(entry.getKey(), val);
            }
        }
        response.setStatus(rCode);
        if (responseLength > 0) {
            response.setContentLengthLong(responseLength);
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(request.getRemoteAddr(), request.getRemotePort());
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(request.getLocalAddr(), request.getLocalPort());
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {}

    @Override
    public HttpPrincipal getPrincipal() {
        return null;
    }
}
