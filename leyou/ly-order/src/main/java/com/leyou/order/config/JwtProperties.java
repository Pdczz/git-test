package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
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
