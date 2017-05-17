package com.faendir.acra.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.security.SecureRandom;
import java.util.List;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Component
public class AppManager {
    private final SecureRandom secureRandom;
    private final AppRepository appRepository;

    @Autowired
    public AppManager(SecureRandom secureRandom, AppRepository appRepository) {
        this.secureRandom = secureRandom;
        this.appRepository = appRepository;
    }

    public App createNewApp(String name){
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        return appRepository.save(new App(name, Base64Utils.encodeToString(bytes)));
    }

    public List<App> getApps(){
        return appRepository.findAll();
    }

    public App getApp(String id){
        return appRepository.findOne(id);
    }
}
