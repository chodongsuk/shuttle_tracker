package com.viktorjankov.shuttletracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Company implements Serializable {

    private String companyName;
    private String companyCode;
    private List<DestinationLocation> destinationList;

    public Company() {

    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public List<DestinationLocation> getDestinationList() {
        return destinationList;
    }

    public void setDestinationList(List<DestinationLocation> destinationList) {
        this.destinationList = destinationList;
    }

    public void addDestinationLocation(DestinationLocation destinationLocation) {
        if (destinationList != null) {
            destinationList.add(destinationLocation);
        }
        else {
            destinationList = new ArrayList<DestinationLocation>();
            destinationList.add(destinationLocation);
        }
    }

    public String toString() {
        return "\n" +
                "Company name: " + companyName + "\n" +
                "Company code: " + companyCode + "\n" +
                "Destinations: " + destinationList.size();
    }
}
