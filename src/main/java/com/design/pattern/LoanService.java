package com.design.pattern;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static final String SERVICE_NAME ="circuitbreaker-pattern-service";

    private static final String CALLING_TO_SERVICE="http://localhost:9000/api/rates/";


    @CircuitBreaker(name=SERVICE_NAME, fallbackMethod = "getDefaultLoans")
    public List<Loan> getListOfLoanByType(String type) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InterestRate> entity = new HttpEntity<InterestRate>(null, headers);

        ResponseEntity<InterestRate> response = restTemplate
                .exchange(
                        (CALLING_TO_SERVICE+type),
                        HttpMethod.GET, entity, InterestRate.class
                );
        InterestRate  rate = response.getBody();
        List<Loan> loanList =null;
        if(rate!=null){
             loanList = loanRepository.findByType(type).stream()
                    .map(loan -> {
                        loan.setInterest(loan.getAmount()*(rate.getRateValue()/100));
                        return loan;
                    }).collect(Collectors.toList());
        }
        return loanList;
    }

    public List<Loan> getDefaultLoans(Exception e) {
        return new ArrayList<>();
    }
}
