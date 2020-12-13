package duberchat.events;

import duberchat.chatutil.Channel;

/**
 * A {@code ChannelDemoteMemberEvent} is an event that is created when a user is
 * demoted from admin to normal user. 
 * <p>
 * The client should send this event when a user is demoted in a channel. The
 * server will update and propagate the event (with an updated {@code Channel}
 * object) to the other clients, allowing for proper updating of channel
 * members.
 * <p>
 * Only admin users should be able to create this event.
 * 
 * <p>
 * Since <b>2020-12-04</b>.
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Paula Yuan
 */
public class ChannelDemoteMemberEvent extends ChannelHierarchyChangeEvent {
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ChannelDemoteMemberEvent}.
     * 
     * @param source  The source of this event.
     * @param channel The associated channel with this event.
     * @param username  The username of the user to demote.
     */
    public ChannelDemoteMemberEvent(Object source, Channel channel, String username) {
        super(source, channel, username);
    }
}
