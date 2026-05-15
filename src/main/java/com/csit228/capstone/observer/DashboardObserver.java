// com/csit228/capstone/observer/DashboardObserver.java
package com.csit228.capstone.observer;

import com.csit228.capstone.model.TicketView;
import java.util.List;

public interface DashboardObserver {
    void onDataChanged(List<TicketView> updatedTickets);
}