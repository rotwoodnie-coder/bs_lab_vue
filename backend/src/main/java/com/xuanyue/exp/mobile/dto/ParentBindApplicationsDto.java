package com.xuanyue.exp.mobile.dto;



import java.util.List;



public class ParentBindApplicationsDto {



    private String accountStatus;

    private String accountStatusLabel;

    private List<ParentBindResultDto> applications;



    public String getAccountStatus() { return accountStatus; }

    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getAccountStatusLabel() { return accountStatusLabel; }

    public void setAccountStatusLabel(String accountStatusLabel) { this.accountStatusLabel = accountStatusLabel; }

    public List<ParentBindResultDto> getApplications() { return applications; }

    public void setApplications(List<ParentBindResultDto> applications) { this.applications = applications; }

}


