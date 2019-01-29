package demo.sync.boot.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

import demo.sync.boot.service.HelloService;

@RestController
public class SyncController {

	@Autowired
	HelloService hello;

	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

	/**
	 * http://localhost:8080/helloworld
	 * 
	 * @return
	 */
	@GetMapping("/helloworld")
	public String helloWorldController() {
		return hello.sayHello();
	}

	/**
	 * 
	 * http://localhost:8080/hello_callable
	 * 
	 * 异步调用restful
	 * 当controller返回值是Callable的时候，springmvc就会启动一个线程将Callable交给TaskExecutor去处理
	 * 然后DispatcherServlet还有所有的spring拦截器都退出主线程，然后把response保持打开的状态
	 * 当Callable执行结束之后，springmvc就会重新启动分配一个request请求，然后DispatcherServlet就重新
	 * 调用和处理Callable异步执行的返回结果， 然后返回视图
	 * 
	 * @return
	 */
	@GetMapping("/hello_callable")
	public Callable<String> helloController() {
		System.err.println(Thread.currentThread().getName() + " 进入helloController方法");
		Callable<String> callable = new Callable<String>() {
			@Override
			public String call() throws Exception {
				System.err.println(Thread.currentThread().getName() + " 进入call方法");
				String say = hello.sayHello();
				System.err.println(Thread.currentThread().getName() + " 从helloService方法返回");
				return say;
			}
		};
		System.err.println(Thread.currentThread().getName() + " 从helloController方法返回");
		return callable;
	}

	/**
	 * 带超时时间的异步请求 通过WebAsyncTask自定义客户端超时间
	 * 
	 * @return
	 */
	@GetMapping("/world")
	public WebAsyncTask<String> worldController() {
		System.err.println(Thread.currentThread().getName() + " 进入helloController方法");
		
		// 3s钟没返回，则认为超时
		WebAsyncTask<String> webAsyncTask = new WebAsyncTask<>(3000, new Callable<String>() {

			@Override
			public String call() throws Exception {
				System.err.println(Thread.currentThread().getName() + " 进入call方法");
				String say = hello.sayHello();
				System.err.println(Thread.currentThread().getName() + " 从helloService方法返回");
				return say;
			}
		});

		System.err.println(Thread.currentThread().getName() + " 从helloController方法返回");

		webAsyncTask.onCompletion(new Runnable() {

			@Override
			public void run() {
				System.err.println(Thread.currentThread().getName() + " 执行完毕");
			}
		});

		webAsyncTask.onTimeout(new Callable<String>() {

			@Override
			public String call() throws Exception {
				System.err.println(Thread.currentThread().getName() + " onTimeout");
				// 超时的时候，直接抛异常，让外层统一处理超时异常
				throw new TimeoutException("调用超时");
			}
		});
		return webAsyncTask;
	}

	/**
	 * 异步调用，异常处理，详细的处理流程见MyExceptionHandler类
	 * 
	 * @return
	 */
	@GetMapping("/exception")
	public WebAsyncTask<String> exceptionController() {
		System.err.println(Thread.currentThread().getName() + " 进入helloController方法");
		Callable<String> callable = new Callable<String>() {

			@Override
			public String call() throws Exception {
				System.err.println(Thread.currentThread().getName() + " 进入call方法");
				throw new TimeoutException("调用超时!");
			}
		};
		System.err.println(Thread.currentThread().getName() + " 从helloController方法返回");
		return new WebAsyncTask<>(20000, callable);
	}
	
	
	
	@Async
	public void execute(DeferredResult<String> deferred){
		System.err.println(Thread.currentThread().getName() + "进入 taskService 的 execute方法");
		try {
			// 模拟长时间任务调用，睡眠2s
			TimeUnit.SECONDS.sleep(2);

			// 2s后给Deferred发送成功消息，告诉Deferred，我这边已经处理完了，可以返回给客户端了
			deferred.setResult("world");

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

//    @GetMapping("/deferred")
//    public DeferredResult<String> executeSlowTask() {
//        System.err.println(Thread.currentThread().getName() + "进入executeSlowTask方法");
//        DeferredResult<String> deferredResult = new DeferredResult<>();
//        // 调用长时间执行任务
//        taskService.execute(deferredResult);
//        // 当长时间任务中使用deferred.setResult("world");这个方法时，会从长时间任务中返回，继续controller里面的流程
//        System.err.println(Thread.currentThread().getName() + "从executeSlowTask方法返回");
//        // 超时的回调方法
//        deferredResult.onTimeout(new Runnable(){
//			@Override
//			public void run() {
//				System.err.println(Thread.currentThread().getName() + " onTimeout");
//				// 返回超时信息
//				deferredResult.setErrorResult("time out!");
//			}
//		});
//        
//        // 处理完成的回调方法，无论是超时还是处理成功，都会进入这个回调方法
//        deferredResult.onCompletion(new Runnable(){
//		
//			@Override
//			public void run() {
//				System.err.println(Thread.currentThread().getName() + " onCompletion");
//			}
//		});
//        
//        return deferredResult;
//    }
	

}
