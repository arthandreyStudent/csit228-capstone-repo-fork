package com.csit228.capstone.observer;

import com.csit228.capstone.model.TicketView;
import java.util.List;

public interface TicketObserver {
    void onTicketChange(List<TicketView> updatedTickets);
}