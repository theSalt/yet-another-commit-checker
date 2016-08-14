package ut.com.isroot.stash.plugin.mock;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.AuthorisationURIGenerator;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.sal.api.net.Request;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Sean Ford
 * @since 2016-02-14
 */
public class MockApplicationLink implements ApplicationLink {
    private String name;

    public MockApplicationLink() {
        name = null;
    }

    public MockApplicationLink(String name) {
        this.name = name;
    }

    public static MockApplicationLink requestThrowsCredentialException() {
        AuthorisationURIGenerator uriGenerator = new AuthorisationURIGenerator() {
            @Override
            public URI getAuthorisationURI(URI uri) {
                throw new UnsupportedOperationException();
            }

            @Override
            public URI getAuthorisationURI() {
                try {
                    return new URI("https://server/auth/uri");
                }
                catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return new MockApplicationLink() {
            @Override
            public ApplicationLinkRequestFactory createAuthenticatedRequestFactory() {
                return MockApplicationLinkRequestFactory.mockCredentialsException(new CredentialsRequiredException(uriGenerator, "auth exception"));
            }
        };
    }

    public static MockApplicationLink requestReturnsResponse(Request.MethodType methodType, String path, String response) {
        return new MockApplicationLink() {
            @Override
            public ApplicationLinkRequestFactory createAuthenticatedRequestFactory() {
                return MockApplicationLinkRequestFactory.mockReturnResponse(methodType, path, new MockApplicationLinkRequest(response));
            }
        };
    }

    public static MockApplicationLink requestReturnsResponse(Request.MethodType methodType, String path, int statusCode) {
        return new MockApplicationLink() {
            @Override
            public ApplicationLinkRequestFactory createAuthenticatedRequestFactory() {
                return MockApplicationLinkRequestFactory.mockReturnResponse(methodType, path, new MockApplicationLinkRequest(statusCode));
            }
        };
    }

    public MockApplicationLink setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ApplicationId getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getDisplayUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getRpcUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPrimary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSystem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory(Class<? extends AuthenticationProvider> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createImpersonatingAuthenticatedRequestFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequestFactory createNonImpersonatingAuthenticatedRequestFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProperty(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object putProperty(String s, Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object removeProperty(String s) {
        throw new UnsupportedOperationException();
    }
}
