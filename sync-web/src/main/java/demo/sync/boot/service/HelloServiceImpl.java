package demo.sync.boot.service;

import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService {

	@Override
	public String sayHello() {
		return "Hello!" + String.valueOf(System.currentTimeMillis());
	}

}
