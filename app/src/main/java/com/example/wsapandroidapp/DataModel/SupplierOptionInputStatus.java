package com.example.wsapandroidapp.DataModel;

public class SupplierOptionInputStatus {

    boolean validFirstOption, validSecondOption, validThirdOption;

    public SupplierOptionInputStatus() {
    }

    public SupplierOptionInputStatus(boolean validFirstOption, boolean validSecondOption, boolean validThirdOption) {
        this.validFirstOption = validFirstOption;
        this.validSecondOption = validSecondOption;
        this.validThirdOption = validThirdOption;
    }

    public boolean isValidFirstOption() {
        return validFirstOption;
    }

    public boolean isValidSecondOption() {
        return validSecondOption;
    }

    public boolean isValidThirdOption() {
        return validThirdOption;
    }

    public boolean isValidAll() {
        return validFirstOption && validSecondOption && validThirdOption;
    }

    public void setValidFirstOption(boolean validFirstOption) {
        this.validFirstOption = validFirstOption;
    }

    public void setValidSecondOption(boolean validSecondOption) {
        this.validSecondOption = validSecondOption;
    }

    public void setValidThirdOption(boolean validThirdOption) {
        this.validThirdOption = validThirdOption;
    }
}
