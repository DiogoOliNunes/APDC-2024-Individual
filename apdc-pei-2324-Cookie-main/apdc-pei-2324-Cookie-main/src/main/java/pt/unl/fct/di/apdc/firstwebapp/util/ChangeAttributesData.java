package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAttributesData {

    public String attribute;
    public String newAttribute;
    public String username;
    public ChangeAttributesData() {

    }

    public ChangeAttributesData(String attribute, String newAttribute, String username) {
        this.attribute = attribute;
        this.newAttribute = newAttribute;
        this.username = username;
    }

}
