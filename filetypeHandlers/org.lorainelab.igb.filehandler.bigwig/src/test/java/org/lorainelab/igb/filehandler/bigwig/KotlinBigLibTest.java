package org.lorainelab.igb.filehandler.bigwig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.jetbrains.bio.big.BigSummary;
import org.jetbrains.bio.big.BigWigFile;
import org.junit.Test;

/**
 *
 * @author dcnorris
 */
public class KotlinBigLibTest {

    @Test
    public void testKotlinLib() throws IOException, URISyntaxException {
        String path = "variable_step.bw";
        BigWigFile read = BigWigFile.read(Paths.get(KotlinBigLibTest.class.getClassLoader().getResource(path).toURI()));
        BigSummary totalSummary = read.getTotalSummary();
        long count = totalSummary.getCount();
        System.out.println(totalSummary);
        for (String chr : read.getChromosomes().valueCollection()) {
            System.out.println(chr);
        }
    }
}
