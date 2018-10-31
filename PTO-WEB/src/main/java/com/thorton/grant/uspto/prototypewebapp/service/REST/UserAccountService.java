package com.thorton.grant.uspto.prototypewebapp.service.REST;


import com.thorton.grant.uspto.prototypewebapp.factories.ServiceBeanFactory;
import com.thorton.grant.uspto.prototypewebapp.interfaces.Secruity.UserCredentialsService;
import com.thorton.grant.uspto.prototypewebapp.model.entities.security.UserCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@Service
public class UserAccountService {

    private final ServiceBeanFactory serviceBeanFactory;

    public UserAccountService(ServiceBeanFactory serviceBeanFactory) {
        this.serviceBeanFactory = serviceBeanFactory;
    }




    // will add request headers for security in the next update
    @RequestMapping(value = "/rest/api/test", headers = "key=val", method = GET)
    @ResponseBody
    public String restTest() {
        return "Get some Foos with Header";
    }





    @CrossOrigin(origins = {"http://localhost:80","http://efile-reimagined.com"})
    @RequestMapping(method = GET, value="/REST/apiGateway/user/update/pw/{password1}/{password2}")
    @ResponseBody
    ResponseEntity<String> updateUserPassword(@PathVariable String password1, @PathVariable String password2){



        // retrieve current userName from spring security

        // check if current password matches what is stored

        // set status code based on if that matched

        // if matched. update password for credentials object
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials userCredentials = userCredentialsService.findByEmail(email);
        boolean pwMatched = bCryptPasswordEncoder.matches(password1, userCredentials.getPassword());
        //String storedPw = authentication.getPrincipal().toString();

        String statusCode = "200";
        String responseMsg;


        if(pwMatched == false){
            statusCode = "420";
            responseMsg = "Entered current password did not match!";
        }
        else {
            // update pass word
            // verify password is not null
            if(password2 != "" || password2 != null){

                // user userCredentials object to save passowrd, then save object to repository via service
                userCredentials.setPassword(bCryptPasswordEncoder.encode(password2));
                userCredentialsService.save(userCredentials);

                responseMsg = "Your new password have been saved";

            }
            else{
                statusCode = "444";
                responseMsg = "New password can not be empty.";
            }
        }

        responseMsg = "{status:" + statusCode +" } { msg:"+responseMsg+" }";
        System.out.println(password1);
        //UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        //userCredentialsService.findByEmail(email);

        HttpHeaders responseHeader = new HttpHeaders ();
        //responseHeader.set("Access-Control-Allow-Origin", "http://18.221.138.198:8080");
        responseHeader.setAccessControlAllowOrigin("http://efile-reimagined.com");
        ArrayList<String> headersAllowed = new ArrayList<String>();
        headersAllowed.add("Access-Control-Allow-Origin");
        responseHeader.setAccessControlAllowHeaders(headersAllowed);
        ArrayList<String> methAllowed = new ArrayList<String>();
        //methAllowed.add("GET");
        //methAllowed.add("POST");
        //responseHeader.setAccessControlAllowMethods(methAllowed);
        System.out.println("response header : "+responseHeader.getAccessControlAllowOrigin());


       return ResponseEntity.ok().headers(responseHeader).body(responseMsg) ;

    }

    @WebFilter("/REST/apiGateway*")
    public class AddResponseHeaderFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setHeader(
                    "Access-Control-Allow-Origin", "http://efile-reimagined.com");
            chain.doFilter(request, response);
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // ...
        }

        @Override
        public void destroy() {
            // ...
        }
    }


}
