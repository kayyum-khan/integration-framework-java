package com.appearnetworks.aiq.integrationframework.platform;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;

/**
 * Service for invoking various operations in the platform.
 * <p>
 * The framework will provide exactly one thread-safe implementation of it in the Spring application context.
 * The easiest way to obtain a reference to it is:
 * <pre>
 * {@literal @Autowired}
 * private PlatformService platformService;
 * </pre>
 *
 * The methods in this class will throw {@link com.appearnetworks.aiq.integrationframework.platform.PlatformUnavailableException}
 * if the platform responds with HTTP status code 503 (Service Unavailable), or if connection to platform fails.
 * In this case, you should automatically retry the operation later.
 *
 * The methods in this class will throw {@link com.appearnetworks.aiq.integrationframework.platform.UnauthorizedException}
 * if authorization to the platform fails. In this case, you should <em>not</em> automatically retry the same operation,
 * since it will likely fail again.
 *
 * The methods in this class will throw {@link com.appearnetworks.aiq.integrationframework.platform.PlatformException}
 * if the platform responds with an unexpected HTTP status code (other than 503). In this case, you should <em>not</em>
 * automatically retry the same operation, since it will likely fail again.
 */
public interface PlatformService {
    /**
     * Fetch all currently active client sessions.
     *
     * @return List of all currently active client sessions, never {@code null}
     */
    List<ClientSession> fetchClientSessions();

    /**
     * Fetch a specific client session by id.
     *
     * @param id  client session id, from {@link com.appearnetworks.aiq.integrationframework.platform.ClientSession#get_id()}
     *
     * @return the client session if still active, or {@code null} if the session was not found or no longer active
     */
    ClientSession fetchClientSession(String id);

    /**
     * Terminate a specific client session by id.
     *
     * @param id  client session id, from {@link com.appearnetworks.aiq.integrationframework.platform.ClientSession#get_id()}
     *
     * @return {@code true} if client session existed and was terminated, {@code false} if the session was not found or no longer active
     */
    boolean terminateClientSession(String id);

    /**
     * Update backend context for a client session.
     *
     * @param userId user id, from {@link com.appearnetworks.aiq.integrationframework.platform.PlatformUser#get_id()}
     * @param deviceId device id, from {@link com.appearnetworks.aiq.integrationframework.platform.ClientSession#getDeviceId()}
     * @param provider context provider name
     * @param data context data
     *
     * @return {@code true} if session existed and was updated, {@code false} if session was not found
     */
    boolean updateBackendContext(String userId, String deviceId, String provider, ObjectNode data);

    /**
     * Remove backend context for a client session.
     *
     * @param userId user id, from {@link com.appearnetworks.aiq.integrationframework.platform.PlatformUser#get_id()}
     * @param deviceId device id, from {@link com.appearnetworks.aiq.integrationframework.platform.ClientSession#getDeviceId()}
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
     * Fetch a specific backend message by id along with this message read reports.
     * The returned message will not contain recipients data.
     *
     * @param id  backend message id, from {@link com.appearnetworks.aiq.integrationframework.platform.EnrichedBackendMessage#get_id()}
     *
     * @return the backend message, or {@code null} if not found
     */
    EnrichedBackendMessage fetchBackendMessage(String id);

    /**
     * update an existing backend message.
     * Time to live, payload and notification can only be updated for a backend message
     *
     * @param id backend message id, from {@link com.appearnetworks.aiq.integrationframework.platform.EnrichedBackendMessage#get_id()}
     * @param messageUpdate updates on the message {@link com.appearnetworks.aiq.integrationframework.platform.BackendMessageUpdate}
     *
     * @return {@code true} if the message existed and was updated, {@code false} if not found or was not updated
     */
    boolean updateBackendMessage(String id, BackendMessageUpdate messageUpdate);

    /**
     * Deletes a backend message.
     *
     * @param id  backend message id, from {@link com.appearnetworks.aiq.integrationframework.platform.EnrichedBackendMessage#get_id()}
     *
     * @return {@code true} if the message existed and were deleted, {@code false} if not found
     */
    boolean deleteBackendMessage(String id);

    /**
     * Fetch list of all backend messages available on the platform however the read reports are not fetched for any of the message.
     * The returned messages will not contain recipients data.
     *
     * @return List of all messages available on the platform, never {@code null}
     */
    List<EnrichedBackendMessage> fetchBackendMessages();

    /**
     * Fetch list of all distribution lists.
     *
     * This will not return the content of the distribution lists, use {@link #fetchDistributionList(String)} to fetch content.
     *
     * @return List of distribution lists, never {@code null}
     */
    List<DistributionList> fetchDistributionLists();

    /**
     * Fetch a specific distribution list by id.
     *
     * @param id  distribution lists id, from {@link com.appearnetworks.aiq.integrationframework.platform.DistributionList#get_id()}
     *
     * @return the distribution lists, or {@code null} if the distribution lists was not found
     */
    DistributionList fetchDistributionList(String id);

    /**
     * Create a new distribution list, and let the platform choose an id for it.
     *
     * @param users  set of user ids
     *
     * @return id of the newly created distribution list, never {@code null}
     */
    String createDistributionList(Collection<String> users);

    /**
     * Create a new distribution list, and choose the id yourself.
     *
     * @param id     desired id of the distribution list
     * @param users  set of user ids
     *
     * @throws com.appearnetworks.aiq.integrationframework.platform.PlatformException if the given id is already used
     */
    void createDistributionList(String id, Collection<String> users);

    /**
     * Update a specific distribution list.
     *
     * <em>Note:</em> This will affect existing backend messages using this distribution list. Added users will receive the message.
     * The message will be removed for users removed from this distribution list (unless they are in an other distribution list
     * of the message, or are direct recipients of the message).
     *
     * @param id  distribution lists id, from {@link com.appearnetworks.aiq.integrationframework.platform.DistributionList#get_id()}
     * @param users  new set of user ids
     *
     * @return {@code true} if the message existed and was updated, {@code false} if not found
     */
    boolean updateDistributionList(String id, Collection<String> users);

    /**
     * Delete a specific distribution list.
     *
     * <em>Note:</em> This will affect existing backend messages using this distribution list.
     * The message will be removed for users in this distribution list (unless they are in an other distribution list
     * of the message, or are direct recipients of the message).
     *
     * @param id  distribution lists id, from {@link com.appearnetworks.aiq.integrationframework.platform.DistributionList#get_id()}
     *
     * @return {@code true} if the distribution list and were deleted, {@code false} if not found
     */
    boolean deleteDistributionList(String id);

    /**
     * Notify platform that there is new data available for some users, but do not send push notifications to devices.
     *
     * @param userIds   list of user ids for whom there is new data available
     */
    void newDataAvailableForUsers(List<String> userIds);

    /**
     * Notify platform that there is new data available for some users, and send push notifications to affected devices.
     *
     * @param userIds   list of user ids for whom there is new data available
     * @param condition  only notify devices matching this context condition, {@code null} to not filter on context
     */
    void newDataAvailableForUsers(List<String> userIds, ObjectNode condition);

    /**
     * Notify platform that there is new data available for some launchables, but do not send push notifications to devices.
     *
     * @param launchableIds  list of launchable ids for which there is new data available
     */
    void newDataAvailableForLaunchables(List<String> launchableIds);

    /**
     * Notify platform that there is new data available for some launchables, and send push notifications to affected devices.
     *
     * @param launchableIds  list of launchable ids for which there is new data available
     * @param condition      only notify devices matching this context condition, {@code null} to not filter on context
     */
    void newDataAvailableForLaunchables(List<String> launchableIds, ObjectNode condition);

    /**
     * Notify platform that there is new data available for all users, but do not send push notifications to devices.
     */
    void newDataAvailableForAllUsers();

    /**
     * Notify platform that there is new data available for all users, and send push notifications to affected devices.
     *
     * @param condition  only notify devices matching this context condition, {@code null} to not filter on context
     */
    void newDataAvailableForAllUsers(ObjectNode condition);

    /**
     * Validates the token for a user.
     *
     * @param token token to validate
     *
     * @return the platform user if token is valid, or {@code null} if token is not valid
     */
    PlatformUser validatePlatformUserToken(String token);

    /**
     * Fetch list of users.
     *
     * @return List of users, never {@code null}
     */
    List<PlatformUser> fetchUsers();

    /**
     * Fetch a specific user by id.
     *
     * @param id  user id, from {@link com.appearnetworks.aiq.integrationframework.platform.PlatformUser#get_id()}
     *
     * @return the user, or {@code null} if the user was not found
     */
    PlatformUser fetchUser(String id);
}
