package com.thorton.grant.uspto.prototypewebapp.interfaces.Secruity;

import com.thorton.grant.uspto.prototypewebapp.model.entities.DTO.RegistrationDTO;
import com.thorton.grant.uspto.prototypewebapp.model.entities.security.UserCredentials;
import com.thorton.grant.uspto.prototypewebapp.model.entities.security.VerificationToken;

/////////////////////////////////////////
// use identity utility class interface
/////////////////////////////////////////

public interface IUserService {
    UserCredentials registerNewUserAccount( RegistrationDTO accountDto);
    void createVerificationToken(UserCredentials userCredentials, String token);
    UserCredentials getUserCredential(String verificationToken);

    void saveRegisteredUserCredential(UserCredentials userCredentials);

    VerificationToken getVerificationToken(String VerificationToken);

}