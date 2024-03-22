package com.osinTechInnovation.ogrdapp.model;

public class QRModel {
    private String adminId;
    private String QRCode;
    private int delay;

    public QRModel() {
    }

    public QRModel(String adminId, String qrCodeText, int qrDelay) {
        this.adminId = adminId;
        QRCode = qrCodeText;
        this.delay = qrDelay;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getQRCode() {
        return QRCode;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
