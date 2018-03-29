package com.breakthecore;

public interface Observer {
    void onNotify(NotificationType type, Object ob);
}
