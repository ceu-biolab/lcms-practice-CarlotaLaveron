package lipid;

import adduct.Adduct;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdductDetectionTest {
    @Before
    public void setup() {
        // TODO Empty by now,you can create common objects for all tests.
    }

    @Test
    public void shouldDetectAdductBasedOnMzDifference() {

        // Given two peaks with ~21.98 Da difference (e.g., [M+H]+ and [M+Na]+)
        Peak mH = new Peak(700.500, 100000.0); // [M+H]+
        Peak mNa = new Peak(722.482, 80000.0);  // [M+Na]+
        Lipid lipid = new Lipid(1, "PC 34:1", "C42H82NO8P", LipidType.PC, 34, 1);

        double annotationMZ = 700.49999d;
        double annotationIntensity = 80000.0;
        double annotationRT = 6.5d;
        Annotation annotation = new Annotation(lipid, annotationMZ, annotationIntensity, annotationRT, Ionization.POSITIVE,Set.of(mH, mNa));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try{
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();
            assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
        }finally {
            instance.close();
        }

        // Then we should call the algorithmic/knowledge system rules fired to detect the adduct and Set it!
        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }


    @Test
    public void shouldDetectLossOfWaterAdduct() {
        Peak mh = new Peak(700.500, 90000.0);        // [M+H]+
        Peak mhH2O = new Peak(682.4894, 70000.0);     // [M+H–H₂O]+, ~18.0106 Da less

        Lipid lipid = new Lipid(1, "PE 36:2", "C41H78NO8P", LipidType.PE, 36, 2);
        Annotation annotation = new Annotation(lipid, mh.getMz(), mh.getIntensity(), 7.5d, Ionization.POSITIVE,Set.of(mh, mhH2O));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try{
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();
            assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
        }finally {
            instance.close();
        }

        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());

    }

    @Test
    public void shouldDetectDoublyChargedAdduct() {
        // Assume real M = (700.500 - 1.0073) = 699.4927
        // So [M+2H]2+ = (M + 2.0146) / 2 = 350.7536
        Peak singlyCharged = new Peak(700.500, 100000.0);  // [M+H]+
        Peak doublyCharged = new Peak(350.754, 85000.0);   // [M+2H]2+

        Lipid lipid = new Lipid(3, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        Annotation annotation = new Annotation(lipid, singlyCharged.getMz(), singlyCharged.getIntensity(), 10d, Ionization.POSITIVE, Set.of(singlyCharged, doublyCharged));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        System.out.println("mz = " + annotation.getMz());

        try{


            annotationUnit.getAnnotations().add(annotation);
            instance.fire();
            assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
        }finally {
            instance.close();
        }

        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectNegativeAdduct() {
        // Simulamos una molécula con M = 700.500 + 1.007276 ≈ 701.507276
        // El aducto [M-H]− sería: (M - 1.007276) = 700.500

        Peak negativeAdductPeak = new Peak(698.485, 100000.0);  // [M-H]−
        Peak alternativePeak = new Peak(734.462, 75000.0);     // [M+Cl]−

        Lipid lipid = new Lipid(3, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        Annotation annotation = new Annotation(lipid, negativeAdductPeak.getMz(), negativeAdductPeak.getIntensity(), 10d, Ionization.NEGATIVE, Set.of(negativeAdductPeak, alternativePeak));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try{
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();
            assertEquals( "Adduct inferred from lowest mz in group","[M-H]−", annotation.getAdduct());
        }finally {
            instance.close();
        }

        assertNotNull("[M-H]− should be detected", annotation.getAdduct());
        assertEquals("[M-H]−", annotation.getAdduct());
    }

    @Test
    public void shouldDetectClAdduct() {
        // Simulamos una molécula con M = 700.500
        // El aducto [M+Cl]− sería: (M + 34.969402) = 735.469402
        // mapMZNegativeAdductsTMP.put("[M-H]−", 1.007276d);
        // mapMZNegativeAdductsTMP.put("[M+Cl]−", -735.469402d);

        Peak mH = new Peak(698.4854, 100000.0); // [M–H]⁻
        Peak mCl = new Peak(734.462, 80000.0);  // [M+Cl]⁻, diferencia de ~34.9694 Da      // [M-H]− (más bajo, pero menor intensidad)

        Lipid lipid = new Lipid(3, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        Annotation annotation = new Annotation(lipid, mH.getMz(), mH.getIntensity(), 10d, Ionization.NEGATIVE, Set.of(mH, mCl));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try {
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();
            assertEquals("Adduct inferred from peak with m/z ≈ M + 34.969402", "[M-H]−", annotation.getAdduct());
        } finally {
            instance.close();
        }

        assertNotNull("[M-H]− should be detected", annotation.getAdduct());
        assertEquals("[M-H]−", annotation.getAdduct());
    }

    @Test
    public void shouldDetectMostProbableAdductAmongFormateAndMH() {
        double neutralMass = 699.492724;
        // Simulamos una molécula con M = 700.500
        // [M-H]− => 700.500 - 1.007276 = 699.492724
        // [M+HCOOH-H]− => 699.492724 + 44.998201 = 744.490925

        Peak mhAdductPeak = new Peak(neutralMass - 1.007276d, 80000.0);       // [M-H]−, alta intensidad y m/z cercano
        Peak formateAdductPeak = new Peak(neutralMass + 44.998201 , 85000.0);  // [M+HCOOH-H]−, más intensidad pero menor coincidencia en m/z

        Lipid lipid = new Lipid(5, "PE 38:4", "C43H80NO8P", LipidType.PE, 38, 4);
        Annotation annotation = new Annotation(lipid, mhAdductPeak.getMz(), mhAdductPeak.getIntensity(), 10d, Ionization.NEGATIVE, Set.of(mhAdductPeak, formateAdductPeak));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try {
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();
            assertEquals("Adduct inferred should be [M-H]− due to best m/z match", "[M-H]−", annotation.getAdduct());
        } finally {
            instance.close();
        }

        assertNotNull("Adduct should not be null", annotation.getAdduct());
        assertEquals("[M-H]−", annotation.getAdduct());
    }

    @Test
    public void shouldDetectBestAdductAmongThreeNegativeOptions() {
        //Masa neutra simulada de la molécula
        double neutralMass = 699.492724;

        // Simulamos los picos detectados
        Peak clAdductPeak = new Peak(neutralMass + 34.969402, 80000.0);
        Peak formateAdductPeak = new Peak(neutralMass + 44.998201, 90000.0);
        Peak mhH2OAdductPeak = new Peak(neutralMass - 1.0073 - 18.0106, 85000.0);
        Peak doublyChargedPeak = new Peak((neutralMass - 1.007276*2)/2, 75000.0);
        //Peak mhAdductPeak = new Peak(neutralMass - 1.0073, 80000.0);

        Lipid lipid = new Lipid(6, "TG 34:1", "C39H79N2O6P", LipidType.TG, 34, 1);
        Annotation annotation = new Annotation(
                lipid,
                clAdductPeak.getMz(),   // Seleccionamos este como el m/z base
                clAdductPeak.getIntensity(),
                10d,
                Ionization.NEGATIVE,
                Set.of(clAdductPeak, formateAdductPeak, mhH2OAdductPeak, doublyChargedPeak)
        );

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try {
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();

            // Esperamos que se detecte el aducto [M+HCOOH−H]− por tener m/z más cercano al esperado
            assertEquals("[M+Cl]−", annotation.getAdduct());
        } finally {
            instance.close();
        }

        assertNotNull("Adduct should be inferred from available peaks", annotation.getAdduct());
        assertEquals("[M+Cl]−", annotation.getAdduct());
    }

    @Test
    public void shouldDetectDimerizationAndBaseMonomer() {
        double base_M = 699.492724;

        Peak monomer = new Peak(base_M + 1.007276, 100000.0);
        // Dímero [2M+H]+ 1400.993 (2*699.492724)-(-1.007276)=1399.993
        Peak dimer = new Peak((base_M*2) + 1.007276, 50000.0);

        Lipid lipid = new Lipid(5, "PC 34:2", "C42H80NO8P", LipidType.TG, 34, 2);
        lipid.setMonoisotropic(base_M);
        Annotation annotation = new Annotation(lipid, monomer.getMz(), monomer.getIntensity(), 7d, Ionization.POSITIVE,Set.of(monomer, dimer));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try {
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();

            // Esperamos que se detecte el aducto [M+HCOOH−H]− por tener m/z más cercano al esperado
            assertEquals("[M+H]+", annotation.getAdduct());
        } finally {
            instance.close();
        }

        // Verificamos que se detecta correctamente como [M+H]+ (el más pequeño)
        assertNotNull("Debe detectar el aducto principal", annotation.getAdduct());
        assertEquals("Debe ser [M+H]+ como base", "[M+H]+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectAdductFromMultiplePeaks() {
        // Definimos 4 picos compatibles con 4 aductos distintos para la misma molécula
        Peak p1 = new Peak(700.500, 100000.0);// [M+H]+
        Peak p2 = new Peak(722.482, 80000.0);// [M+Na]+
        Peak p3 = new Peak(350.754, 85000.0);// [M+2H]2+
        Peak p4 = new Peak(682.4894, 70000.0);// [M+H–H₂O]+

        // Datos del lípido (nombre y fórmula solo a efectos de completar la estructura)
        Lipid lipid = new Lipid(5, "PC 36:4", "C44H80NO8P", LipidType.TG, 36, 4);

        double rt = 6.0;

        Annotation annotation = new Annotation(lipid, p1.getMz(), p1.getIntensity(), rt, Ionization.POSITIVE ,Set.of(p1, p2, p3, p4));

        AnnotationUnit annotationUnit = new AnnotationUnit();
        RuleUnitInstance<AnnotationUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(annotationUnit);
        try {
            annotationUnit.getAnnotations().add(annotation);
            instance.fire();

            // Esperamos que se detecte el aducto [M+HCOOH−H]− por tener m/z más cercano al esperado
            assertEquals("[M+H]+", annotation.getAdduct());
        } finally {
            instance.close();
        }

        // Comprobamos que ha detectado correctamente el aducto principal
        //assertNotNull("Adduct should be detected", annotation.getAdduct());
        assertEquals("[M+H]+", annotation.getAdduct());  // Es el que coincide con annotationMz
        }


}

