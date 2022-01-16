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

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前拦截
     * 该用户如果登录了就设置 userId，否则设置 userKey
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 Redis 中获取 Session 数据
        HttpSession session = request.getSession();
        // 从 Session 中获取 loginUser 信息：memberResponseVo
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        // UserInfoTo 负责封装用户的 id 信息。
        // 该数据将存储在 ThreadLocal 里，在当前线程内共享，以便 Service 层能获取到该数据
        UserInfoTo userInfoTo = new UserInfoTo();

        // 1. 如果用户已经登录，则设置 userInfoTo.userId 为用户 id
        if (memberResponseVo != null){
            userInfoTo.setUserId(memberResponseVo.getId());
        }

        // 2. 如果用户没登录，userInfoTo.userId 就是 Null，代表该用户是临时用户。使用 userKey 标识其身份
        Cookie[] cookies = request.getCookies();
        // 遍历每一个 Cookie，判断其中是否有 user-Key，如果有，说明当前临时用户之前就访问过本网站
        // 否则说明其没有访问过本网站，就要为其生成一个 user-key = uuid
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                // 如果浏览器中已经有 user-Key，代表该临时用户之前已经访问过本网站，
                // 则直接设置 userInfoTo.userKey 为 Cookie 中之前存储过的 user-key 对应的值
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    // 设置浏览器已有 user-key
                    userInfoTo.setTempUser(true);
                }
            }
        }

        // 3. 如果浏览器没有 user-key，我们通过 uuid 为其生成一个 user-key
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            // 设置其 userKey 为 uuid
            userInfoTo.setUserKey(uuid);
        }
        // 将该数据存储在 ThreadLocal 里，在当前线程内共享，以便 Service 层能获取到该数据
        threadLocal.set(userInfoTo);
        // 放行
        return true;
    }

    /**
     * 业务执行之后执行。分配临时用户的 Cookie，返给浏览器令其保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 获取 userInfoTo
        UserInfoTo userInfoTo = threadLocal.get();
        // 如果浏览器中没有 user-key，我们为其生成，并返回给浏览器保存该 Cookie
        if (!userInfoTo.getTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            // 设置父域
            cookie.setDomain("localhost");
            // 设置过期时间，这样即使浏览器关闭也存在。就能做到30天内只要该用户再访问，还能看到之前添加的购物车信息
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
