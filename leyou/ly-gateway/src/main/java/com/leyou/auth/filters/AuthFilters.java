package com.leyou.auth.filters;

import com.leyou.auth.config.FilterProperties;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilters extends ZuulFilter {

    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE; //过滤类型，前置过滤
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;//过滤器顺序
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取URI
        String path = request.getRequestURI();
        //放行返回false

        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path) {
        //遍历白名单
        for (String allowPath : filterProp.getAllowPaths()) {
            //判断是否允许
            if (path.startsWith(allowPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取cookie中的token
        String cookieValue = CookieUtils.getCookieValue(request, prop.getCookieName());
        //解析token
        try {
            UserInfo user = JwtUtils.getInfoFromToken(cookieValue, prop.getPublicKey());
        }catch (Exception e){
            //解析token失败，未登录，拦截
            ctx.setSendZuulResponse(false);
            //返回状态码
            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}
