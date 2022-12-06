package com.shubh.accounts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.shubh.accounts.clients.CardsFeignClient;
import com.shubh.accounts.clients.LoanFeignClient;
import com.shubh.accounts.config.AccountServiceConfig;
import com.shubh.accounts.model.Accounts;
import com.shubh.accounts.model.Cards;
import com.shubh.accounts.model.Customer;
import com.shubh.accounts.model.CustomerDetails;
import com.shubh.accounts.model.Loans;
import com.shubh.accounts.model.Properties;
import com.shubh.accounts.repository.AccountsRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;

@RestController
public class AccountsController {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);
	@Autowired
	LoanFeignClient loanFeignClient;
	
	@Autowired
	CardsFeignClient cardsFeignClient;
	
	@Autowired
	private AccountsRepository accountsRepository;
	
	@Autowired
	AccountServiceConfig accountServiceConfig;

	@PostMapping("/myAccount")
	@Timed(value = "getAccountDetails.time", description = "Time taken to return Account Details")
	public Accounts getAccountDetails(@RequestBody Customer customer) {

		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		if (accounts != null) {
			return accounts;
		} else {
			return null;
		}

	}
	
	@GetMapping("/account/properties")
	public String getPropertyDetails() throws JsonProcessingException{
		ObjectWriter objectWriter=new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties=new Properties(accountServiceConfig.getMsg(), accountServiceConfig.getBuildVersion(), accountServiceConfig.getMailDetails(), accountServiceConfig.getActiveBranches());
		String jsonStr=objectWriter.writeValueAsString(properties);
		
		return jsonStr;
	}
	
	 //@circit braker implimention and fall back 
	 @RequestMapping("/myCustomerDetails")
	 @CircuitBreaker(name = "detailsForCustomerSupportApp",fallbackMethod ="customerFallbackmethod")
	 public CustomerDetails getCustomerDetails(@RequestBody Customer customer){
		 logger.info("myCustomerDetails() method started");
		 Accounts accounts= accountsRepository.findByCustomerId(customer.getCustomerId());
		
		List<Cards> cards=cardsFeignClient.getCardDetails(customer);
		
		List<Loans> loan=loanFeignClient.getLoanDetails(customer);
		
		CustomerDetails customerDetails=new CustomerDetails();
		
		customerDetails.setAccounts(accounts);
		customerDetails.setCards(cards);
		customerDetails.setLoans(loan);
		logger.info("myCustomerDetails() method ended");
		return customerDetails;
	}

	
/*	@RequestMapping("/myCustomerDetailsRetry")
	@Retry(name="retryForCustomerDetails",fallbackMethod ="customerFallbackmethod")
	public CustomerDetails getCustomerDetailsForRetry(@RequestBody Customer customer){
		System.out.println("invoking card service ");
		Accounts accounts= accountsRepository.findByCustomerId(customer.getCustomerId());
		
		List<Cards> cards=cardsFeignClient.getCardDetails(customer);
		
		List<Loans> loan=loanFeignClient.getLoanDetails(customer);
		
		CustomerDetails customerDetails=new CustomerDetails();
		
		customerDetails.setAccounts(accounts);
		customerDetails.setCards(cards);
		customerDetails.setLoans(loan);
		
		return customerDetails;
	} */

	

	public CustomerDetails customerFallbackmethod(Customer customer,Throwable t){
		Accounts accounts= accountsRepository.findByCustomerId(customer.getCustomerId());	
		List<Loans> loan=loanFeignClient.getLoanDetails(customer);
		
		CustomerDetails customerDetails=new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loan);
		return customerDetails;
	}
	
	@GetMapping("/sayHello")
	@RateLimiter(name="sayHello",fallbackMethod = "sayHelloFallback")
	public String sayHello(){
		return "welcome in java wold ";
		
	}
	public String sayHelloFallback(Throwable t) {
		return "i am fall back method";
	}
}
