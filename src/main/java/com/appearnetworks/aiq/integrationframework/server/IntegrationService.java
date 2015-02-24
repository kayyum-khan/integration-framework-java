package com.appearnetworks.aiq.integrationframework.server;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;

/**
 * Service for invoking various operations in the server.
 * <p>
 * The framework will provide exactly one thread-safe implementation of it in the Spring application context.
 * The easiest way to obtain a reference to it is:
 * <pre>
 * {@literal @Autowired}
 * private IntegrationService integrationService;
 * </pre>
 *
 * The methods in this class will throw {@link ServerUnavailableException}
 * if the server responds with HTTP status code 503 (Service Unavailable), or if connection to server fails.
 * In this case, you should automatically retry the operation later.
 *
 * The methods in this class will throw {@link com.appearnetworks.aiq.integrationframework.server.UnauthorizedException}
 * if authorization to the server fails. In this case, you should <em>not</em> automatically retry the same operation,
 * since it will likely fail again.
 *
 * The methods in this class will throw {@link ServerException}
 * if the server responds with an unexpected HTTP status code (other than 503). In this case, you should <em>not</em>
 * automatically retry the same operation, since it will likely fail again.
 */
public interface IntegrationService {
    /**
     * Fetch all currently active client sessions.
     *
     * @return List of all currently active client sessions, never {@code null}
     */
    List<ClientSession> fetchClientSessions();

    /**
     * Fetch a specific client session by id.
     *
     * @param id  client session id, from {@link com.appearnetworks.aiq.integrationframework.server.ClientSession#get_id()}
     *
     * @return the client session if still active, or {@code null} if the session was not found or no longer active
     */
    ClientSession fetchClientSession(String id);

    /**
     * Terminate a specific client session by id.
     *
     * @param id  client session id, from {@link com.appearnetworks.aiq.integrationframework.server.ClientSession#get_id()}
     *
     * @return {@code true} if client session existed and was terminated, {@code false} if the session was not found or no longer active
     */
    boolean terminateClientSession(String id);

    /**
     * Update backend context for a client session.
     *
     * @param userId user id, from {@link User#get_id()}
     * @param deviceId device id, from {@link com.appearnetworks.aiq.integrationframework.server.ClientSession#getDeviceId()}
     * @param provider context provider name
     * @param data context data
     *
     * @return {@code true} if session existed and was updated, {@code false} if session was not found
     */
    boolean updateBackendContext(String userId, String deviceId, String provider, ObjectNode data);

    /**
     * Remove backend context for a client session.
     *
     * @param userId user id, from {@link User#get_id()}
     * @param deviceId device id, from {@link com.appearnetworks.aiq.integrationframework.server.ClientSession#getDeviceId()}
     * @param provider context provider name
     *
     * @return {@code true} if session existed and was updated, {@code false} if session was not found
     */
    boolean removeBackendContext(String userId, String deviceId, String provider);

    /**
     * Create a new backend message.
     *
     * @param message  message to create
     *
     * @return Id of the newly created backend message, never {@code null}
     */
    String createBackendMessage(BackendMessage message);

    /**
     * Create a new backend message with attachments.
     *
     * @param message      message to create
     * @param attachments  attachments
     *
     * @return Id of the newly created backend message, never {@code null}
     */
    String createBackendMessage(BackendMessage message, Collection<MessageAttachment> attachments);

    /**
     * Fetch a specific backend message by id along with read reports and payload.
     * The returned message will not contain recipients data.
     *
     * @param id  backend message id, from {@link com.appearnetworks.aiq.integrationframework.server.EnrichedBackendMessage#get_id()}
     *
     * @return the backend message, or {@code null} if not found
     */
    EnrichedBackendMessage fetchBackendMessage(String id);

    /**
     * update an existing backend message.
     * Time to live, payload and notification can only be updated for a backend message
     *
     * @param id backend message id, from {@link com.appearnetworks.aiq.integrationframework.server.EnrichedBackendMessage#get_id()}
     * @param messageUpdate updates on the message {@link com.appearnetworks.aiq.integrationframework.server.BackendMessageUpdate}
     *
     * @return {@code true} if the message existed and was updated, {@code false} if not found or was not updated
     */
    boolean updateBackendMessage(String id, BackendMessageUpdate messageUpdate);

    /**
     * Deletes a backend message.
     *
     * @param id  backend message id, from {@link com.appearnetworks.aiq.integrationframework.server.EnrichedBackendMessage#get_id()}
     *
     * @return {@code true} if the message existed and were deleted, {@code false} if not found
     */
    boolean deleteBackendMessage(String id);

    /**
     * Fetch list of all backend messages available on the server, including payload (but not including read reports nor recipients data).
     *
     * @return List of, never {@code null}
     */
    List<EnrichedBackendMessage> fetchBackendMessages();

    /**
     * Fetch list of all backend messages available on the server (not including read reports nor recipients data).
     *
     * @param withPayload  whether to include the payload
     *
     * @return List of messages, never {@code null}
     */
    List<EnrichedBackendMessage> fetchBackendMessages(boolean withPayload);

    /**
     * Fetch list of backend messages of a specified type available on the server (not including read reports nor recipients data).
     *
     * @param messageType  the type of message to fetch
     * @param withPayload  whether to include the payload
     *
     * @return List of messages, never {@code null}
     */
    List<EnrichedBackendMessage> fetchBackendMessages(String messageType, boolean withPayload);

    /**
     * Notify server that there is new data available for some users, but do not send push notifications to devices.
     *
     * @param userIds   list of user ids for whom there is new data available
     */
    void newDataAvailableForUsers(List<String> userIds);

    /**
     * Notify server that there is new data available for some users, and send push notifications to affected devices.
     *
     * @param userIds   list of user ids for whom there is new data available
     * @param condition  only notify devices matching this context condition, {@code null} to not filter on context
     */
    void newDataAvailableForUsers(List<String> userIds, ObjectNode condition);

    /**
     * Notify server that there is new data available for some launchables, but do not send push notifications to devices.
     *
     * @param launchableIds  list of launchable ids for which there is new data available
     */
    void newDataAvailableForLaunchables(List<String> launchableIds);

    /**
     * Notify server that there is new data available for some launchables, and send push notifications to affected devices.
     *
     * @param launchableIds  list of launchable ids for which there is new data available
     * @param condition      only notify devices matching this context condition, {@code null} to not filter on context
     */
    void newDataAvailableForLaunchables(List<String> launchableIds, ObjectNode condition);

    /**
     * Notify server that there is new data available for all users, but do not send push notifications to devices.
     */
    void newDataAvailableForAllUsers();

    /**
     * Notify server that there is new data available for all users, and send push notifications to affected devices.
     *
     * @param condition  only notify devices matching this context condition, {@code null} to not filter on context
     */
    void newDataAvailableForAllUsers(ObjectNode condition);

    /**
     * Validates the token for a user.
     *
     * @param token token to validate
     *
     * @return the server user if token is valid, or {@code null} if token is not valid
     */
    User validateUserToken(String token);

    /**
     * Fetch list of users.
     *
     * @return List of users, never {@code null}
     */
    List<User> fetchUsers();

    /**
     * Fetch a specific user by id.
     *
     * @param id  user id, from {@link User#get_id()}
     *
     * @return the user, or {@code null} if the user was not found
     */
    User fetchUser(String id);
}
