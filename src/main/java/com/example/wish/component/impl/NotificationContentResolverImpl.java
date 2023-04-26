package com.example.wish.component.impl;

import com.example.wish.component.NotificationContentResolver;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class NotificationContentResolverImpl implements NotificationContentResolver {

    private final Configuration fmConfiguration;

    @Override
    public String resolve(String template, Map<String, Object> model) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(fmConfiguration.getTemplate(template), model);

        } catch (IOException | TemplateException e) {
            throw new IllegalArgumentException("Can't resolve string template: " + e.getMessage(), e);
        }
    }
}
