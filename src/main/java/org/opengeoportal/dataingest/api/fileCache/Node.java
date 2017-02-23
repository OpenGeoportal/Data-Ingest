package org.opengeoportal.dataingest.api.fileCache;

/**
 * Created by joana on 22/02/17.
 */
class Node {
    /**
     * Typename name = workspace:dataset .
     */
    private String key;
    /**
     * Size of the file.
     */
    private long value;
    /**
     * Previous node.
     */
    private Node pre;
    /**
     * Next node.
     */
    private Node next;

    /**
     * Class constructor, where we initialize key and value.
     *
     * @param key
     *            key.
     * @param value
     *            value.
     */
    Node(final String key, final long value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get Key.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Set key .
     *
     * @param key
     *            key
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * Get value.
     *
     * @return value
     */
    public long getValue() {
        return value;
    }

    /**
     * Set value.
     *
     * @param value
     *            value
     */
    public void setValue(final long value) {
        this.value = value;
    }

    /**
     * Get previous node.
     *
     * @return previous node.
     */
    public Node getPre() {
        return pre;
    }

    /**
     * Set previous node.
     *
     * @param pre
     *            previous node
     */
    public void setPre(final Node pre) {
        this.pre = pre;
    }

    /**
     * Get next nodel.
     *
     * @return next node.
     */
    public Node getNext() {
        return next;
    }

    /**
     * Set next node.
     *
     * @param next
     *            next node.
     */
    public void setNext(final Node next) {
        this.next = next;
    }
}
