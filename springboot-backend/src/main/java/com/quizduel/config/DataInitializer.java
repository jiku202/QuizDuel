package com.quizduel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.quizduel.service.DemoDataService;

/** Runs once at startup, after the schema exists. */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DemoDataService demo;
    private final boolean seedDemo;

    public DataInitializer(DemoDataService demo, @Value("${app.seed-demo-data}") boolean seedDemo) {
        this.demo = demo;
        this.seedDemo = seedDemo;
    }

    @Override
    public void run(ApplicationArguments args) {
        demo.ensureDefaultBadges();
        if (seedDemo && demo.seedDemoData()) {
            log.info("Seeded demo data. Teacher login: teacher@school.edu.ph / password123 (join code MABINI)");
        }
    }
}
