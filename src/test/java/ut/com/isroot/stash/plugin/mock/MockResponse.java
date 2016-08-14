package ut.com.isroot.stash.plugin.mock;

import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Sean Ford
 * @since 2016-03-19
 */
class MockResponse implements Response {
    private final int statusCode;

    public MockResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getResponseBodyAsString() throws ResponseException {
        return "MOCK RESPONSE BODY";
    }

    @Override
    public InputStream getResponseBodyAsStream() throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getEntity(Class<T> aClass) throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStatusText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSuccessful() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }
}
