package lipid;
import adduct.Adduct;
import adduct.AdductList;
import java.util.*;
import static adduct.Adduct.*;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private String adduct;
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;
    private Ionization ionization;
    private final int TOLERANCE_PMM = 10;

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, Ionization ionization) {
        this(lipid, mz, intensity, retentionTime, ionization, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, Ionization ionization, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.groupedSignals = orderSignals(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
        this.ionization = ionization;
        //this.adduct = identifyAdduct(this.groupedSignals);
    }

    public Set<Peak> orderSignals(Set<Peak> signals) {
        Set<Peak> orderedSet = new TreeSet<>(Comparator.comparingDouble(Peak::getMz));
        orderedSet.addAll(signals);
        return orderedSet;
    }

    public String identifyAdduct() {
        Map<String, Double> adductMap = ionization == Ionization.POSITIVE ? AdductList.MAPMZPOSITIVEADDUCTS : AdductList.MAPMZNEGATIVEADDUCTS;

        for (String adduct1 : adductMap.keySet()) {
            for (String adduct2 : adductMap.keySet()) {
                if (adduct1.equals(adduct2)) continue;

                for (Peak peak1 : groupedSignals) {
                    for (Peak peak2 : groupedSignals) {
                        if (peak1.equals(peak2)) continue;

                        Double monoisotopicMass1 = Adduct.getMonoisotopicMassFromMZ(peak1.getMz(), adduct1, this.ionization);
                        Double monoisotopicMass2 = Adduct.getMonoisotopicMassFromMZ(peak2.getMz(), adduct2, this.ionization);
                        if (monoisotopicMass1 == null || monoisotopicMass2 == null) continue;

                        int ppmDifference = calculatePPMIncrement(monoisotopicMass1, monoisotopicMass2);
                        if (ppmDifference < TOLERANCE_PMM) {
                            int toleranceDa1 = calculatePPMIncrement(peak1.getMz(), this.mz);
                            int toleranceDa2 = calculatePPMIncrement(peak2.getMz(), this.mz);

                            if(this.lipid.getMonoisotropic() != 0.0){
                                double inferredMass1 = getMonoisotopicMassFromMZ(this.mz, adduct1, this.ionization);
                                double inferredMass2 = getMonoisotopicMassFromMZ(this.mz, adduct2, this.ionization);
                                if (toleranceDa1 < TOLERANCE_PMM && calculatePPMIncrement(lipid.getMonoisotropic(), inferredMass1) < TOLERANCE_PMM) {
                                    return adduct1;
                                } else if (toleranceDa2 < TOLERANCE_PMM && calculatePPMIncrement(lipid.getMonoisotropic(), inferredMass2) < TOLERANCE_PMM) {
                                    return adduct2;
                                }

                            } else{
                                if (toleranceDa1 < TOLERANCE_PMM) {
                                    return adduct1;
                                } else if (toleranceDa2 < TOLERANCE_PMM) {
                                    return adduct2;
                                }
                            }
                        }
                    }
                }
            }
        }
    return "unknown";
    }

    public Ionization getIonization() {
        return ionization;
    }

    public void setIonization(Ionization ionization) {
        this.ionization = ionization;
    }

    public int getTotalScoresApplied() {
        return totalScoresApplied;
    }

    public void setTotalScoresApplied(int totalScoresApplied) {
        this.totalScoresApplied = totalScoresApplied;
    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // !TODO Take into account that the score should be normalized between 0 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }

    public double getNormalizedScore() {
        return (double) this.score / this.totalScoresApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

}
