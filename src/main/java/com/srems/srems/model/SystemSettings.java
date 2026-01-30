package com.srems.srems.model;

import jakarta.persistence.*;

@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean autoApprove;
    private boolean docRequired;
    private boolean notifyAdmin;

    private double peakLimit;
    private int highUsageAlert;
    private int retentionMonths;

    private String adminEmail;
    private String securityPhone;

    private boolean emailOnOverload;
    private boolean smsOnRegister;

    private boolean synced = true;

    /* ===== GETTERS & SETTERS ===== */

    public boolean isAutoApprove() { return autoApprove; }
    public void setAutoApprove(boolean autoApprove) { this.autoApprove = autoApprove; }

    public boolean isDocRequired() { return docRequired; }
    public void setDocRequired(boolean docRequired) { this.docRequired = docRequired; }

    public boolean isNotifyAdmin() { return notifyAdmin; }
    public void setNotifyAdmin(boolean notifyAdmin) { this.notifyAdmin = notifyAdmin; }

    public double getPeakLimit() { return peakLimit; }
    public void setPeakLimit(double peakLimit) { this.peakLimit = peakLimit; }

    public int getHighUsageAlert() { return highUsageAlert; }
    public void setHighUsageAlert(int highUsageAlert) { this.highUsageAlert = highUsageAlert; }

    public int getRetentionMonths() { return retentionMonths; }
    public void setRetentionMonths(int retentionMonths) { this.retentionMonths = retentionMonths; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getSecurityPhone() { return securityPhone; }
    public void setSecurityPhone(String securityPhone) { this.securityPhone = securityPhone; }

    public boolean isEmailOnOverload() { return emailOnOverload; }
    public void setEmailOnOverload(boolean emailOnOverload) { this.emailOnOverload = emailOnOverload; }

    public boolean isSmsOnRegister() { return smsOnRegister; }
    public void setSmsOnRegister(boolean smsOnRegister) { this.smsOnRegister = smsOnRegister; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}