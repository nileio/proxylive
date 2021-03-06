/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.segator.proxylive.service;

import com.github.segator.proxylive.config.ProxyLiveConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 * @author isaac
 */
@Configuration
public class AuthenticationServiceFactory {
      @Autowired
    private ProxyLiveConfiguration configuration;
    @Bean
    public AuthenticationService createAuthenticationService() {
        if(configuration.getAuthentication()==null){
            return new WithoutAuthenticationService();
        }
        if(configuration.getAuthentication().getLdap()!=null){
            return new LDAPAuthenticationService();
        }else if(configuration.getAuthentication().getPlex()!=null){
            return new PlexAuthenticationService();
        }else{
            return new WithoutAuthenticationService();
        }
    }
}
