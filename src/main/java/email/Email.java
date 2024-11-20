package email;

import java.util.Objects;
import java.util.UUID;

/**
 * <p><strong>A datatype that represents an email message.</strong></p>
 *
 * <p>
 * An email message contains:
 * <ul>
 * <li>a unique message identifier</li>
 * <li>an integer timestamp</li>
 * <li>a sender</li>
 * <li>a recipient</li>
 * <li>a subject</li>
 * <li>a message body</li>
 * <li>an identifier of the message this was a response to</li>
 * </ul>
 * </p>
 */
public class Email implements Cloneable, TimestampedObject {

    public static final UUID NO_PARENT_ID = UUID.fromString("78dfc210-e294-11e8-9f32-f2801f1b9fd1");

    private final UUID      msgId;
    private final UUID      responseTo;
    private final int       timestamp;
    private final String    sender;
    private final String    recipient;
    private final String    subject;
    private final String    body;

    /**
     * Create a new email message with the given message parameters
     *
     * @param timestamp  is >= 0
     * @param sender     is not null or ""
     * @param recipient  is not null or ""
     * @param subject    is not null or ""
     * @param body       is not null or ""
     * @param responseTo is not null
     */
    public Email(int timestamp, String sender, String recipient, String subject, String body, UUID responseTo) {
        this.msgId      = UUID.randomUUID();
        this.timestamp  = timestamp;
        this.sender     = sender;
        this.recipient  = recipient;
        this.subject    = subject;
        this.body       = body;
        this.responseTo = responseTo;
    }

    /**
     * Create a new email message that is not a response to another message
     *
     * @param timestamp  is >= 0
     * @param sender     is not null or ""
     * @param recipient  is not null or ""
     * @param subject    is not null or ""
     * @param body       is not null or ""
     */
    public Email(int timestamp, String sender, String recipient, String subject, String body) {
        msgId           = UUID.randomUUID();
        this.timestamp  = timestamp;
        this.sender     = sender;
        this.recipient  = recipient;
        this.subject    = subject;
        this.body       = body;
        this.responseTo = Email.NO_PARENT_ID;
    }

    /**
     * Obtain the message identifier
     *
     * @return the message's unique identifier
     */
    public UUID getId() {
        return msgId;
    }

    /**
     * Obtain the message timestamp
     *
     * @return the message's timestamp
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Obtain the message sender
     *
     * @return the message's sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Obtain the message recipient
     *
     * @return the message's recipient
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Obtain the message subject
     *
     * @return the message's subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Obtain the message body
     * @return the message's body
     */
    public String getBody() {
        return body;
    }

    /**
     * Obtain the UUID of the message that this message was a response to
     * @return the UUID of the message that this was a response to
     */
    public UUID getResponseTo() {
        return responseTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Email email = (Email) o;
        return Objects.equals(msgId, email.msgId);
    }

    @Override
    public int hashCode() {
        return msgId.hashCode();
    }

    @Override
    public Email clone() throws CloneNotSupportedException {
        return (Email) super.clone();
    }

    @Override
    public String toString() {
        return this.msgId.toString() + " | " + this.sender + " | "
                + this.recipient + " | " + this.subject + " | " + this.body;
    }
}
