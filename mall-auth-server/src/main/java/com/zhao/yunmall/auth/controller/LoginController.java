package com.zhao.yunmall.auth.controller;

import com.zhao.common.constant.AuthServerConstant;
import com.zhao.common.exception.BizCodeEnum;
import com.zhao.common.utils.R;
import com.zhao.yunmall.auth.feign.MemberFeignService;
import com.zhao.yunmall.auth.feign.ThirdPartFeignService;
import com.zhao.yunmall.auth.vo.UserLoginVo;
import com.zhao.yunmall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yuyun zhao
 * @date 2022/1/12 13:54
 */
@Controller
public class LoginController {

	@Autowired
	ThirdPartFeignService thirdPartFeignService;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Autowired
	MemberFeignService memberFeignService;


	/**
	 * 大量的页面跳转需求：发送一个请求，直接跳转到一个页面
	 * 可以使用 Spring MVC viewController，将请求和页面进行映射，简化开发
	 *
	 * @return
	 */

	@ResponseBody
	@GetMapping("/sms/sendCode")
	public R sendCode(@RequestParam("phone") String phone) {
		// TODO 1. 接口防刷


		// 首先检验用户是否在60秒内重复点击发送按钮，
		// 检验方式为：去Redis中查询当前手机号对应的value值（为该短信上一次发送的时间），如果该时间与当前相比不大于60秒，则拒绝发送
		String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
		if (!StringUtils.isEmpty(redisCode)) {
			long time = Long.parseLong(redisCode.split("_")[1]);
			if (System.currentTimeMillis() - time < 60 * 1000) {
				// 如果发送间隔小于60s，不允许再发送
				return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
			}
		}

		// 当可以再次发送时，随机生成验证码，并将该验证码存储在 Redis 中
		String code = UUID.randomUUID().toString().substring(0, 5);
		// 向 Redis 中存储验证码时，需要带上当前时间（为了保证60秒内不能重复发送短信）
		String codeWithTime = code + "_" + System.currentTimeMillis();
		// 将用户的手机号和验证码存到缓存中。格式：prefix:phoneNumber-currentTime - 验证码
		// 例如 sms:code:1829847-83627171 - s82hd
		// 新的验证码会覆盖旧的。给每个验证码设置过期时间为十分钟
		redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
				codeWithTime, 10, TimeUnit.MINUTES);
		// 远程调用第三方服务，向用户手机发送验证码
		thirdPartFeignService.sendCode(phone, code);
		return R.ok();
	}

	/**
	 * 	使用重定向，防止用户多次刷新页面时重复提交
	 * @param registerVo
	 * @param result
	 * @param attributes
	 * @return
	 */
	@PostMapping("/register")
	public String register(@Valid UserRegisterVo registerVo, BindingResult result, RedirectAttributes attributes) {
		//1.判断校验是否通过
		Map<String, String> errors = new HashMap<>();
		if (result.hasErrors()){
			//1.1 如果校验不通过，则封装校验结果
			result.getFieldErrors().forEach(item->{
				errors.put(item.getField(), item.getDefaultMessage());
				//1.2 将错误信息封装到session中
				attributes.addFlashAttribute("errors", errors);
			});
			//1.2 重定向到注册页
			return "redirect:http://localhost:20000/reg.html";
		}else {
			//2.若JSR303校验通过
			//判断验证码是否正确
			String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
			//2.1 如果对应手机的验证码不为空且与提交上的相等 -> 验证码正确
			if (!StringUtils.isEmpty(code) && registerVo.getCode().equals(code.split("_")[0])) {
				//2.1.1 使得验证后的验证码失效
				redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());

				//2.1.2 远程调用会员服务注册
				R r = memberFeignService.register(registerVo);
				if (r.getCode() == 0) {
					//调用成功，重定向登录页
					return "redirect:http://localhost:20000/login.html";
				}else {
					//调用失败，返回注册页并显示错误信息
					String msg = (String) r.get("msg");
					errors.put("msg", msg);
					attributes.addFlashAttribute("errors", errors);
					return "redirect:http://localhost:20000/reg.html";
				}
			}else {
				//2.2 验证码错误
				errors.put("code", "验证码错误");
				attributes.addFlashAttribute("errors", errors);
				return "redirect:http://localhost:20000/reg.html";
			}
		}
	}

	@PostMapping("/login")
	public String login(UserLoginVo vo, RedirectAttributes redirectAttributes) {
		R res = memberFeignService.login(vo);
		if (res.getCode() == 0) {
			// 成功
			return "redirect:http://localhost:10000";
		} else {
			Map<String, String> errors = new HashMap<>();
			errors.put("msg", (String) res.get("msg"));
			redirectAttributes.addFlashAttribute("errors", errors);
			return "redirect:http://localhost:20000/login.html";
		}

	}


}
