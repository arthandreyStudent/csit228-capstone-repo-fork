package com.csit228.capstone.observer;

import com.csit228.capstone.model.Comment;
import java.util.List;

public interface CommentObserver {
    void onCommentsChanged(List<Comment> updatedComments);

    int getTicketId();
}
