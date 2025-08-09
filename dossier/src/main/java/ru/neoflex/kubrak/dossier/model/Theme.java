package ru.neoflex.kubrak.dossier.model;

public enum Theme {

    FINISH_REGISTRATION("Finish registration"),
    CREATE_DOCUMENTS("Create documents"),
    SEND_DOCUMENTS("Your loan documents"),
    SEND_SES("Sign documents with SES code"),
    CREDIT_ISSUED("Credit issued"),
    STATEMENT_DENIED("Statement denied");

    private final String theme;

    Theme(String theme) {
        this.theme = theme;
    }

    public String toString() {
        return theme;
    }
}
