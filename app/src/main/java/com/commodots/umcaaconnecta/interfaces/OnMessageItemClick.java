package com.commodots.umcaaconnecta.interfaces;

import com.commodots.umcaaconnecta.models.Message;

/**
 * Created by a_man on 5/14/2017.
 */

public interface OnMessageItemClick {
    void OnMessageClick(Message message, int position);

    void OnMessageLongClick(Message message, int position);
}
