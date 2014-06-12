package com.appearnetworks.aiq.integrationframework.impl.server;

import com.appearnetworks.aiq.integrationframework.impl.ProtocolConstants;
import com.appearnetworks.aiq.integrationframework.server.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IntegrationServiceImpl implements IntegrationService {
    private static final String ROOT_LINK_CACHE_KEY = "link_";
    private static final String ACCESS_TOKEN_CACHE_KEY = "token";
    private static final String INTEGRATION_LINK_CACHE_KEY = "link_integration_";

    private static final String CLIENTSESSIONS = "clientsessions";
    private static final String NEWDATAAVAILABLE = "newdataavailable";
    private static final String BACKENDCONTEXT = "backendcontext";
    private static final String BACKENDMESSAGES = "backendmessages";
    private static final String VALIDATETOKEN = "validatetoken";
    private static final String DISTRIBUTIONLISTS = "distributionlists";
    private static final String ADAPTER = "adapter";

    private static final String USERS = "users";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    private ObjectMapper mapper = new ObjectMapper();

    @Value("${aiq.url}")
    private String aiqUrl;

    @Value("${aiq.orgname}")
    private String aiqOrgName;

    @Value("${aiq.username}")
    private String aiqUsername;

    @Value("${aiq.password}")
    private String aiqPassword;

    @Value("${aiq.scope:integration}")
    private String aiqScope;

    public void fetchOrgRootMenu() {
        try {
            OrgRootMenu orgRootMenu = new RestTemplate().getForObject(aiqUrl + "?orgName=" + aiqOrgName, OrgRootMenu.class);

            URI baseURL = URI.create(aiqUrl);

            for (Iterator<Map.Entry<String, JsonNode>> iterator = orgRootMenu.getLinks().fields(); iterator.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                cache.put(ROOT_LINK_CACHE_KEY + entry.getKey(), baseURL.resolve(entry.getValue().textValue()));
            }
        } catch (HttpStatusCodeException e) {
            throw reportHttpError(e);
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public String fetchUserToken() {
        if (!cache.containsKey(ACCESS_TOKEN_CACHE_KEY)) {
            fetchAccessToken();
        }
        return (String) cache.get(ACCESS_TOKEN_CACHE_KEY);
    }

    public URI fetchIntegrationLink(String link) {
        if (!cache.containsKey(INTEGRATION_LINK_CACHE_KEY + link)) {
            fetchAccessToken();
        }
        return (URI) cache.get(INTEGRATION_LINK_CACHE_KEY + link);
    }

    public URI fetchRootLink(String link) {
        if (!cache.containsKey(ROOT_LINK_CACHE_KEY + link)) {
            fetchOrgRootMenu();
        }
        return (URI) cache.get(ROOT_LINK_CACHE_KEY + link);
    }

    private void fetchAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "username=" + aiqUsername + "&password=" + aiqPassword + "&grant_type=password&scope=" + aiqScope;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            URI baseURL = fetchRootLink("token");
            AccessToken accessToken = new RestTemplate().postForObject(baseURL, request, AccessToken.class);

            cache.put(ACCESS_TOKEN_CACHE_KEY, accessToken.getAccess_token());

            for (Iterator<Map.Entry<String, JsonNode>> iterator = accessToken.getLinks().fields(); iterator.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                cache.put(INTEGRATION_LINK_CACHE_KEY + entry.getKey(), baseURL.resolve(entry.getValue().textValue()));
            }
        } catch (HttpStatusCodeException e) {
            throw reportHttpError(e);
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    private void invalidateUserToken() {
        cache.remove(ACCESS_TOKEN_CACHE_KEY);
    }

    public String authorizationValue(String token) {
        return "Bearer " + token;
    }

    @Override
    public List<ClientSession> fetchClientSessions() {
        ClientSession[] clientSessions;

        try {
            clientSessions = getForObject(fetchIntegrationLink(CLIENTSESSIONS), ClientSession[].class);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                clientSessions = getForObject(fetchIntegrationLink(CLIENTSESSIONS), ClientSession[].class);
            else
                throw e;
        }

        return Arrays.asList(clientSessions);
    }

    @Override
    public ClientSession fetchClientSession(String id) {
        try {
            return getForObjectOrNull(fetchIntegrationLink(CLIENTSESSIONS) + "/" + id, ClientSession.class);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return getForObjectOrNull(fetchIntegrationLink(CLIENTSESSIONS) + "/" + id, ClientSession.class);
            else
                throw e;
        }
    }

    @Override
    public boolean terminateClientSession(String id) {
        try {
            return delete(fetchIntegrationLink(CLIENTSESSIONS) + "/" + id);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return delete(fetchIntegrationLink(CLIENTSESSIONS) + "/" + id);
            else
                throw e;
        }
    }

    @Override
    public void newDataAvailableForUsers(List<String> userIds) {
        ObjectNode request = mapper.createObjectNode();
        ArrayNode users = mapper.createArrayNode();
        for (String userId : userIds) {
            users.add(userId);
        }
        request.put("users", users);
        request.put("urgent", false);

        makeNewDataAvailableRequest(request);
    }

    @Override
    public void newDataAvailableForUsers(List<String> userIds, ObjectNode condition) {
        ObjectNode request = mapper.createObjectNode();
        ArrayNode users = mapper.createArrayNode();
        for (String userId : userIds) {
            users.add(userId);
        }
        request.put("users", users);
        request.put("urgent", true);
        if (condition != null) {
            request.put("condition", condition);
        }

        makeNewDataAvailableRequest(request);
    }

    @Override
    public void newDataAvailableForLaunchables(List<String> launchableIds) {
        ObjectNode request = mapper.createObjectNode();
        ArrayNode launchables = mapper.createArrayNode();
        for (String launchableId : launchableIds) {
            launchables.add(launchableId);
        }
        request.put("launchables", launchables);
        request.put("urgent", false);

        makeNewDataAvailableRequest(request);
    }

    @Override
    public void newDataAvailableForLaunchables(List<String> launchableIds, ObjectNode condition) {
        ObjectNode request = mapper.createObjectNode();
        ArrayNode launchables = mapper.createArrayNode();
        for (String launchableId : launchableIds) {
            launchables.add(launchableId);
        }
        request.put("launchables", launchables);
        request.put("urgent", true);
        if (condition != null) {
            request.put("condition", condition);
        }

        makeNewDataAvailableRequest(request);
    }

    @Override
    public void newDataAvailableForAllUsers() {
        ObjectNode request = mapper.createObjectNode();
        request.put("all", true);
        request.put("urgent", false);

        makeNewDataAvailableRequest(request);
    }

    @Override
    public void newDataAvailableForAllUsers(ObjectNode condition) {
        ObjectNode request = mapper.createObjectNode();
        request.put("all", true);
        request.put("urgent", true);
        if (condition != null) {
            request.put("condition", condition);
        }

        makeNewDataAvailableRequest(request);
    }

    private void makeNewDataAvailableRequest(ObjectNode request) {
        try {
            postForAccept(fetchIntegrationLink(NEWDATAAVAILABLE), request);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                postForAccept(fetchIntegrationLink(NEWDATAAVAILABLE), request);
            else
                throw e;
        }
    }

    @Override
    public boolean updateBackendContext(String userId, String deviceId, String provider, ObjectNode data) {
        try {
            return doPut(fetchIntegrationLink(BACKENDCONTEXT) +
                    "?userId={userId}&deviceId={deviceId}&provider={provider}", data, userId, deviceId, provider);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return doPut(fetchIntegrationLink(BACKENDCONTEXT) +
                        "?userId={userId}&deviceId={deviceId}&provider={provider}", data, userId, deviceId, provider);
            else
                throw e;
        }
    }

    @Override
    public boolean removeBackendContext(String userId, String deviceId, String provider) {
        try {
            return delete(fetchIntegrationLink(BACKENDCONTEXT) +
                    "?userId={userId}&deviceId={deviceId}&provider={provider}", userId, deviceId, provider);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return delete(fetchIntegrationLink(BACKENDCONTEXT) +
                        "?userId={userId}&deviceId={deviceId}&provider={provider}", userId, deviceId, provider);
            else
                throw e;
        }
    }

    @Override
    public List<EnrichedBackendMessage> fetchBackendMessages() {
        EnrichedBackendMessage[] backendMessages;

        try {
            backendMessages = getForObject(fetchIntegrationLink(BACKENDMESSAGES), EnrichedBackendMessage[].class);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                backendMessages = getForObject(fetchIntegrationLink(BACKENDMESSAGES), EnrichedBackendMessage[].class);
            else
                throw e;
        }

        return Arrays.asList(backendMessages);
    }

    @Override
    public List<DistributionList> fetchDistributionLists() {
        try {
            return Arrays.asList(getForObject(fetchIntegrationLink(DISTRIBUTIONLISTS), DistributionList[].class));
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return Arrays.asList(getForObject(fetchIntegrationLink(DISTRIBUTIONLISTS), DistributionList[].class));
            else
                throw e;
        }
    }

    @Override
    public DistributionList fetchDistributionList(String id) {
        try {
            return getForObjectOrNull(fetchIntegrationLink(DISTRIBUTIONLISTS) + "/" + id, DistributionList.class);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return getForObjectOrNull(fetchIntegrationLink(DISTRIBUTIONLISTS) + "/" + id, DistributionList.class);
            else
                throw e;
        }
    }

    @Override
    public String createDistributionList(Collection<String> users) {
        return _createDistributionList(null, users);
    }

    @Override
    public void createDistributionList(String id, Collection<String> users) {
        _createDistributionList(id, users);
    }

    private String _createDistributionList(String id, Collection<String> users) {
        ObjectNode request = mapper.valueToTree(new DistributionList(users, id));
        try {
            return extractEntityId(postForEntity(fetchIntegrationLink(DISTRIBUTIONLISTS).toString(), request, ObjectNode.class).getBody());
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return extractEntityId(postForEntity(fetchIntegrationLink(DISTRIBUTIONLISTS).toString(), request, ObjectNode.class).getBody());
            else
                throw e;
        }
    }

    @Override
    public boolean updateDistributionList(String id, Collection<String> users) {
        ObjectNode request = mapper.valueToTree(new DistributionList(users, id));
        try {
            return doPut(fetchIntegrationLink(DISTRIBUTIONLISTS) + "/" + id, request);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return doPut(fetchIntegrationLink(DISTRIBUTIONLISTS) + "/" + id, request);
            else
                throw e;
        }
    }

    @Override
    public boolean deleteDistributionList(String id) {
        try {
            return delete(fetchIntegrationLink(DISTRIBUTIONLISTS) + "/" + id);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return delete(fetchIntegrationLink(DISTRIBUTIONLISTS) + "/" + id);
            else
                throw e;
        }
    }

    @Override
    public EnrichedBackendMessage fetchBackendMessage(String id) {
        EnrichedBackendMessage enrichedBackendMessage;

        try {
            enrichedBackendMessage = getForObjectOrNull(fetchIntegrationLink(BACKENDMESSAGES) + "/" + id, EnrichedBackendMessage.class);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                enrichedBackendMessage = getForObjectOrNull(fetchIntegrationLink(BACKENDMESSAGES) + "/" + id, EnrichedBackendMessage.class);
            else
                throw e;
        }

        return enrichedBackendMessage;

    }

    @Override
    public String createBackendMessage(BackendMessage message) {
        ObjectNode request = mapper.valueToTree(message);
        try {
            return extractEntityId(postForEntity(fetchIntegrationLink(BACKENDMESSAGES).toString(), request, ObjectNode.class).getBody());
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return extractEntityId(postForEntity(fetchIntegrationLink(BACKENDMESSAGES).toString(), request, ObjectNode.class).getBody());
            else
                throw e;
        }
    }

    @Override
    public String createBackendMessage(BackendMessage message, Collection<MessageAttachment> attachments) {
        RestTemplate restTemplate = getRestTemplateWithAuth();
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        formHttpMessageConverter.addPartConverter(new AttachmentHttpMessageConverter());
        formHttpMessageConverter.addPartConverter(new MappingJackson2HttpMessageConverter());
        restTemplate.setMessageConverters(Arrays.asList(
                formHttpMessageConverter,
                new MappingJackson2HttpMessageConverter()
        ));

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add(ProtocolConstants.MESSAGE, message);
        for (MessageAttachment attachment : attachments) {
            parts.add(attachment.name, attachment);
        }

        try {
            return extractEntityId(
                    restTemplate.postForObject(fetchIntegrationLink(BACKENDMESSAGES).toString(), parts, ObjectNode.class));
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                    invalidateUserToken();
                    if (fetchUserToken() != null)
                        try {
                            return extractEntityId(
                                    restTemplate.postForObject(fetchIntegrationLink(BACKENDMESSAGES).toString(), parts, ObjectNode.class));
                        } catch (HttpStatusCodeException e2) {
                            switch (e2.getStatusCode()) {
                                case UNAUTHORIZED:
                                    throw new UnauthorizedException();

                                default:
                                    throw reportHttpError(e2);
                            }
                        } catch (ResourceAccessException e2) {
                            throw new ServerUnavailableException(e2.getMessage());
                        } catch (HttpMessageConversionException | RestClientException e2) {
                            throw new ServerException(e2.getMessage());
                        }
                    else
                        throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    @Override
    public boolean deleteBackendMessage(String id) {
        try {
            return delete(fetchIntegrationLink(BACKENDMESSAGES) + "/" + id);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return delete(fetchIntegrationLink(BACKENDMESSAGES) + "/" + id);
            else
                throw e;
        }
    }

    @Override
    public boolean updateBackendMessage(String id, BackendMessageUpdate messageUpdate) {
        ObjectNode request = mapper.valueToTree(messageUpdate);

        try {
            return postForEntity(fetchIntegrationLink(BACKENDMESSAGES) + "/" + id, request, Void.class) != null;
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return postForEntity(fetchIntegrationLink(BACKENDMESSAGES) + "/" + id, request, Void.class) != null;
            else
                throw e;
        }
    }

    @Override
    public User validateUserToken(String token) {
        ObjectNode request = mapper.valueToTree(new UserToken(token));
        AuthorizedUser authorizedUser;

        try {
            authorizedUser = postForObjectOrNull(fetchIntegrationLink(VALIDATETOKEN), request, AuthorizedUser.class);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                authorizedUser = postForObjectOrNull(fetchIntegrationLink(VALIDATETOKEN), request, AuthorizedUser.class);
            else
                throw e;
        }

        if (authorizedUser != null)
            return authorizedUser.getUser();
        else
            return null;
    }

    @Override
    public List<User> fetchUsers() {
        try {
            return Arrays.asList(getForObject(fetchIntegrationLink(USERS), User[].class));
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return Arrays.asList(getForObject(fetchIntegrationLink(USERS), User[].class));
            else
                throw e;
        }
    }

    @Override
    public User fetchUser(String id) {
        try {
            return getForObjectOrNull(fetchIntegrationLink(USERS) + "/" + id, User.class);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                return getForObjectOrNull(fetchIntegrationLink(USERS) + "/" + id, User.class);
            else
                throw e;
        }
    }

    public void register(String integrationURL, String integrationPassword) {
        ObjectNode request = mapper.valueToTree(new RegisterAdapterRequest(integrationURL, integrationPassword));
        try {
            doPut(fetchIntegrationLink(ADAPTER).toString(), request);
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                doPut(fetchIntegrationLink(ADAPTER).toString(), request);
            else
                throw e;
        }
    }

    public void unregister() {
        try {
            delete(fetchIntegrationLink(ADAPTER).toString());
        } catch (UnauthorizedException e) {
            if (fetchUserToken() != null)
                delete(fetchIntegrationLink(ADAPTER).toString());
            else
                throw e;
        }
    }

    public String extractEntityId(ObjectNode doc) {
        return doc.get("_id").textValue();
    }

    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> type) {
        try {
            return getRestTemplateWithAuth().postForEntity(url, request, type);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case NOT_FOUND:
                    return null;

                case UNAUTHORIZED:
                    invalidateUserToken();
                    throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public boolean doPut(String uri, Object data, Object... parameters) {
        try {
            getRestTemplateWithAuth().put(uri, data, parameters);
            return true;
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case NOT_FOUND:
                    return false;

                case UNAUTHORIZED:
                    invalidateUserToken();
                    throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public <T> T getForObject(URI url, Class<T> type) {
        try {
            return getRestTemplateWithAuth().getForObject(url, type);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                    invalidateUserToken();
                    throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public <T> T getForObjectOrNull(String url, Class<T> type) {
        try {
            return getRestTemplateWithAuth().getForObject(url, type);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case NOT_FOUND:
                    return null;

                case UNAUTHORIZED:
                    invalidateUserToken();
                    throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public <T> T postForObjectOrNull(URI url, JsonNode requestEntity, Class<T> type) {
        try {
            return getRestTemplateWithAuth().postForObject(url, requestEntity, type);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case BAD_REQUEST:
                    return null;

                case UNAUTHORIZED:
                    invalidateUserToken();
                    throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public void postForAccept(URI url, JsonNode requestEntity) {
        try {
            getRestTemplateWithAuth().postForEntity(url, requestEntity, Void.class);
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case ACCEPTED:
                    return;

                case UNAUTHORIZED:
                    invalidateUserToken();
                    throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public boolean delete(String url, Object... parameters) {
        try {
            getRestTemplateWithAuth().delete(url, parameters);
            return true;
        } catch (HttpStatusCodeException e) {
            switch (e.getStatusCode()) {
                case NOT_FOUND:
                    return false;

                case UNAUTHORIZED:
                    invalidateUserToken();
                    throw new UnauthorizedException();

                default:
                    throw reportHttpError(e);
            }
        } catch (ResourceAccessException e) {
            throw new ServerUnavailableException(e.getMessage());
        } catch (HttpMessageConversionException | RestClientException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public RestTemplate getRestTemplateWithAuth() {
        ClientHttpRequestInterceptor interceptor = new HeaderHttpRequestInterceptor(fetchUserToken());
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(interceptor);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }

    private RuntimeException reportHttpError(HttpStatusCodeException e) {
        if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE)
            return new ServerUnavailableException();
        else
            return new ServerException(e.getStatusCode(), e.getResponseBodyAsString());
    }

    class HeaderHttpRequestInterceptor implements ClientHttpRequestInterceptor {
        private final String token;

        public HeaderHttpRequestInterceptor(String token) {
            this.token = token;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {

            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
            requestWrapper.getHeaders().add(AUTHORIZATION_HEADER, authorizationValue(token));
            return execution.execute(requestWrapper, body);
        }
    }
}
