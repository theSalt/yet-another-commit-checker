package ut.com.isroot.stash.plugin.mock;

import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.Request;
import com.google.common.base.Preconditions;

import java.net.URI;

/**
 * @author Sean Ford
 * @since 2016-03-19
 */
class MockApplicationLinkRequestFactory implements ApplicationLinkRequestFactory {
    private final Request.MethodType methodType;
    private final String path;
    private final ApplicationLinkRequest request;

    private MockApplicationLinkRequestFactory(Request.MethodType methodType, String path, ApplicationLinkRequest request) {
        this.methodType = methodType;
        this.path = path;
        this.request = request;
    }

    public static ApplicationLinkRequestFactory mockReturnResponse(Request.MethodType methodType, String path, ApplicationLinkRequest request) {
        return new MockApplicationLinkRequestFactory(methodType, path, request);
    }

    public static ApplicationLinkRequestFactory mockCredentialsException(CredentialsRequiredException e) {
        return new ApplicationLinkRequestFactory() {

            @Override
            public URI getAuthorisationURI(URI uri) {
                throw new UnsupportedOperationException();
            }

            @Override
            public URI getAuthorisationURI() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ApplicationLinkRequest createRequest(Request.MethodType methodType, String s) throws CredentialsRequiredException {
                throw e;
            }
        };
    }


    @Override
    public ApplicationLinkRequest createRequest(Request.MethodType methodType, String path)
            throws CredentialsRequiredException {
        Preconditions.checkArgument(this.methodType.equals(methodType), "expected %s but was %s", this.methodType, methodType);
        Preconditions.checkArgument(this.path.equals(path), "expected %s but was %s", this.path, path);

        return request;
    }

    @Override
    public URI getAuthorisationURI(URI uri) {
        return null;
    }

    @Override
    public URI getAuthorisationURI() {
        return null;
    }
}
