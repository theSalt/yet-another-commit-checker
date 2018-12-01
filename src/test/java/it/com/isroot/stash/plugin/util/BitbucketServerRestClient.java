package it.com.isroot.stash.plugin.util;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sean Ford
 * @since 2017-08-20
 */
public class BitbucketServerRestClient {
    private static final Logger log = LoggerFactory.getLogger(BitbucketServerRestClient.class);

    private static final String BASE_URI = "http://localhost:7990/bitbucket";
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";
    private static final String PROJECT = "PROJECT_1";

    private final HttpClient client;
    private UsernamePasswordCredentials creds;

    public BitbucketServerRestClient() {
        client = createClient();
    }

    private HttpClient createClient() {
        creds = new UsernamePasswordCredentials(USER, PASSWORD);

        HttpClient client = HttpClientBuilder.create()
                .build();

        return client;
    }

    public String createRepo(String name) {
        HttpPost post = new HttpPost(buildUri("/rest/api/1.0/projects/%s/repos", PROJECT));

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        post.setEntity(buildJsonEntity(params));

        HttpResponse response = execute(post);

        return responseToJson(response)
                .getString("slug");
    }

    public void enableHook(String hookKey, @Nullable String repoSlug) {
        log.info("enabling hook, hookKey={} repoSlug={}", hookKey, repoSlug);

        HttpPut enableHook = new HttpPut(hookUri(hookKey, repoSlug) + "/enabled");

        enableHook.setEntity(buildJsonEntity(new HashMap<>()));

        HttpResponse response = execute(enableHook);
        try {
            log.info("response: {}", EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disableHook(String hookKey, @Nullable String repoSlug) {
        HttpDelete disableHook = new HttpDelete(hookUri(hookKey, repoSlug) + "/enabled");

        HttpResponse response = execute(disableHook);
        try {
            log.info("response: {}", EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getHookSettings(String repoSlug, String hookKey) {
        HttpGet hookSettings = new HttpGet(hookUri(hookKey, repoSlug) + "/settings");

        HttpResponse response = execute(hookSettings);
        try {
            log.info("settings: {}",  EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHookSettings(String hookKey, @Nullable String repoSlug,
                                Map<String, String> settings) {
        log.info("setting hook settings, hookKey={} repoSlug={} settings={}", hookKey, repoSlug,
                settings);

        HttpPut update = new HttpPut(hookUri(hookKey, repoSlug) + "/settings");

        update.setEntity(buildJsonEntity(settings));
        HttpResponse response = execute(update);
        try {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void doFormPost(String path, Map<String, String> settings) {
        log.info("form post, path={} settings={}", path, settings);

        HttpPost post = new HttpPost(buildUri(path));

        List<NameValuePair> params = new ArrayList<>();
        for(Map.Entry<String, String> setting : settings.entrySet()) {
            params.add(new BasicNameValuePair(setting.getKey(), setting.getValue()));
        }

        log.info("settings: {}", params);

        try {
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse  = execute(post);
            log.info("status: {}", httpResponse.getStatusLine());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse execute(HttpUriRequest request) {
        try {
            request.addHeader(new BasicScheme().authenticate(creds, request, null));
            request.addHeader("X-Atlassian-Token", "no-check");

            return client.execute(request);
        } catch (IOException | AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildUri(String path, Object... args) {
        return String.format("%s%s", BASE_URI, String.format(path, args));
    }

    private HttpEntity buildJsonEntity(Map<String, String> params) {
        return new StringEntity(new Gson().toJson(params), ContentType.APPLICATION_JSON);
    }

    private JsonObject responseToJson(HttpResponse response) {
        try {
            String json = EntityUtils.toString(response.getEntity());
            log.info("response: {}", json);

            return Json.createReader(new StringReader(json)).readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String hookUri(String hookKey, @Nullable String repoSlug) {
        String uri;

        if(repoSlug != null) {
            uri = String.format("/rest/api/1.0/projects/PROJECT_1/repos/%s/settings/hooks/%s",
                    repoSlug, hookKey);
        } else {
            uri = String.format("/rest/api/1.0/projects/PROJECT_1/settings/hooks/%s",
                    hookKey);
        }

        return buildUri(uri);
    }
}
