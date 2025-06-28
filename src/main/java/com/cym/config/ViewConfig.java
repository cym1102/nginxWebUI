package com.cym.config;

import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.view.freemarker.FreemarkerRender;

@Configuration
public class ViewConfig {
    public static String lang;
    @Bean
    public void init(FreemarkerRender render) throws Exception {
        //todo: 从 “app.onEvent(freemarker.template.Configuration.class, cfg->{})“ 迁移过来
        render.getProvider().setSetting("classic_compatible", "true");
        render.getProvider().setSetting("number_format", "0.##");

        if (render.getProviderOfDebug() != null) {
            render.getProviderOfDebug().setSetting("classic_compatible", "true");
            render.getProviderOfDebug().setSetting("number_format", "0.##");
        }
    }
}
