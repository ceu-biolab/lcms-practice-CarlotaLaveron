package adduct;

import lipid.Ionization;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    public static int extractSign(String adduct) {
        Pattern pattern = Pattern.compile("\\[([0-9]*)M[^\\]]*]([0-9]*)([+-−])?");
        Matcher matcher = pattern.matcher(adduct);
        if (matcher.find()) {
            String chargeStr = matcher.group(2);
            return matcher.group(3).equals("-") ? -1 : 1;
        }
        return 0; // default
    }

    public static int extractCharge(String adduct) {
        Pattern pattern = Pattern.compile("\\[([0-9]*)M[^\\]]*]([0-9]*)([+-−])?");
        Matcher matcher = pattern.matcher(adduct);
        if (matcher.find()) {
            String chargeStr = matcher.group(2);
            return chargeStr.isEmpty() ? 1 : Integer.parseInt(chargeStr);
        }
        return 1; // default
    }

    public static int extractMultimer(String adduct) {
        Pattern pattern = Pattern.compile("\\[([0-9]*)M[^\\]]*]([0-9]*)([+-−])?");
        Matcher matcher = pattern.matcher(adduct);
        if (matcher.find()) {
            String multimerStr = matcher.group(1);
            return multimerStr.isEmpty() ? 1 : Integer.parseInt(multimerStr);
        }
        return 1;
    }
    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param mz experimental mz
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return the mass difference within the tolerance respecting to the
     * massToSearch
     */

    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct, Ionization ionization) {
        if (mz != null || adduct != null) {
            int charge = extractCharge(adduct);
            int multimer = extractMultimer(adduct);
            //int sign = extractSign(adduct);
            Double adductMass = 0.0;

            Map<String, Double> adductMap = ionization == Ionization.POSITIVE ? AdductList.MAPMZPOSITIVEADDUCTS : AdductList.MAPMZNEGATIVEADDUCTS;
            adductMass = adductMap.get(adduct);

            //System.out.println("adduct: " +adduct +", charge: " +charge+", multimer: " + multimer);
            return (mz * charge + adductMass)  / multimer;

        }
        //System.out.println("No multimer charge detection workds" + adduct);
        return null;

    }

    public static Double getMZFromMonoisotopicMass(Double mm, String adduct, Ionization ionization) {
        if (mm != null || adduct != null) {
            int charge = extractCharge(adduct);
            int multimer = extractMultimer(adduct);
            Double adductMass = 0.0;

            Map<String, Double> adductMap = ionization == Ionization.POSITIVE ? AdductList.MAPMZPOSITIVEADDUCTS : AdductList.MAPMZNEGATIVEADDUCTS;
            adductMass = adductMap.get(adduct);

            return ((mm * multimer) - adductMass) / charge;
        }
        return null;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass Mass measured by MS
     * @param theoreticalMass Theoretical mass of the compound
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000
                / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param ppm ppm of tolerance
     */
    public static double calculateDeltaPPM(Double experimentalMass, int ppm) {
        double deltaPPM;
        deltaPPM =  Math.round(Math.abs((experimentalMass * ppm) / 1000000));
        return deltaPPM;

    }

}
