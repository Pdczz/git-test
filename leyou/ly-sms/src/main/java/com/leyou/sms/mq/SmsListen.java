package com.leyou.sms.mq;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListen {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties prop;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 发送短信验证码
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name="sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange",type = ExchangeTypes.TOPIC),
            key = {"sms.verify.code"}
    ))
    public void listensendMsg(Map<String,String> msg)  {

        if (CollectionUtils.isEmpty(msg)){
            return;
        }
        String phone = msg.remove("phone");
        if (StringUtils.isBlank(phone)){
            return;
        }
        String key="sms:phone:"+phone;
        //读取时间
        String lastTime = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(lastTime)){
            Long last = Long.valueOf(lastTime);
            if (System.currentTimeMillis() - last < 60000){
                log.info("[短信服务] 发送频率过高，被拦截手机号:{}",phone);
                return ;
            }
        }
        redisTemplate.opsForValue().set(key,String.valueOf(System.currentTimeMillis()),1, TimeUnit.MINUTES);
        //smsUtils.sendSms(phone,prop.getSignName(),prop.getVerifyCodeTemplate(), JsonUtils.serialize(msg));
        log.info("[短信服务] 发送短信验证码，手机号:{}",phone);
    }

}
