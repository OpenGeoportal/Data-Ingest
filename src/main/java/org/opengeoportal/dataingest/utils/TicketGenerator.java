/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.utils;

import java.util.HashMap;

/**
 * The Class TicketGenerator.
 */
public class TicketGenerator {
    
    /** The ticket list. */
    private static HashMap<Long, Boolean> ticketList = new HashMap<Long, Boolean>();
    
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
     * Checks if it's closed.
     *
     * @param ticket the ticket
     * @return true, if is closed
     */
    public static boolean isClosed(long ticket) {
        return ticketList.get(ticket);
    }

}
