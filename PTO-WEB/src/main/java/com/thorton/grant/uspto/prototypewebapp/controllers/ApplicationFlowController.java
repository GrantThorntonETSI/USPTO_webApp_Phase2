package com.thorton.grant.uspto.prototypewebapp.controllers;


import com.thorton.grant.uspto.prototypewebapp.config.host.bean.endPoint.HostBean;
import com.thorton.grant.uspto.prototypewebapp.factories.ServiceBeanFactory;
import com.thorton.grant.uspto.prototypewebapp.interfaces.Secruity.UserCredentialsService;
import com.thorton.grant.uspto.prototypewebapp.interfaces.USPTO.PTOUserService;
import com.thorton.grant.uspto.prototypewebapp.interfaces.USPTO.tradeMark.application.types.BaseTradeMarkApplicationService;
import com.thorton.grant.uspto.prototypewebapp.model.entities.DTO.application.ContactsDisplayDTO;
import com.thorton.grant.uspto.prototypewebapp.model.entities.DTO.application.SelectedContactsDisplayDTO;
import com.thorton.grant.uspto.prototypewebapp.model.entities.DTO.application.form.NewAttorneyContactFormDTO;
import com.thorton.grant.uspto.prototypewebapp.model.entities.DTO.application.form.NewOwnerContactFormDTO;
import com.thorton.grant.uspto.prototypewebapp.model.entities.DTO.application.form.partnerDTO;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.application.participants.Lawyer;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.application.participants.Owner;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.application.types.BaseTrademarkApplication;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.assets.GoodAndService;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.assets.TradeMark;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.user.ManagedContact;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.user.PTOUser;
import com.thorton.grant.uspto.prototypewebapp.model.entities.security.UserCredentials;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

@Controller
public class ApplicationFlowController {

    private final  ServiceBeanFactory serviceBeanFactory;
    private static long counter = 3000000;


    /////////////////////////////////////////////////////////////////////////////////////////
    // based on the profile  ...we should be able
    // to inject the correct bean mapped to the correct host file here
    ////////////////////////////////////////////////////////////////////////////////////////
    private final HostBean hostBean;


    private final ApplicationContext appContext;

    public ApplicationFlowController(ServiceBeanFactory serviceBeanFactory, ApplicationContext appContext) {
        this.serviceBeanFactory = serviceBeanFactory;
        this.appContext = appContext;

        this.hostBean = (HostBean) appContext.getBean(HostBean.class);

    }

    //private boolean continuation = false;

    @Transactional
    @RequestMapping({"/application/start","application/start"})
    public String applicationStart
        (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account", credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();


        ArrayList<String> contactNamesMC = new ArrayList<>();
        ArrayList<String> contactEmailsMC = new ArrayList<>();
        ArrayList<String> contactSubTypesMC = new ArrayList<>();
        ManagedContact managedContact = null;

        for (Iterator<ManagedContact> iter = ptoUser.getMyManagedContacts().iterator(); iter.hasNext(); ) {
            managedContact = iter.next();
            if (managedContact.getContactType() == "attorney") {
                contactNamesMC.add(managedContact.getDisplayName());
                contactEmailsMC.add(managedContact.getEmail());
                contactSubTypesMC.add(managedContact.getContactType());
            }


        }
        Collections.reverse(contactNamesMC);
        Collections.reverse(contactEmailsMC);
        Collections.reverse(contactSubTypesMC);
        ContactsDisplayDTO mcDisplayDTO = new ContactsDisplayDTO();
        mcDisplayDTO.setContactNames(contactNamesMC);
        mcDisplayDTO.setContactEmails(contactEmailsMC);
        mcDisplayDTO.setContactEntitySubType(contactSubTypesMC);
        model.addAttribute("myManagedContacts", mcDisplayDTO);





        ///////////////////////////////////////////////////////////////////////////////////////////////
        // add attorneys pool first name last name
        // and add to model
        ///////////////////////////////////////////////////////////////////////////////////////////////
        boolean isAttorneyOptionSet = false;
        boolean isAttorneyFiling = false;

        if (trademarkInternalID.equals("new")) {

            BaseTrademarkApplication trademarkApplication = new BaseTrademarkApplication();
            //trademarkApplication.setLastViewModel("application/owner/individual/ownerInfo");
            //trademarkApplication.setLastViewModel("application/OwnerStart");

            ////////////////////////////////////////////////////////////////////////////
            // bread crumb and continue application updates
            ////////////////////////////////////////////////////////////////////////////
            trademarkApplication.setLastViewModel("application/attorney/AttorneyStart");
            ArrayList<String> sectionStatus = trademarkApplication.getSectionStatus();
            sectionStatus.set(0,"active");
            trademarkApplication.setSectionStatus(sectionStatus);
            ////////////////////////////////////////////////////////////////////////////



            trademarkApplication.setAttorneySet(false);
            trademarkApplication.setAttorneyFiling(false);


            trademarkApplication.setOwnerEmail(ptoUser.getEmail());

            /////////////////////////////////////////////////////////////////////////////////
            // add a method to PTOUser to just add one application
            /////////////////////////////////////////////////////////////////////////////////
            baseTradeMarkApplicationService.save(trademarkApplication);
            trademarkApplication.setTrademarkName("my_first_trademark");
            trademarkApplication.setApplicationInternalID(UUID.randomUUID().toString());
            counter++;
            trademarkApplication.setTrademarkName("" + counter);
            ptoUser.addApplication(trademarkApplication); // adds to myApplications Collection
            ptoUserService.save(ptoUser);
            model.addAttribute("baseTrademarkApplication", trademarkApplication);

            /////////////////////////////////////////////////////////////////////////////////
            // set empty selected contacts for thymeleaf
            // add emtpy selected list
            /////////////////////////////////////////////////////////////////////////////////
            ArrayList<String> scontactNames = new ArrayList<>();
            ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
            selectedAttorneyDisplayDTO.setContactNames(scontactNames);

            model.addAttribute("selectedAttorneys", selectedAttorneyDisplayDTO);
            model.addAttribute("isAttorneyOptionSet", isAttorneyOptionSet);
            model.addAttribute("isAttorneyFiling", isAttorneyFiling);

            ArrayList<String> selectedContactEmails2 = new ArrayList<>();
            for(Iterator<Lawyer> iter = trademarkApplication.getAvailableLawyers().iterator(); iter.hasNext(); ) {
                Lawyer current = iter.next();
                selectedContactEmails2.add(current.getEmail());
            }
            ContactsDisplayDTO selectedAttorneyDisplayDTO2 = new ContactsDisplayDTO();
            selectedAttorneyDisplayDTO2.setContactEmails(selectedContactEmails2 );
            model.addAttribute("selectedAttorneys",selectedAttorneyDisplayDTO2);
            model.addAttribute("breadCrumbStatus", trademarkApplication.getSectionStatus());


        } else {
            BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

            ArrayList<String> selectedContactNames = new ArrayList<>();
            for (Iterator<Lawyer> iter = baseTrademarkApplication.getAvailableLawyers().iterator(); iter.hasNext(); ) {
                Lawyer current = iter.next();
                selectedContactNames.add(current.getFirstName() + " " + current.getLastName());
            }
            ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
            selectedAttorneyDisplayDTO.setContactNames(selectedContactNames);
            model.addAttribute("selectedAttorneys", selectedAttorneyDisplayDTO);

            model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
            isAttorneyOptionSet = baseTrademarkApplication.isAttorneySet();
            isAttorneyFiling = baseTrademarkApplication.isAttorneyFiling();
            model.addAttribute("isAttorneyOptionSet", isAttorneyOptionSet);
            model.addAttribute("isAttorneyFiling", isAttorneyFiling);

            ArrayList<String> selectedContactEmails2 = new ArrayList<>();
            for(Iterator<Lawyer> iter = baseTrademarkApplication.getAvailableLawyers().iterator(); iter.hasNext(); ) {
                Lawyer current = iter.next();
                selectedContactEmails2.add(current.getEmail());
            }
            ContactsDisplayDTO selectedAttorneyDisplayDTO2 = new ContactsDisplayDTO();
            selectedAttorneyDisplayDTO2.setContactEmails(selectedContactEmails2 );
            model.addAttribute("selectedAttorneys",selectedAttorneyDisplayDTO2);

            model.addAttribute("breadCrumbStatus",baseTrademarkApplication.getSectionStatus());

        }
        NewAttorneyContactFormDTO attorneyContactFormDTO = new NewAttorneyContactFormDTO();
        model.addAttribute("addNewAttorneyContactFormDTO", attorneyContactFormDTO);
        // add any selected attorneys to model as selectedContacts


        model.addAttribute("hostBean", hostBean);
        return "application/attorney/AttorneyStart";


    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // controller for AttorneySet page
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @Transactional
    @RequestMapping({"/application/AttorneySet","application/AttorneySet"})
    public String applicationAttorneySet
            (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();


        /////////////////////////////////////////////
        // load my contacts list for thyemleaf
        /////////////////////////////////////////////
        ArrayList<String> contactNames = new ArrayList<>();
        ArrayList<String> contactEmails = new ArrayList<>();
        ArrayList<String> contactFirms = new ArrayList<>();
        Lawyer lawyer = null;

        for(Iterator<Lawyer> iter = ptoUser.getMyLawyers().iterator(); iter.hasNext(); ) {
            lawyer = iter.next();
            contactNames.add(lawyer.getFirstName()+" "+lawyer.getLastName());
            contactEmails.add(lawyer.getEmail());
            contactFirms.add(lawyer.getLawFirmName());

        }
        Collections.reverse(contactNames);
        Collections.reverse(contactEmails);
        Collections.reverse(contactFirms);
        ContactsDisplayDTO contactsDisplayDTO = new ContactsDisplayDTO();
        contactsDisplayDTO.setContactNames(contactNames);
        contactsDisplayDTO.setContactEmails(contactEmails);
        contactsDisplayDTO.setContactFirms(contactFirms);
        model.addAttribute("myContacts", contactsDisplayDTO);
        SelectedContactsDisplayDTO selectedContactsDisplayDTO = new SelectedContactsDisplayDTO();

///////////////////////////////////////////
        // load my contacts list for thyemleaf
        ////////////////////////////////////////////
        ArrayList<String> contactNamesMC = new ArrayList<>();
        ArrayList<String> contactEmailsMC = new ArrayList<>();
        ArrayList<String> contactSubTypesMC = new ArrayList<>();
        ManagedContact managedContact = null;

        for(Iterator<ManagedContact> iter = ptoUser.getMyManagedContacts().iterator(); iter.hasNext(); ) {
            managedContact = iter.next();
            if(managedContact.getContactType() == "attorney") {
                contactNamesMC.add(managedContact.getDisplayName());
                contactEmailsMC.add(managedContact.getEmail());
                contactSubTypesMC.add(managedContact.getContactType());
            }

        }
        Collections.reverse(contactNamesMC);
        Collections.reverse(contactEmailsMC);
        Collections.reverse(contactSubTypesMC);
        ContactsDisplayDTO mcDisplayDTO = new ContactsDisplayDTO();
        mcDisplayDTO.setContactNames(contactNamesMC);
        mcDisplayDTO.setContactEmails(contactEmailsMC);
        mcDisplayDTO.setContactEntitySubType(contactSubTypesMC);
        model.addAttribute("myManagedContacts", mcDisplayDTO);

        // set empty selected contacts for thymeleaf
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Lawyer> iter = baseTrademarkApplication.getAvailableLawyers().iterator(); iter.hasNext(); ) {
            Lawyer current = iter.next();
            selectedContactNames.add(current.getFirstName()+" "+current.getLastName());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactNames(selectedContactNames);
        model.addAttribute("selectedAttorneys",selectedAttorneyDisplayDTO);
        baseTrademarkApplication.setLastViewModel("application/AttorneySet");

        if(trademarkInternalID.equals("new")) {

            BaseTrademarkApplication trademarkApplication = new BaseTrademarkApplication();

            trademarkApplication.setAttorneySet(false);
            trademarkApplication.setAttorneyFiling(false);

            trademarkApplication.setOwnerEmail(ptoUser.getEmail());

            baseTradeMarkApplicationService.save(trademarkApplication);
            trademarkApplication.setTrademarkName("my_first_trademark");
            trademarkApplication.setApplicationInternalID(UUID.randomUUID().toString());
            counter++;
            trademarkApplication.setTrademarkName(""+counter);
            ptoUser.addApplication(trademarkApplication); // adds to myApplications Collection
            ptoUserService.save(ptoUser);
            model.addAttribute("baseTrademarkApplication", trademarkApplication);




            model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);


        }
        else{

            ////////////////////////////////////////////////////////////////////////////
            // existing trade  mark application
            ////////////////////////////////////////////////////////////////////////////
            // loaded baseTradeMarkapplication by internal id and add to model
            ////////////////////////////////////////////////////////////////////////////
            // BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
            // we are already setting this value in the begging for selected contacts rendering
            /////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////
            // add relationship (i.e grey out the correspondant contacts table)
            // we can do this when rendering the contacts table rows ...
            // do a check and insert different HTML for the row
            //////////////////////////////////////////////////////////////////////////

            ////////////////////////////////////////////////////////////////////////////////////////////
            // add selected contacts display info to model
            ////////////////////////////////////////////////////////////////////////////////////////////



            // baseTrademarkApplication.setLastViewModel("application/AttorneyStart");

            model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
            model.addAttribute("lawyerPool", baseTrademarkApplication.getAvailableLawyers());


        }



        model.addAttribute("hostBean", hostBean);
        return "application/attorney/AttorneySet";

    }










    // hopefully just a redirect here, we won't need to add the applicaiton and credentials to the model
    @RequestMapping({"/application/OwnerStart"})
    public String ownerStart (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);

        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        baseTrademarkApplication.setLastViewModel("application/owner/OwnerStart");
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);
        /////////////////////////////////////////////
        // load my contacts list for thyemleaf
        /////////////////////////////////////////////
        ArrayList<String> contactNames = new ArrayList<>();
        ArrayList<String> contactEmails = new ArrayList<>();
        ArrayList<String> contactSubTypes = new ArrayList<>();
        ManagedContact managedContact = null;

        for(Iterator<ManagedContact> iter = ptoUser.getMyManagedContacts().iterator(); iter.hasNext(); ) {
            managedContact = iter.next();
            if(managedContact.getContactType()== "owner"){
                contactNames.add(managedContact.getDisplayName());
                contactEmails.add(managedContact.getEmail());
                contactSubTypes.add(managedContact.getContactType());
            }


        }
        Collections.reverse(contactNames);
        Collections.reverse(contactEmails);
        Collections.reverse(contactSubTypes);
        ContactsDisplayDTO contactsDisplayDTO = new ContactsDisplayDTO();
        contactsDisplayDTO.setContactNames(contactNames);
        contactsDisplayDTO.setContactEmails(contactEmails);
        contactsDisplayDTO.setContactEntitySubType(contactSubTypes);
        model.addAttribute("myManagedContacts", contactsDisplayDTO);


        ///////////////////////////////////////////////////////////////
        // set selected owner contacts ...
        // since there is just one owner ...


        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);


        return "application/owner/OwnerStart";
    }





    // hopefully just a redirect here, we won't need to add the applicaiton and credentials to the model
    @RequestMapping({"/application/owner/Ind/info"})
    public String ownerIndvUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);


        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);

        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);


        return "application/owner/individual/ownerInfo2";
    }


    @RequestMapping({"/application/owner/us/solp"})
    public String ownerSolPUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);

        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);


        return "application/owner/solP/ownerInfo2";
    }

    @RequestMapping({"/application/owner/us/corp"})
    public String ownerCorpUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);
        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);


        return "application/owner/corp/ownerInfo2";
    }

    @RequestMapping({"/application/owner/us/llc"})
    public String ownerLLCUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);
        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);


        return "application/owner/llc/ownerInfo2";
    }

    @RequestMapping({"/application/owner/us/partnership"})
    public String ownerPartnershipUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        partnerDTO partner = new partnerDTO();
        newOwnerContactFormDTO.addPartner(partner);


        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);

        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);

        return "application/owner/partnership/ownerInfo2";
    }


    @RequestMapping({"/application/owner/us/llp"})
    public String ownerLLPUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        partnerDTO partner = new partnerDTO();
        newOwnerContactFormDTO.addPartner(partner);

        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);
        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);

        return "application/owner/llp/ownerInfo";
    }


    @RequestMapping({"/application/owner/us/jointVC"})
    public String ownerJointVCUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        partnerDTO partner = new partnerDTO();
        newOwnerContactFormDTO.addPartner(partner);

        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);
        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);

        return "application/owner/jointVC/ownerInfo";
    }

    @RequestMapping({"/application/owner/us/trust"})
    public String ownerTrustUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        partnerDTO partner = new partnerDTO();
        newOwnerContactFormDTO.addPartner(partner);

        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);
        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);

        return "application/owner/trust/ownerInfo";
    }



    @RequestMapping({"/application/owner/us/estate"})
    public String ownerEstateUSInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        partnerDTO partner = new partnerDTO();
        newOwnerContactFormDTO.addPartner(partner);

        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);
        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);

        return "application/owner/estate/ownerInfo";
    }


    @RequestMapping({"/application/owner/foreign/corp"})
    public String ownerCorpForeignInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        model.addAttribute("hostBean", hostBean);

        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        partnerDTO partner = new partnerDTO();
        newOwnerContactFormDTO.addPartner(partner);

        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);
        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getEmail());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactEmails(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);

        return "application/owner/corp/ownerInfoForeign";
    }











    /////////////////////////////////////////////////////////////////////////////////////////////////
    // controller for AttorneySet page
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @Transactional
    @RequestMapping({"/application/OwnerSetView","application/OwnerSetView"})
    public String applicationOwnerView
    (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        // create a new application and tie it to user then save it to repository
        // create attorneyDTO + to model
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication  baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        baseTrademarkApplication.setLastViewModel("application/owner/OwnerSetView");

        ////////////////////////////////////////////////////////////////////////////////////////////
        // add contacts display info to model
        ////////////////////////////////////////////////////////////////////////////////////////////



        /////////////////////////////////////////////////////////////////////////////////////////////
        // create new check
        /////////////////////////////////////////////////////////////////////////////////////////////
        // check if user has LoadContinueUrl set ...
        // in the web url that triggers, you get that from an iteration of
        // myTradeMarks on the dashboard
        /////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////
        // load my contacts list for thyemleaf
        ////////////////////////////////////////////
        ArrayList<String> contactNamesMC = new ArrayList<>();
        ArrayList<String> contactEmailsMC = new ArrayList<>();
        ArrayList<String> contactSubTypesMC = new ArrayList<>();
        ManagedContact managedContact = null;

        for(Iterator<ManagedContact> iter = ptoUser.getMyManagedContacts().iterator(); iter.hasNext(); ) {
            managedContact = iter.next();
            if(managedContact.getContactType() == "owner"){
                contactNamesMC.add(managedContact.getDisplayName());
                contactEmailsMC.add(managedContact.getEmail());
                contactSubTypesMC.add(managedContact.getContactType());
            }


        }
        Collections.reverse(contactNamesMC);
        Collections.reverse(contactEmailsMC);
        Collections.reverse(contactSubTypesMC);
        ContactsDisplayDTO mcDisplayDTO = new ContactsDisplayDTO();
        mcDisplayDTO.setContactNames(contactNamesMC);
        mcDisplayDTO.setContactEmails(contactEmailsMC);
        mcDisplayDTO.setContactEntitySubType(contactSubTypesMC);
        model.addAttribute("myManagedContacts", mcDisplayDTO);




        ArrayList<String> selectedContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedContactNames.add(current.getFirstName()+" "+current.getLastName());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactNames(selectedContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);




        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);


        model.addAttribute("hostBean", hostBean);
        return "application/owner/OwnerSetView";

    }






    @Transactional
    @RequestMapping(value = "/application/continueApplication", method = RequestMethod.GET)
    public String continueApplication
            (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {

        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        //////////////////////////////////////////////////////////////////////////
        // retrieve trade mark using internal id
        // add trademark to the model
        // and return the the view that trademark object has saved.
        //////////////////////////////////////////////////////////////////////////



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PTOUserService  ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());
        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        //////////////////////////////////////////////////////
        // this is set back to null upon verification check
        //////////////////////////////////////////////////////
        ptoUser.setContinuationURL(trademarkInternalID);
        ptoUserService.save(ptoUser);
        //////////////////////////////////////////////////////
        //continuation = true;

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);


        model.addAttribute("hostBean", hostBean);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // add contacts display info to model
        ////////////////////////////////////////////////////////////////////////////////////////////
        ArrayList<String> contactNames = new ArrayList<>();
        ArrayList<String> contactEmails = new ArrayList<>();
        ArrayList<String> contactFirms = new ArrayList<>();
        Lawyer lawyer = null;

        for(Iterator<Lawyer> iter = ptoUser.getMyLawyers().iterator(); iter.hasNext(); ) {
            lawyer = iter.next();
            contactNames.add(lawyer.getFirstName()+" "+lawyer.getLastName());
            contactEmails.add(lawyer.getEmail());
            contactFirms.add(lawyer.getLawFirmName());
        }
        Collections.reverse(contactNames);
        Collections.reverse(contactEmails);
        Collections.reverse(contactFirms);
        ContactsDisplayDTO contactsDisplayDTO = new ContactsDisplayDTO();
        contactsDisplayDTO.setContactNames(contactNames);
        contactsDisplayDTO.setContactEmails(contactEmails);
        contactsDisplayDTO.setContactFirms(contactFirms);

        model.addAttribute("myContacts", contactsDisplayDTO);

        // add selected attorney info for continuing views

        ArrayList<String> selectedContactNames = new ArrayList<>();
        Lawyer selected_lawyer = null;
        Set<Lawyer> applicationLawyerPool = baseTrademarkApplication.getAvailableLawyers();

        if(applicationLawyerPool != null){
            for(Iterator<Lawyer> iterSelectedContacts = baseTrademarkApplication.getAvailableLawyers().iterator(); iterSelectedContacts.hasNext(); ) {
                selected_lawyer = iterSelectedContacts.next();
                selectedContactNames.add(selected_lawyer.getFirstName()+" "+selected_lawyer.getLastName());
            }
            Collections.reverse(selectedContactNames);
            /////////////////////////////////////////////////////////////////////////////////////////////
            // we need a DTO for passing data to view layer
            /////////////////////////////////////////////////////////////////////////////////////////////
            SelectedContactsDisplayDTO selectedContactsDisplayDTO = new SelectedContactsDisplayDTO();
            selectedContactsDisplayDTO.setSelectedNames(selectedContactNames);
            model.addAttribute("selectedAttorneys", selectedContactsDisplayDTO);
        }
        else{
            // if there are no one in the application available pool ..
            // simply add empty value.

            SelectedContactsDisplayDTO selectedContactsDisplayDTO = new SelectedContactsDisplayDTO();
            selectedContactNames.add("");
            selectedContactsDisplayDTO.setSelectedNames(selectedContactNames);
            model.addAttribute("selectedAttorneys", selectedContactsDisplayDTO);
        }

        model.addAttribute("lawyerPool", baseTrademarkApplication.getAvailableLawyers());
        NewOwnerContactFormDTO newOwnerContactFormDTO = new NewOwnerContactFormDTO();
        model.addAttribute("addNewOwnerContactFormDTO", newOwnerContactFormDTO);

        ArrayList<String> managedContactNames = new ArrayList<>();
        ArrayList<String> managedContactEmails = new ArrayList<>();
        ArrayList<String> managedCcontactSubTypes = new ArrayList<>();
        ManagedContact managedContact = null;

        for(Iterator<ManagedContact> iter = ptoUser.getMyManagedContacts().iterator(); iter.hasNext(); ) {
            managedContact = iter.next();
            managedContactNames.add(managedContact.getDisplayName());
            managedContactEmails.add(managedContact.getEmail());
            managedCcontactSubTypes.add(managedContact.getContactType());

        }
        Collections.reverse(managedContactNames);
        Collections.reverse(managedContactEmails);
        Collections.reverse(managedCcontactSubTypes);
        ContactsDisplayDTO managedContactsDisplayDTO = new ContactsDisplayDTO();
        managedContactsDisplayDTO.setContactNames(managedContactNames);
        managedContactsDisplayDTO.setContactEmails(managedContactEmails);
        managedContactsDisplayDTO.setContactEntitySubType(managedCcontactSubTypes);
        model.addAttribute("myManagedContacts", managedContactsDisplayDTO);




         // add selected owner info for continuing views
        ArrayList<String> selectedOwnerContactNames = new ArrayList<>();
        for(Iterator<Owner> iter = baseTrademarkApplication.getOwners().iterator(); iter.hasNext(); ) {
            Owner current = iter.next();
            selectedOwnerContactNames.add(current.getFirstName()+" "+current.getLastName());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO.setContactNames(selectedOwnerContactNames);
        model.addAttribute("selectedOwners",selectedAttorneyDisplayDTO);

        // add default trade mark persistence
        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }


        ArrayList<String> selectedContactNames2 = new ArrayList<>();
        for(Iterator<Lawyer> iter = baseTrademarkApplication.getAvailableLawyers().iterator(); iter.hasNext(); ) {
            Lawyer current = iter.next();
            selectedContactNames2.add(current.getFirstName()+" "+current.getLastName());
        }
        ContactsDisplayDTO selectedAttorneyDisplayDTO2 = new ContactsDisplayDTO();
        selectedAttorneyDisplayDTO2.setContactNames(selectedContactNames2);
        model.addAttribute("selectedAttorneys",selectedAttorneyDisplayDTO2);


        boolean isAttorneyOptionSet = baseTrademarkApplication.isAttorneySet();
        boolean isAttorneyFiling = baseTrademarkApplication.isAttorneyFiling();
        model.addAttribute("isAttorneyOptionSet", isAttorneyOptionSet);
        model.addAttribute("isAttorneyFiling", isAttorneyFiling);

        NewAttorneyContactFormDTO attorneyContactFormDTO = new NewAttorneyContactFormDTO();
        model.addAttribute("addNewAttorneyContactFormDTO", attorneyContactFormDTO);




        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getClassDescription());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);

        System.out.println("las view model : "+baseTrademarkApplication.getLastViewModel());

        return baseTrademarkApplication.getLastViewModel();

    }




    @RequestMapping({"/application/MarkDetails"})
    public String test (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        baseTrademarkApplication.setLastViewModel("application/mark/MarkDetailsStart");
        ArrayList<String> sectionStatus = baseTrademarkApplication.getSectionStatus();
        sectionStatus.set(0,"done");
        sectionStatus.set(1,"active");
        baseTrademarkApplication.setSectionStatus(sectionStatus);
        //////////////////////////////////////////////////////////////////////////////////////


        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        //return "application/MarkDetailsExamples";
        return "application/mark/MarkDetailsStart";
        //return "registrationConfirm/VerificationEmail";
    }

    @RequestMapping({"/mark/designWithText"})
    public String markUpload (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        //public String markUpload (WebRequest request, Model model) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        baseTrademarkApplication.setLastViewModel("application/mark/MarkDetailsUpload");


        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());
        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }

        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        return "application/mark/MarkDetailsUpload";

    }


    @RequestMapping({"/mark/designWithTextDetails"})
    public String designWithTextDetails (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        //public String markUpload (WebRequest request, Model model) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        baseTrademarkApplication.setLastViewModel("application/mark/MarkDetailsDesignWText");

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }




        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();
        boolean translationFW = baseTrademarkApplication.getTradeMark().isForeignLanguageTranslationWording();
        boolean transliterationFW = baseTrademarkApplication.getTradeMark().isForeignLanguateTransliterationWording();
        boolean containsSignatureName = baseTrademarkApplication.getTradeMark().isContainNamePortaitSignature();

        boolean isName = baseTrademarkApplication.getTradeMark().isName();
        boolean isSignature = baseTrademarkApplication.getTradeMark().isSignature();
        boolean isPortrait =  baseTrademarkApplication.getTradeMark().isPortrait();
        boolean isNPSLivingPerson =  baseTrademarkApplication.getTradeMark().isNPSLivingPerson();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);
        model.addAttribute("translationFW", translationFW);
        model.addAttribute("translitFW", transliterationFW);
        model.addAttribute("containsSignatureName", containsSignatureName );

        model.addAttribute("isName", isName );
        model.addAttribute("isSignature", isSignature );
        model.addAttribute("isPortrait", isPortrait );
        model.addAttribute("isNPSLivingPerson", isNPSLivingPerson );
        return "application/mark/MarkDetailsDesignWText";

    }




    @RequestMapping({"/mark/standard"})
    public String standardCharacterDetails (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        //public String markUpload (WebRequest request, Model model) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        baseTrademarkApplication.setLastViewModel("application/mark/MarkDetailsStandard");
        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }




        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();
        boolean translationFW = baseTrademarkApplication.getTradeMark().isForeignLanguageTranslationWording();
        boolean transliterationFW = baseTrademarkApplication.getTradeMark().isForeignLanguateTransliterationWording();
        boolean containsSignatureName = baseTrademarkApplication.getTradeMark().isContainNamePortaitSignature();

        boolean isName = baseTrademarkApplication.getTradeMark().isName();
        boolean isSignature = baseTrademarkApplication.getTradeMark().isSignature();
        boolean isPortrait =  baseTrademarkApplication.getTradeMark().isPortrait();
        boolean isNPSLivingPerson =  baseTrademarkApplication.getTradeMark().isNPSLivingPerson();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);
        model.addAttribute("translationFW", translationFW);
        model.addAttribute("translitFW", transliterationFW);
        model.addAttribute("containsSignatureName", containsSignatureName );

        model.addAttribute("isName", isName );
        model.addAttribute("isSignature", isSignature );
        model.addAttribute("isPortrait", isPortrait );
        model.addAttribute("isNPSLivingPerson", isNPSLivingPerson );
        return "application/mark/MarkDetailsStandard";

    }



    @RequestMapping({"/application/MarkExamples"})
    public String markExamples( WebRequest request, Model model, @RequestParam("anchorID") String anchorID){
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);


         //int cutOff =  anchorID.indexOf("#");
         //anchorID = anchorID.substring(0, cutOff-1);

        model.addAttribute("anchorID", anchorID );

        return "application/mark/MarkDetailsExamples";

        //return "registrationConfirm/VerificationEmail";
    }




    @RequestMapping({"/mark/designWithTextDisclaimer"})
    public String designWithTextDisclaimer (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        //public String markUpload (WebRequest request, Model model) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        //String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        baseTrademarkApplication.setLastViewModel("application/mark/MarkDetailsDesignWTextDisclaimer");


        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        boolean activeDisclaimer = baseTrademarkApplication.getTradeMark().isActvieDisclaimer();
        boolean priorRegistration = baseTrademarkApplication.getTradeMark().isPriorRegistratoin();
        boolean markHasMeaning = baseTrademarkApplication.getTradeMark().isMarkWordingHasSignifigance();

        model.addAttribute("activeDisclaimer", activeDisclaimer);
        model.addAttribute("priorRegistration", priorRegistration);
        model.addAttribute("markHasMeaning", markHasMeaning);



        return "application/mark/MarkDetailsDesignWTextDisclaimer";

    }




    @RequestMapping({"/application/goodsAndServicesStart"})
    public String goodsAndServicesStart (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        baseTrademarkApplication.setLastViewModel("application/goods_services/GoodsServicesStart");
        ArrayList<String> sectionStatus = baseTrademarkApplication.getSectionStatus();
        sectionStatus.set(0,"done");
        sectionStatus.set(1,"done");
        sectionStatus.set(2,"active");
        baseTrademarkApplication.setSectionStatus(sectionStatus);
        //////////////////////////////////////////////////////////////////////////////////////

        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();
        boolean searchDB = baseTrademarkApplication.isSearchExistingGSdatabase();



        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);
        model.addAttribute("searchDB", searchDB);

        //return "application/MarkDetailsExamples";
        return "application/goods_services/GoodsServicesStart";
        //return "registrationConfirm/VerificationEmail";
    }



    @RequestMapping({"/application/goodsAndServicesSelect"})
    public String goodsAndServicesSelect (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        baseTrademarkApplication.setLastViewModel("application/goods_services/GoodsServicesSelect");

        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();


        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getInternalID());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);

        int numberOfSelectedGS = baseTrademarkApplication.getGoodAndServices().size();
        boolean isGSempty = true;
        if(numberOfSelectedGS > 0){
            isGSempty = false;
        }
        model.addAttribute("isGSempty", isGSempty);



        //return "application/MarkDetailsExamples";
        return "application/goods_services/GoodsServicesSelect";
        //return "registrationConfirm/VerificationEmail";
    }




    @RequestMapping({"/application/goodsAndServicesReview"})
    public String goodsAndServicesReview (WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);
        baseTrademarkApplication.setLastViewModel("application/goods_services/GoodsServicesReview");

        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSIDs = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSIDs.add(current.getInternalID());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSIDs);
        model.addAttribute("selectedGoods_Services",selectedDescription);




        //return "application/MarkDetailsExamples";
        return "application/goods_services/GoodsServicesReview";
        //return "registrationConfirm/VerificationEmail";
    }





    @RequestMapping({"/application/filingBasisStart"})
    public String filingBasisStart(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        baseTrademarkApplication.setLastViewModel("application/filing_basis/FilingBasisStart");
        ArrayList<String> sectionStatus = baseTrademarkApplication.getSectionStatus();
        sectionStatus.set(0,"done");
        sectionStatus.set(1,"done");
        sectionStatus.set(2,"done");
        sectionStatus.set(3,"active");
        baseTrademarkApplication.setSectionStatus(sectionStatus);
        //////////////////////////////////////////////////////////////////////////////////////

        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getClassDescription());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);





        return "application/filing_basis/FilingBasisStart";

    }



    @RequestMapping({"/application/filingBasisUpload"})
    public String filingBasisUpload(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        baseTrademarkApplication.setLastViewModel("application/filing_basis/FilingBasisUpload");

        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getClassDescription());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);





        return "application/filing_basis/FilingBasisUpload";

    }


    @RequestMapping({"/application/inUseFilingBasisUpload"})
    public String filingBasisUploadMarkInUse(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info


        System.out.println("999999999999999999999999999999999999999999999999999999");

        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        baseTrademarkApplication.setLastViewModel("application/filing_basis/FilingBasisInuseUpload");

        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getClassDescription());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);





        return "application/filing_basis/FilingBasisInuseUpload";

    }



    @RequestMapping({"/application/additionalInfoUpload"})
    public String additionalInfoUpload(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info




        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        baseTrademarkApplication.setLastViewModel("application/additional/additionalInfo");
        ArrayList<String> sectionStatus = baseTrademarkApplication.getSectionStatus();
        sectionStatus.set(0,"done");
        sectionStatus.set(1,"done");
        sectionStatus.set(2,"done");
        sectionStatus.set(3,"done");
        sectionStatus.set(4,"active");
        baseTrademarkApplication.setSectionStatus(sectionStatus);
        //////////////////////////////////////////////////////////////////////////////////////


        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getClassDescription());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);





        return "application/additional/additionalInfo";

    }



    @RequestMapping({"/application/confirmInfo"})
    public String confirmApplicationInfo(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info


        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        //////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////
        baseTrademarkApplication.setLastViewModel("application/confirm/ConfirmApplicationInfo");
        ArrayList<String> sectionStatus = baseTrademarkApplication.getSectionStatus();
        sectionStatus.set(0,"done");
        sectionStatus.set(1,"done");
        sectionStatus.set(2,"done");
        sectionStatus.set(3,"done");
        sectionStatus.set(4,"done");
        sectionStatus.set(5,"active");
        baseTrademarkApplication.setSectionStatus(sectionStatus);
        //////////////////////////////////////////////////////////////////////////////////////


        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);

        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getClassDescription());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);





        return "application/confirm/ConfirmApplicationInfo";

    }


    @RequestMapping({"/application/confirmPayment"})
    public String confirmApplicationPayment(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info


        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);

        baseTrademarkApplication.setLastViewModel("application/confirm/ConfirmPayment");


        if(baseTrademarkApplication.getTradeMark() == null){
            TradeMark tradeMark = new TradeMark();
            tradeMark.setTrademarkDesignType("");
            baseTrademarkApplication.setTradeMark(tradeMark);
            baseTradeMarkApplicationService.save(baseTrademarkApplication);

        }


        if( baseTrademarkApplication.getTradeMark() != null) {
            model.addAttribute("markImagePath", baseTrademarkApplication.getTradeMark().getTrademarkImagePath());
            model.addAttribute("markImagePathBW",baseTrademarkApplication.getTradeMark().getTrademarkBWImagePath());

        }
        else{
            model.addAttribute("markImagePath","");

            model.addAttribute("markImagePathBW","");


        }
        model.addAttribute("baseTrademarkApplication", baseTrademarkApplication);
        boolean colorClaim = baseTrademarkApplication.getTradeMark().isMarkColorClaim();
        boolean acceptBW = baseTrademarkApplication.getTradeMark().isMarkColorClaimBW();

        model.addAttribute("markColorClaim", colorClaim);
        model.addAttribute("markColorClaimBW", acceptBW);

        ArrayList<String> selectedGSDescrption = new ArrayList<>();
        for(Iterator<GoodAndService> iter = baseTrademarkApplication.getGoodAndServices().iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();
            selectedGSDescrption.add(current.getClassDescription());
        }
        ContactsDisplayDTO selectedDescription = new ContactsDisplayDTO();
        selectedDescription.setContactNames(selectedGSDescrption);
        model.addAttribute("selectedGoods_Services",selectedDescription);





        return "application/confirm/ConfirmPayment";

    }

    @RequestMapping({"/application/success"})
    public String applicaitonSucces(WebRequest request, Model model, @RequestParam("trademarkID") String trademarkInternalID) {
        // get owner info


        // get email and get PTOUser object from repository
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PTOUserService ptoUserService = serviceBeanFactory.getPTOUserService();
        PTOUser ptoUser = ptoUserService.findByEmail(authentication.getName());

        UserCredentialsService userCredentialsService = serviceBeanFactory.getUserCredentialsService();
        UserCredentials credentials = userCredentialsService.findByEmail(authentication.getName());

        model.addAttribute("user", ptoUser);
        model.addAttribute("account",credentials);
        model.addAttribute("hostBean", hostBean);
        String applcationLookupID = trademarkInternalID;
        BaseTradeMarkApplicationService baseTradeMarkApplicationService = serviceBeanFactory.getBaseTradeMarkApplicationService();
        BaseTrademarkApplication baseTrademarkApplication = baseTradeMarkApplicationService.findByInternalID(trademarkInternalID);


        // generate pdf file and add it to model
        // update application status
        baseTrademarkApplication.setLastViewModel("application/success/index");
        ArrayList<String> sectionStatus = baseTrademarkApplication.getSectionStatus();
        sectionStatus.set(0,"done");
        sectionStatus.set(1,"done");
        sectionStatus.set(2,"done");
        sectionStatus.set(3,"done");
        sectionStatus.set(4,"done");
        sectionStatus.set(5,"done");
        baseTrademarkApplication.setSectionStatus(sectionStatus);






        return "application/success/index";

    }


}
