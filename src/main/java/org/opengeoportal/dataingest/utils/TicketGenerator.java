/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.utils;

import java.util.HashMap;

import org.opengeoportal.dataingest.exception.GeoServerException;
import org.opengeoportal.dataingest.exception.RequestNotPresentException;

/**
 * The Class TicketGenerator.
 */
public class TicketGenerator {
    
    /** The ticket list. */
    private static HashMap<Long, Boolean> ticketList = new HashMap<Long, Boolean>();
    /** The message list. */
    private static HashMap<Long, String> messageList = new HashMap<Long, String>();
    
    /**
     * Opens a ticket.
     *
     * @return the long
     */
    public synchronized static long openATicket() {
        
        Long ticket = System.currentTimeMillis();
        
        ticketList.put(ticket, false);
        return ticket;
    }
    
    /**
     * Closes a ticket.
     *
     * @param ticket the ticket
     */
    public synchronized static void closeATicket(long ticket) {
        
        // TRUE when done
        ticketList.put(ticket, true);
    }
    
    /**
     * Close a ticket with error message.
     * @param ticket
     */
    public synchronized static void closeATicket(long ticket, String msg) {
        
        // TRUE when done
        ticketList.put(ticket, true);
        messageList.put(ticket, msg);
    }
    
    /**
     * Checks if it's closed.
     *
     * @param ticket the ticket
     * @return true, if is closed
     * @throws RequestNotPresentException 
     * @throws GeoServerException 
     */
    public static boolean isClosed(long ticket) throws RequestNotPresentException, GeoServerException {
        
        Boolean status = ticketList.get(ticket);
        
            if(status != null) {
                if(!status) {
                    return false;
                }
                if(status && messageList.get(ticket)==null) {
                    return true;
                } else {
                    throw new GeoServerException(messageList.get(ticket));
                }
            } else {
                throw new RequestNotPresentException();
            }
        
    }

}
