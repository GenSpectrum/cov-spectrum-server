package ch.ethz.covspectrum.entity.core;


public class SamplePrivateMetadata {

    private final String country;

    private final String division;

    private final String location;

    private final String zipCode;

    private final String host;

    private final int age;

    private final String sex;

    private final String submittingLab;

    private final String originatingLab;


    public SamplePrivateMetadata(
            String country,
            String division,
            String location,
            String zipCode,
            String host,
            int age,
            String sex,
            String submittingLab,
            String originatingLab
    ) {
        this.country = country;
        this.division = division;
        this.location = location;
        this.zipCode = zipCode;
        this.host = host;
        this.age = age;
        this.sex = sex;
        this.submittingLab = submittingLab;
        this.originatingLab = originatingLab;
    }


    public String getCountry() {
        return country;
    }


    public String getDivision() {
        return division;
    }


    public String getLocation() {
        return location;
    }


    public String getZipCode() {
        return zipCode;
    }


    public String getHost() {
        return host;
    }


    public int getAge() {
        return age;
    }


    public String getSex() {
        return sex;
    }


    public String getSubmittingLab() {
        return submittingLab;
    }


    public String getOriginatingLab() {
        return originatingLab;
    }
}
