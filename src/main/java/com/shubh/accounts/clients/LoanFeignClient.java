package com.shubh.accounts.clients;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.shubh.accounts.model.Customer;
import com.shubh.accounts.model.Loans;

@FeignClient("loans")
public interface LoanFeignClient {
	@RequestMapping(method = RequestMethod.POST,value ="/myLoans",consumes = "application/json")
	public List<Loans> getLoanDetails(@RequestBody Customer customer);
	
////@FeignClient(configuration =.class,name="",contextId = "")
}
