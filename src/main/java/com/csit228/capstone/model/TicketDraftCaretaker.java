package com.csit228.capstone.model;

public class TicketDraftCaretaker {

  private TicketMemento draft;

  public TicketDraftCaretaker() {}


  public TicketMemento restoreDraft() {
    return draft;
  }

  public boolean hasDraft() {
    return draft != null;
  }

  public void clearDraft() {
    draft = null;
  }
}
