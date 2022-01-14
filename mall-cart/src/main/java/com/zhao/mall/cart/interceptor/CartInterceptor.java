package com.zhao.mall.cart.interceptor;

import com.zhao.common.constant.AuthServerConstant;
import com.zhao.common.constant.CartConstant;
import com.zhao.common.vo.MemberResponseVo;
import com.zhao.mall.cart.to.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法之前，先判断用户的登录状态。并封装传递给 Controller 目标请求
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal=new ThreadLocal<>();

    /**
     * 在目标方法执行之前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        UserInfoTo userInfoTo = new UserInfoTo();
        // 1 用户已经登录，设置userId
        if (memberResponseVo != null){
            userInfoTo.setUserId(memberResponseVo.getId());
        }
        // 如果用户没登录，userId就是Null，代表该用户是临时用户。

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                // 2 如果浏览器中已经有user-Key，则直接设置
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }

        // 3 如果浏览器没有user-key，我们通过uuid生成user-key
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行之后执行。分配临时用户的Cookie，返给浏览器令其保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        // 如果浏览器中没有user-key，我们为其生成，并返回给浏览器该Cookie
        if (!userInfoTo.getTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("localhost");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
