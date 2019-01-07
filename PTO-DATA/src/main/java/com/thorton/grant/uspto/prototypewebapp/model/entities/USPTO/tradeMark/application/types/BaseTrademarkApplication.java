package com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.application.types;


import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.application.actions.OfficeActions;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.application.participants.Lawyer;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.application.participants.Owner;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.assets.GoodAndService;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.tradeMark.assets.TradeMark;
import com.thorton.grant.uspto.prototypewebapp.model.entities.USPTO.user.PTOUser;
import com.thorton.grant.uspto.prototypewebapp.model.entities.base.BaseEntity;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.*;


@Entity
public class BaseTrademarkApplication  {

    // need to have an owner

    // could have an lawyer as well

    public BaseTrademarkApplication() {
        availableLawyers = new HashSet<>();
        actions = new HashSet<>();
        owners = new HashSet<>();
        goodAndServices = new HashSet<>();
        GoodsAndSevicesMap = new TreeMap<>();
    }

    ////////////////////////////////////////////////////////
    // stage 1 save point flags
    ////////////////////////////////////////////////////////
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String trademarkName;
    private String applicationInternalID;
    private boolean isAttorneySet = false;
    private boolean isAttorneyFiling;
    private boolean isForeignEnityFiling;
    private String currentStage;
    private String lastViewModel;

    private String ownerEmail;

    public String getOwnerEmail() {
        return ownerEmail;
    }


    ////////////////////////////////////////////////////////
    // modeling
    ////////////////////////////////////////////////////////
    //  parent entity
    ////////////////////////////////////////////////////////
    @ManyToOne
    private PTOUser ptoUser;

    ////////////////////////////////////////////////////////
    // sub ordinate objects
    ////////////////////////////////////////////////////////
    // set these details in stage
    // lawyer is the subordinate object here

    /////////////////////////////////////////////////////////////////////
    // stage 1
    /////////////////////////////////////////////////////////////////////

    @OneToOne(cascade = CascadeType.ALL)
    @Nullable
    private Lawyer primaryLawyer;

    @OneToMany(cascade = CascadeType.ALL)
    @Nullable
    private Set<Lawyer> availableLawyers;


    // can be a lawyer or owner ???

    @OneToMany(cascade = CascadeType.ALL)
    @Nullable
    private Set<Owner> owners; // default owner   PTO user




    /////////////////////////////////////////////////////////////////////
    // stage 2
    /////////////////////////////////////////////////////////////////////
    @OneToOne(cascade = CascadeType.ALL)
    @Nullable
    private TradeMark tradeMark;


    @OneToMany(cascade =  CascadeType.ALL)
    @Nullable
    private Set<OfficeActions> actions;
    ////////////////////////////////////////////////////////


    private String ownerType;
    private String ownerSubType;
    private String attorneyCollapseID;
    private boolean searchExistingGSdatabase = false;


    ////////////////////////////////////////////////////////
    // one class of Good and service // old code
    // leave it here for now until the new code works
    ////////////////////////////////////////////////////////
    @OneToMany(cascade = CascadeType.ALL)
    @Nullable
    private Set<GoodAndService> goodAndServices; // default owner   PTO user
    ////////////////////////////////////////////////////////
    // new data structure to store all of the goods and services
    // key: classNumber
    // object HashSet of GoodAndService
    ////////////////////////////////////////////////////////

    private TreeMap<String, HashSet<GoodAndService>> GoodsAndSevicesMap;
    ////////////////////////////////////////////////////////
    // an list of ids that corresponds to the TreeMap of GS hash set sorted by TreeMap key (classNumber)/ and also sorted
    // within each GoodAndServices set
    ////////////////////////////////////////////////////////
    private ArrayList<String> GoodsAndServicesIDListView;





    public boolean isAttorneyPoolEmpty() {


        boolean status = false;

        if (availableLawyers.size()== 0) {
            status = true;
        }

        return  status;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerSubType() {
        return ownerSubType;
    }

    public void setOwnerSubType(String ownerSubType) {
        this.ownerSubType = ownerSubType;
    }

    public PTOUser getPtoUser() {
        return ptoUser;
    }

    public void setPtoUser(PTOUser ptoUser) {
        this.ptoUser = ptoUser;
    }

    public Lawyer getPrimaryLawyer() {
        return primaryLawyer;
    }

    public void setPrimaryLawyer(@Nullable Lawyer primaryLawyer) {
        this.primaryLawyer = primaryLawyer;
    }


    public Set<Lawyer> getAvailableLawyers() {
        return availableLawyers;
    }

    public void setAvailableLawyers(@Nullable Set<Lawyer> availableLawyers) {

        this.availableLawyers = availableLawyers;

    }

    public void addAvailableLawyer(Lawyer lawyer){
        availableLawyers.add(lawyer);

    }
    public void removeAvailableLawyer(Lawyer lawyer){
        availableLawyers.remove(lawyer);

    }
    public Lawyer findContactByEmail(String email){
        Lawyer lawyer = null;
        for(Iterator<Lawyer> iter = availableLawyers.iterator(); iter.hasNext(); ) {
             Lawyer current = iter.next();

            if(current.getEmail().equals(email)){
                lawyer = current;
            }
        }
        return lawyer;
    }

    public Lawyer findAttorneyContactByDisplayName(String name){
        Lawyer lawyer = null;
        for(Iterator<Lawyer> iter = availableLawyers.iterator(); iter.hasNext(); ) {
            Lawyer current = iter.next();

            if((current.getFirstName()+" "+current.getLastName()).equals(name)){
                lawyer = current;
            }
        }
        return lawyer;
    }


    @Nullable
    public Set<Owner> getOwners() {
        return owners;
    }

    public void setOwners(@Nullable Set<Owner> owners) {
        this.owners = owners;
    }

    public Owner addOwner(Owner newOwner){

        this.owners.add(newOwner);
        return newOwner;

    }

    public void removeOwner(Owner owner){
        this.owners.remove(owner);
    }

    public Owner findOwnerByEmail(String email){
        Owner owner = null;
        for(Iterator<Owner> iter = owners.iterator(); iter.hasNext(); ) {
            Owner current = iter.next();

            if(current.getEmail().equals(email)){
                owner = current;
            }
        }
        return owner;
    }

    public Owner findOwnerByDisplayName(String name){
        Owner owner = null;
        for(Iterator<Owner> iter = owners.iterator(); iter.hasNext(); ) {
            Owner current = iter.next();

            if(current.getOwnerDisplayname().equals(name)){
                owner = current;
            }
        }
        return owner;
    }


     public boolean isOwnerSet(){

        boolean ownerSet = false;

        if(owners.size() > 0){
            ownerSet = true;
        }
        return ownerSet;
     }








    public boolean isAttorneyFiling() {
        return isAttorneyFiling;
    }

    public void setAttorneyFiling(boolean attorneyFiling) {
        isAttorneyFiling = attorneyFiling;
    }

    public boolean isForeignEnityFiling() {
        return isForeignEnityFiling;
    }

    public void setForeignEnityFiling(boolean foreignEnityFiling) {
        isForeignEnityFiling = foreignEnityFiling;
    }


    public TradeMark getTradeMark() {
        return tradeMark;
    }

    public void setTradeMark(@Nullable TradeMark tradeMark) {
        this.tradeMark = tradeMark;
    }


    public Set<OfficeActions> getActions() {
        return actions;
    }

    public void setActions(@Nullable Set<OfficeActions> actions) {
        this.actions = actions;
    }



    public Long getId() {
        return id;
    }

    public void copyAvailableLawyers(Set<Lawyer> availableLawyers){

        for(Iterator<Lawyer> iter = availableLawyers.iterator(); iter.hasNext(); ) {
           this.availableLawyers.add(new Lawyer( iter.next() ));
        }

    }


    public String getTrademarkName() {
        return trademarkName;
    }

    public void setTrademarkName(String trademarkName) {
        this.trademarkName = trademarkName;
    }

    public String getApplicationInternalID() {
        return applicationInternalID;
    }

    public void setApplicationInternalID(String applicationInternalID) {
        this.applicationInternalID = applicationInternalID;
    }

    public boolean isAttorneySet() {
        return isAttorneySet;
    }

    public void setAttorneySet(boolean attorneySet) {
        isAttorneySet = attorneySet;
    }


    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public String getLastViewModel() {
        return lastViewModel;
    }

    public void setLastViewModel(String lastViewModel) {
        this.lastViewModel = lastViewModel;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getAttorneyCollapseID() {
        return attorneyCollapseID;
    }

    public void setAttorneyCollapseID(String attorneyCollapseID) {
        this.attorneyCollapseID = attorneyCollapseID;
    }


    // goods and services
    @Nullable
    public ArrayList<GoodAndService> getGoodAndServicesList() {

        // go through the map, for each hashset,  add each good and services to the hash set that is returned

        Set<GoodAndService> sortedGoodsAndServices = new HashSet<>();

        // determine how many unique keys do we have

        Set<Integer> uniqeClassNumber = getUniqueClassNumberforGS();

        ArrayList<GoodAndService> returnGoodsServicesList = new ArrayList<>();

        // for each unique key, after its sorted
        // sort uniqeIDs
        List<Integer> sortedUniqeClassNumbers = new ArrayList<>(uniqeClassNumber);
        Collections.sort(sortedUniqeClassNumbers);
        System.out.println("number of unique class numbers found : "+sortedUniqeClassNumbers.size());


        for(int a=0; a < sortedUniqeClassNumbers.size(); a++){
            System.out.println("processing class number : "+sortedUniqeClassNumbers.get(a));

            ArrayList<GoodAndService> sortedCategory = new ArrayList<>();

            for(Iterator<GoodAndService> iter = goodAndServices.iterator(); iter.hasNext(); ) {
                GoodAndService current = iter.next();
                if(sortedUniqeClassNumbers.get(a) == Integer.getInteger(current.getClassNumber())){

                    System.out.println("adding GS with description" +current.getClassDescription());
                    System.out.println("GS added has class number "+current.getClassNumber());
                    sortedCategory.add(current);
                }


            }
            // sort sorted category and add it to return value

            Collections.sort(sortedCategory, new CustomComparator());

             for(int b =0; b < sortedCategory.size(); b++){
                 returnGoodsServicesList.add(sortedCategory.get(b));
             }



        }
        // go through the main gs list and find GS that matches this key

        // for each id unique id,
        //   create an arrayList
             // for each GS that matches this unique Category
             // add to the array list

        // // end for loop
        // sort array list
        // and add to return hash set




        return returnGoodsServicesList;
    }

    @Nullable
    public Set<GoodAndService> getGoodAndServices() {
        return goodAndServices;
    }

    public void setGoodAndServices(@Nullable Set<GoodAndService> goodAndServices) {
        this.goodAndServices = goodAndServices;
    }

    public void addGoodAndService(GoodAndService goodAndService){
        goodAndServices.add(goodAndService);
    }
    public void removeGoodAndService(GoodAndService goodAndService){
        goodAndServices.remove(goodAndService);
    }

    public boolean isSearchExistingGSdatabase() {
        return searchExistingGSdatabase;
    }

    public void setSearchExistingGSdatabase(boolean searchExistingGSdatabase) {
        this.searchExistingGSdatabase = searchExistingGSdatabase;
    }

    public GoodAndService findGSbyDescription(String description){
        GoodAndService goodAndService = null;
        for(Iterator<GoodAndService> iter = goodAndServices.iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();

            if(current.getClassDescription().equals(description)){
                goodAndService = current;
            }
        }
        return goodAndService;
    }


    public GoodAndService findGSbyInternalID(String internalID){
        GoodAndService goodAndService = null;
        for(Iterator<GoodAndService> iter = goodAndServices.iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();

            if(current.getInternalID().equals(internalID)){
                goodAndService = current;
            }
        }
        return goodAndService;
    }

    @Nullable
    public TreeMap<String, HashSet<GoodAndService>> getGoodsAndSevicesMap() {
        return GoodsAndSevicesMap;
    }

    public void setGoodsAndSevicesMap(@Nullable TreeMap<String, HashSet<GoodAndService>> goodsAndSevicesMap) {
        GoodsAndSevicesMap = goodsAndSevicesMap;
    }

    public ArrayList<String> getGoodsAndServicesIDListView() {
        return GoodsAndServicesIDListView;
    }

    public void setGoodsAndServicesIDListView(ArrayList<String> goodsAndServicesIDListView) {
        GoodsAndServicesIDListView = goodsAndServicesIDListView;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTrademarkApplication that = (BaseTrademarkApplication) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(trademarkName, that.trademarkName) &&
                Objects.equals(applicationInternalID, that.applicationInternalID) &&
                Objects.equals(ownerEmail, that.ownerEmail) &&
                Objects.equals(availableLawyers, that.availableLawyers) &&
                Objects.equals(ownerType, that.ownerType) &&
                Objects.equals(ownerSubType, that.ownerSubType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trademarkName, applicationInternalID, ownerEmail, availableLawyers, ownerType, ownerSubType);
    }

    @Override
    public String toString() {
        return "BaseTrademarkApplication{" +
                "id=" + id +
                ", trademarkName='" + trademarkName + '\'' +
                ", applicationInternalID='" + applicationInternalID + '\'' +
                ", isAttorneySet=" + isAttorneySet +
                ", isAttorneyFiling=" + isAttorneyFiling +
                ", isForeignEnityFiling=" + isForeignEnityFiling +
                ", currentStage='" + currentStage + '\'' +
                ", lastViewModel='" + lastViewModel + '\'' +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", ptoUser=" + ptoUser +
                ", primaryLawyer=" + primaryLawyer +
                ", availableLawyers=" + availableLawyers +
                ", owners=" + owners +
                ", tradeMark=" + tradeMark +
                ", actions=" + actions +
                ", ownerType='" + ownerType + '\'' +
                ", ownerSubType='" + ownerSubType + '\'' +
                '}';
    }

    public Set<Integer> getUniqueClassNumberforGS(){

       Set<Integer> uniqeIDS = new HashSet<>();

        for(Iterator<GoodAndService> iter = goodAndServices.iterator(); iter.hasNext(); ) {
            GoodAndService current = iter.next();


               uniqeIDS.add(Integer.getInteger(current.getClassNumber()));

        }


        return uniqeIDS;

    }

    public class CustomComparator implements Comparator<GoodAndService> {
        @Override
        public int compare(GoodAndService o1, GoodAndService o2) {

            System.out.println("comparing string 1 : "+o1.getClassDescription()+" | to String 2 : "+o2.getClassDescription());
            System.out.println("compare results : "+o1.getClassDescription().compareTo(o2.getClassDescription()));
            return o1.getClassDescription().compareTo(o2.getClassDescription());
        }
    }
}
