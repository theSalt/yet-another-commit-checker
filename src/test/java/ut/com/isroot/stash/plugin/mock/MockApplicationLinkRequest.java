package ut.com.isroot.stash.plugin.mock;

import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.sal.api.net.RequestFilePart;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.atlassian.sal.api.net.ResponseStatusException;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.atlassian.sal.api.net.auth.Authenticator;

import java.util.List;
import java.util.Map;

/**
 * @author Sean Ford
 * @since 2016-03-19
 */
class MockApplicationLinkRequest implements ApplicationLinkRequest {
    private final String response;
    private final Integer statusCode;

    public MockApplicationLinkRequest(String response) {
        this.response = response;
        this.statusCode = null;
    }

    public MockApplicationLinkRequest(int statusCode) {
        this.response = null;
        this.statusCode = statusCode;
    }

    @Override
    public <R> R execute(ApplicationLinkResponseHandler<R> applicationLinkResponseHandler) throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setConnectionTimeout(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setSoTimeout(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setUrl(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setRequestBody(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setFiles(List<RequestFilePart> list) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setEntity(Object o) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setRequestContentType(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest addRequestParameters(String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest addAuthentication(Authenticator authenticator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest addTrustedTokenAuthentication() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest addTrustedTokenAuthentication(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest addBasicAuthentication(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest addSeraphAuthentication(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest addHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setHeader(String s, String s1) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setFollowRedirects(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(ResponseHandler<Response> responseHandler) throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String execute() throws ResponseException {
        if(response != null) {
            return response;
        } else {
            throw new ResponseStatusException("exception", new MockResponse(statusCode));
        }
    }

    @Override
    public <RET> RET executeAndReturn(ReturningResponseHandler<Response, RET> returningResponseHandler) throws ResponseException {
        throw new UnsupportedOperationException();
    }
}
