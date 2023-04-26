package com.example.wish.component;

import java.util.Map;

public interface NotificationContentResolver {

    String resolve(String template, Map<String, Object> model);
}
