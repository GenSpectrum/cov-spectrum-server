package ch.ethz.vm.entity;


public class DistributionByAgeGroup {

    private final String ageGroup;

    private final int count;

    private final double proportion;


    public DistributionByAgeGroup(String ageGroup, int count, double proportion) {
        this.ageGroup = ageGroup;
        this.count = count;
        this.proportion = proportion;
    }


    public String getAgeGroup() {
        return ageGroup;
    }


    public int getCount() {
        return count;
    }


    public double getProportion() {
        return proportion;
    }
}
