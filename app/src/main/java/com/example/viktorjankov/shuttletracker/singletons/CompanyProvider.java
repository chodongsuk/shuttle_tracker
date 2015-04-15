package com.example.viktorjankov.shuttletracker.singletons;

import com.example.viktorjankov.shuttletracker.model.Company;

public class CompanyProvider {
    private static Company mCompany;

    public static Company getCompany() {
        return mCompany;
    }

    public static void setCompany(Company company) {
        mCompany = company;
    }

}
