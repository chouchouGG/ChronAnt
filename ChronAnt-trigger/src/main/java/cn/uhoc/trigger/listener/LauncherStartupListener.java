package cn.uhoc.trigger.listener;

import cn.uhoc.domain.launcher.service.Launcher;
import lombok.NonNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LauncherStartupListener {

    private final Launcher l;

    public LauncherStartupListener(Launcher launcher) {
        this.l = launcher;
    }

    @EventListener
    public void onApplicationReady(@NonNull ApplicationReadyEvent event) {
        l.start();
    }
}