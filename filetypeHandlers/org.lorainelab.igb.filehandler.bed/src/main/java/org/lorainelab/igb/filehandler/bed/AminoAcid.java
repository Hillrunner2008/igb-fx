package org.lorainelab.igb.filehandler.bed;

import java.util.HashMap;
import java.util.Map;

public enum AminoAcid {

    Alanine("Ala", 'A'),
    Arginine("Arg", 'R'),
    Asparagine("Asn", 'N'),
    AsparticAcid("Asp", 'D'),
    Cysteine("Cys", 'C'),
    GlutamicAcid("Glu", 'E'),
    Glutamine("Gln", 'Q'),
    Glycine("Gly", 'G'),
    Histidine("His", 'H'),
    Isoleucine("Ile", 'I'),
    Leucine("Leu", 'L'),
    Lysine("Lys", 'K'),
    Methionine("Met", 'M'),
    Phenylalanine("Phe", 'F'),
    Proline("Pro", 'P'),
    Serine("Ser", 'S'),
    Threonine("Thr", 'T'),
    Tryptophan("Trp", 'W'),
    Tyrosine("Tyr", 'Y'),
    Valine("Val", 'V'),
    //	Selenocysteine("Sec", 'U'),
    //	Pyrrolysine("Pyl", 'O'),
    STOP("---", '-');

    public static final String START_CODON = "ATG"; //AminoAcid.Methionine - not true for all species
    public static final String[] STOP_CODONS = new String[]{"TAA", "TAG", "TGA"};
    public static final HashMap<String, AminoAcid> CODON_TO_AMINO_ACID = new HashMap<>();

    static {
        CODON_TO_AMINO_ACID.put("AAA", AminoAcid.Lysine);
        CODON_TO_AMINO_ACID.put("AAC", AminoAcid.Asparagine);
        CODON_TO_AMINO_ACID.put("AAG", AminoAcid.Lysine);
        CODON_TO_AMINO_ACID.put("AAT", AminoAcid.Asparagine);
        CODON_TO_AMINO_ACID.put("ACA", AminoAcid.Threonine);
        CODON_TO_AMINO_ACID.put("ACC", AminoAcid.Threonine);
        CODON_TO_AMINO_ACID.put("ACG", AminoAcid.Threonine);
        CODON_TO_AMINO_ACID.put("ACT", AminoAcid.Threonine);
        CODON_TO_AMINO_ACID.put("AGA", AminoAcid.Arginine);
        CODON_TO_AMINO_ACID.put("AGC", AminoAcid.Serine);
        CODON_TO_AMINO_ACID.put("AGG", AminoAcid.Arginine);
        CODON_TO_AMINO_ACID.put("AGT", AminoAcid.Serine);
        CODON_TO_AMINO_ACID.put("ATA", AminoAcid.Isoleucine);
        CODON_TO_AMINO_ACID.put("ATC", AminoAcid.Isoleucine);
        CODON_TO_AMINO_ACID.put("ATG", AminoAcid.Methionine);
        CODON_TO_AMINO_ACID.put("ATT", AminoAcid.Isoleucine);
        CODON_TO_AMINO_ACID.put("CAA", AminoAcid.Glutamine);
        CODON_TO_AMINO_ACID.put("CAC", AminoAcid.Histidine);
        CODON_TO_AMINO_ACID.put("CAG", AminoAcid.Glutamine);
        CODON_TO_AMINO_ACID.put("CAT", AminoAcid.Histidine);
        CODON_TO_AMINO_ACID.put("CCA", AminoAcid.Proline);
        CODON_TO_AMINO_ACID.put("CCC", AminoAcid.Proline);
        CODON_TO_AMINO_ACID.put("CCG", AminoAcid.Proline);
        CODON_TO_AMINO_ACID.put("CCT", AminoAcid.Proline);
        CODON_TO_AMINO_ACID.put("CGA", AminoAcid.Arginine);
        CODON_TO_AMINO_ACID.put("CGC", AminoAcid.Arginine);
        CODON_TO_AMINO_ACID.put("CGG", AminoAcid.Arginine);
        CODON_TO_AMINO_ACID.put("CGT", AminoAcid.Arginine);
        CODON_TO_AMINO_ACID.put("CTA", AminoAcid.Leucine);
        CODON_TO_AMINO_ACID.put("CTC", AminoAcid.Leucine);
        CODON_TO_AMINO_ACID.put("CTG", AminoAcid.Leucine);
        CODON_TO_AMINO_ACID.put("CTT", AminoAcid.Leucine);
        CODON_TO_AMINO_ACID.put("GAA", AminoAcid.GlutamicAcid);
        CODON_TO_AMINO_ACID.put("GAC", AminoAcid.AsparticAcid);
        CODON_TO_AMINO_ACID.put("GAG", AminoAcid.GlutamicAcid);
        CODON_TO_AMINO_ACID.put("GAT", AminoAcid.AsparticAcid);
        CODON_TO_AMINO_ACID.put("GCA", AminoAcid.Alanine);
        CODON_TO_AMINO_ACID.put("GCC", AminoAcid.Alanine);
        CODON_TO_AMINO_ACID.put("GCG", AminoAcid.Alanine);
        CODON_TO_AMINO_ACID.put("GCT", AminoAcid.Alanine);
        CODON_TO_AMINO_ACID.put("GGA", AminoAcid.Glycine);
        CODON_TO_AMINO_ACID.put("GGC", AminoAcid.Glycine);
        CODON_TO_AMINO_ACID.put("GGG", AminoAcid.Glycine);
        CODON_TO_AMINO_ACID.put("GGT", AminoAcid.Glycine);
        CODON_TO_AMINO_ACID.put("GTA", AminoAcid.Valine);
        CODON_TO_AMINO_ACID.put("GTC", AminoAcid.Valine);
        CODON_TO_AMINO_ACID.put("GTG", AminoAcid.Valine);
        CODON_TO_AMINO_ACID.put("GTT", AminoAcid.Valine);
        CODON_TO_AMINO_ACID.put("TAA", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("TAC", AminoAcid.Tyrosine);
        CODON_TO_AMINO_ACID.put("TAG", AminoAcid.STOP); // AminoAcid.Pyrrolysine
        CODON_TO_AMINO_ACID.put("TAT", AminoAcid.Tyrosine);
        CODON_TO_AMINO_ACID.put("TCA", AminoAcid.Serine);
        CODON_TO_AMINO_ACID.put("TCC", AminoAcid.Serine);
        CODON_TO_AMINO_ACID.put("TCG", AminoAcid.Serine);
        CODON_TO_AMINO_ACID.put("TCT", AminoAcid.Serine);
        CODON_TO_AMINO_ACID.put("TGA", AminoAcid.STOP); // AminoAcid.Selenocysteine
        CODON_TO_AMINO_ACID.put("TGC", AminoAcid.Cysteine);
        CODON_TO_AMINO_ACID.put("TGG", AminoAcid.Tryptophan);
        CODON_TO_AMINO_ACID.put("TGT", AminoAcid.Cysteine);
        CODON_TO_AMINO_ACID.put("TTA", AminoAcid.Leucine);
        CODON_TO_AMINO_ACID.put("TTC", AminoAcid.Phenylalanine);
        CODON_TO_AMINO_ACID.put("TTG", AminoAcid.Leucine);
        CODON_TO_AMINO_ACID.put("TTT", AminoAcid.Phenylalanine);

        //SPECIAL CASES WHERE TRANSLATION WAS FORCED.
        // Using permutation and combination.
        // No of possibilites for first place
        CODON_TO_AMINO_ACID.put("A**", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("T**", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("G**", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("C**", AminoAcid.STOP);

        // No of possibilies for first and second place
        CODON_TO_AMINO_ACID.put("AA*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("AT*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("AG*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("AC*", AminoAcid.STOP);

        CODON_TO_AMINO_ACID.put("TA*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("TT*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("TG*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("TC*", AminoAcid.STOP);

        CODON_TO_AMINO_ACID.put("GA*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("GT*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("GG*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("GC*", AminoAcid.STOP);

        CODON_TO_AMINO_ACID.put("CA*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("CT*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("CG*", AminoAcid.STOP);
        CODON_TO_AMINO_ACID.put("CC*", AminoAcid.STOP);
    }

    private static final Map<Character, AminoAcid> code1aminoAcid = new HashMap<>();

    static {
        code1aminoAcid.put('A', Alanine);
        code1aminoAcid.put('R', Arginine);
        code1aminoAcid.put('N', Asparagine);
        code1aminoAcid.put('D', AsparticAcid);
        code1aminoAcid.put('C', Cysteine);
        code1aminoAcid.put('E', GlutamicAcid);
        code1aminoAcid.put('Q', Glutamine);
        code1aminoAcid.put('G', Glycine);
        code1aminoAcid.put('H', Histidine);
        code1aminoAcid.put('I', Isoleucine);
        code1aminoAcid.put('L', Leucine);
        code1aminoAcid.put('K', Lysine);
        code1aminoAcid.put('M', Methionine);
        code1aminoAcid.put('F', Phenylalanine);
        code1aminoAcid.put('P', Proline);
        code1aminoAcid.put('S', Serine);
        code1aminoAcid.put('T', Threonine);
        code1aminoAcid.put('W', Tryptophan);
        code1aminoAcid.put('Y', Tyrosine);
        code1aminoAcid.put('V', Valine);
        code1aminoAcid.put('-', STOP);
    }
    private final String code;
    private final char letter;

    private AminoAcid(String code, char letter) {
        this.code = code;
        this.letter = letter;
    }

    public String getCode() {
        return code;
    }

    public char getLetter() {
        return letter;
    }

    public static String getAminoAcid(String residues, boolean forward) {
        if (!forward) {
            residues = complement(residues);
        }
        StringBuilder aminoAcidsSB = new StringBuilder("");
        String nextCodon;
        for (int pos = 0; pos < residues.length(); pos += 3) {
            try {
                nextCodon = residues.substring(pos, pos + 3).toUpperCase();
            } catch (StringIndexOutOfBoundsException ex) {
                nextCodon = "---";
            }
            String aaCode;
            if (nextCodon.contains("-")) {
                if (forward) {
                    aaCode = "-  ";
                } else {
                    aaCode = "  -";
                }
            } else {
                AminoAcid aminoAcid = AminoAcid.CODON_TO_AMINO_ACID.get(nextCodon);
                char aminoAcidletter = aminoAcid == null ? ' ' : aminoAcid.getLetter();
                if (forward) {
                    aaCode = aminoAcidletter + "  ";
                } else {
                    aaCode = "  " + aminoAcidletter;
                }
            }
            if (forward) {
                aminoAcidsSB.append(aaCode);
            } else {
                aminoAcidsSB.insert(0, aaCode);
            }
        }
        String aminoAcids = aminoAcidsSB.toString();

        return aminoAcids.substring(0, aminoAcids.length());
    }

    private static String complement(String dna) {
        dna = reverse(dna).toLowerCase();
        StringBuilder comp = new StringBuilder();
        for (int k = 0; k < dna.length(); k++) {
            if (dna.charAt(k) == 'a') {
                comp.append('t');
            }
            if (dna.charAt(k) == 't') {
                comp.append('a');
            }
            if (dna.charAt(k) == 'c') {
                comp.append('g');
            }
            if (dna.charAt(k) == 'g') {
                comp.append('c');
            }
            if (dna.charAt(k) == '-') {
                comp.append('-');
            }
        }
        return comp.toString();
    }

    private static String reverse(String s) {
        final StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }
}
