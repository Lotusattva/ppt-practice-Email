package email;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.TreeMap;

/**
 * A datatype that represents a mailbox or collection of email.
 */
public class MailBox {

    TreeMap<Email, Boolean> emails = new TreeMap<>((e1, e2) -> {
        if (e1.getTimestamp() == e2.getTimestamp()) {
            return e2.getId().compareTo(e1.getId());
        }
        return e2.getTimestamp() - e1.getTimestamp();
    });

    /**
     * Add a new message to the mailbox
     *
     * @param msg the message to add
     * @return true if the message was added to the mailbox,
     *         and false if it was not added to the mailbox (because a duplicate
     *         exists
     *         or msg was null)
     */
    public boolean addMsg(Email msg) {
        if (msg == null) {
            return false;
        }
        if (emails.containsKey(msg)) {
            return false;
        }
        emails.put(msg, false);
        return true;
    }

    /**
     * Return the email with the provided id
     * 
     * @param msgID the id of the email to retrieve, is not null
     * @return the email with the provided id
     *         and null if such an email does not exist in this mailbox
     */
    public Email getMsg(UUID msgID) {
        return emails.keySet().stream().filter(e -> e.getId().equals(msgID)).findFirst().orElse(null);
    }

    /**
     * Delete a message from the mailbox
     *
     * @param msgId the id of the message to delete
     * @return true if the message existed in the mailbox and it was removed,
     *         else return false
     */
    public boolean delMsg(UUID msgId) {
        if (emails.keySet().removeIf(e -> e.getId().equals(msgId))) {
            return true;
        }
        return false;
    }

    /**
     * Obtain the number of messages in the mailbox
     *
     * @return the number of messages in the mailbox
     */
    public int getMsgCount() {
        return emails.size();
    }

    /**
     * Mark the message with the given id as read
     *
     * @param msgID the id of the message to mark as read, is not null
     * @return true if the message exists in the mailbox and false otherwise
     */
    public boolean markRead(UUID msgID) {
        Email email = getMsg(msgID);
        if (email == null) {
            return false;
        }
        emails.put(email, true);
        return true;
    }

    /**
     * Mark the message with the given id as unread
     *
     * @param msgID the id of the message to mark as unread, is not null
     * @return true if the message exists in the mailbox and false otherwise
     */
    public boolean markUnread(UUID msgID) {
        Email email = getMsg(msgID);
        if (email == null) {
            return false;
        }
        emails.put(email, false);
        return true;
    }

    /**
     * Determine if the specified message has been read or not
     *
     * @param msgID the id of the message to check, is not null
     * @return true if the message has been read and false otherwise
     * @throws IllegalArgumentException if the message does not exist in the mailbox
     */
    public boolean isRead(UUID msgID) {
        Email email = getMsg(msgID);
        if (email == null) {
            throw new IllegalArgumentException();
        }
        return emails.get(email);
    }

    /**
     * Obtain the number of unread messages in this mailbox
     * 
     * @return the number of unread messages in this mailbox
     */
    public int getUnreadMsgCount() {
        return (int) emails.values().stream().filter(b -> !b).count();
    }

    /**
     * Obtain a list of messages in the mailbox, sorted by timestamp,
     * with most recent message first
     *
     * @return a list that represents a view of the mailbox with messages sorted
     *         by timestamp, with most recent message first. If multiple messages
     *         have
     *         the same timestamp, the ordering among those messages is arbitrary.
     */
    public List<Email> getTimestampView() {
        return emails.keySet().stream().toList();
    }

    /**
     * Obtain all the messages with timestamps such that
     * startTime <= timestamp <= endTime,
     * sorted with the earliest message first and breaking ties arbitrarily
     *
     * @param startTime the start of the time range, >= 0
     * @param endTime   the end of the time range, >= startTime
     * @return all the messages with timestamps such that
     *         startTime <= timestamp <= endTime,
     *         sorted with the earliest message first and breaking ties arbitrarily
     */
    public List<Email> getMsgsInRange(int startTime, int endTime) {
        List<Email> msgs = emails.keySet().stream()
                .filter(e -> e.getTimestamp() >= startTime && e.getTimestamp() <= endTime)
                .collect(Collectors.toList());
        msgs.sort((e1, e2) -> e1.getTimestamp() - e2.getTimestamp());
        return msgs;
    }

    /**
     * Mark all the messages in the same thread as the message
     * with the given id as read
     * 
     * @param msgID the id of a message whose entire thread is to be marked as read
     * @return true if a message with that id is in this mailbox
     *         and false otherwise
     */
    public boolean markThreadAsRead(UUID msgID) {
        Email email = getMsg(msgID);
        if (email == null) {
            return false;
        }
        emails.put(email, true);
        markUpstreamAs(email, true);
        markDownstreamAs(email, true);
        return true;
    }

    private void markUpstreamAs(Email email, boolean read) {
        while (email.getResponseTo() != Email.NO_PARENT_ID) {
            email = getMsg(email.getResponseTo());
            emails.put(email, read);
        }
    }

    private void markDownstreamAs(Email email, boolean read) {
        for (Email e : emails.keySet()) {
            if (e.getResponseTo().equals(email.getId())) {
                emails.put(e, read);
                markDownstreamAs(e, read);
            }
        }
    }

    /**
     * Mark all the messages in the same thread as the message
     * with the given id as unread
     * 
     * @param msgID the id of a message whose entire thread is to be marked as
     *              unread
     * @return true if a message with that id is in this mailbox
     *         and false otherwise
     */
    public boolean markThreadAsUnread(UUID msgID) {
        Email email = getMsg(msgID);
        if (email == null) {
            return false;
        }
        emails.put(email, false);
        markUpstreamAs(email, false);
        markDownstreamAs(email, false);
        return true;
    }

    /**
     * Obtain a list of messages, organized by message threads.
     * <p>
     * The message thread view organizes messages by starting with the thread
     * that has the most recent activity (based on timestamps of messages in the
     * thread) first, and within a thread more recent messages appear first.
     * If multiple emails within a thread have the same timestamp then the
     * ordering among those messages is arbitrary. Similarly, if more than one
     * thread can be considered "most recent", those threads can be ordered
     * arbitrarily.
     * <p>
     * A thread is identified by using information in an email that indicates
     * whether an email was in response to another email. The group of emails
     * that can be traced back to a common parent email message form a thread.
     *
     * @return a list that represents a thread-based view of the mailbox.
     */
    public List<Email> getThreadedView() {
        List<List<Email>> threads = new ArrayList<>();
        for (Email email : emails.keySet()) {
            if (email.getResponseTo() == Email.NO_PARENT_ID) {
                threads.add(getThread(email));
            }
        }
        threads.sort((t1, t2) -> t2.get(0).getTimestamp() - t1.get(0).getTimestamp());
        return threads.stream().flatMap(List::stream).toList();
    }

    private List<Email> getThread(Email email) {
        List<Email> thread = new ArrayList<>();
        thread.add(email);
        addDownstream(email, thread);
        thread.sort((e1, e2) -> e2.getTimestamp() - e1.getTimestamp());
        return thread;
    }

    private void addDownstream(Email email, List<Email> thread) {
        for (Email e : emails.keySet()) {
            if (e.getResponseTo().equals(email.getId())) {
                thread.add(e);
                addDownstream(e, thread);
            }
        }
    }
}
