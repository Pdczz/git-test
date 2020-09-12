package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "leyou.jwt")
public class JwtProperties {


    private String pubKeyPath;// 公钥

    private PublicKey publicKey; // 公钥

    private String cookieName;


    @PostConstruct
    public void init() throws Exception {

        // 获取公钥和私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);


    }

}
