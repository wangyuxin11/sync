package demo.sync.boot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class MyAsyncHandlerInterceptor implements AsyncHandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return true;
	}
 
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
//		HandlerMethod handlerMethod = (HandlerMethod) handler;
		System.err.println(Thread.currentThread().getName()+ "服务调用完成，返回结果给客户端");
	}
 
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if(null != ex){
			System.out.println("发生异常:"+ex.getMessage());
		}
	}
 
	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 拦截之后，重新写回数据，将原来的hello world换成如下字符串
		String resp = "my name is chhliu!";
		response.setContentLength(resp.length());
		response.getOutputStream().write(resp.getBytes());
		
		System.err.println(Thread.currentThread().getName() + " 进入afterConcurrentHandlingStarted方法");
	}
}