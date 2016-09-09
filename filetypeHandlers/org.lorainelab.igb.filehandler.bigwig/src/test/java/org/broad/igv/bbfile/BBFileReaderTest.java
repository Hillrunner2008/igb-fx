package org.broad.igv.bbfile;

import java.io.IOException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BBFileReaderTest {

    @Test
    public void testBigBed() throws IOException {

        String path = "chr21.refseq.bb";
        BBFileReader bbReader = new BBFileReader(BBFileReaderTest.class.getClassLoader().getResource(path).getPath());

        BBFileHeader bbFileHdr = bbReader.getBBFileHeader();
        assertTrue(bbFileHdr.isBigBed());

        String chr = "chr21";
        int start = 26490012;
        int end = 42182827;

        for (BBZoomLevelHeader header : bbReader.getZoomLevels().getZoomLevelHeaders()) {
            assertNotNull(header);

            ZoomLevelIterator zlIter = bbReader.getZoomLevelIterator(header.getZoomLevel(), chr, start, chr, end, false);

            while (zlIter.hasNext()) {
                ZoomDataRecord rec = zlIter.next();
                int n = rec.getBasesCovered();
                if (n > 0) {
                    assertTrue(chr.equals(rec.getChromName()));
                    assertTrue(rec.getChromEnd() >= start && rec.getChromStart() <= end);
                }

            }
        }

    }

    @Test
    public void testBigWig() throws IOException {

        String path = "variable_step.bw";
        BBFileReader bbReader = new BBFileReader(BBFileReaderTest.class.getClassLoader().getResource(path).getPath());

        BBFileHeader bbFileHdr = bbReader.getBBFileHeader();
        assertTrue(bbFileHdr.isBigWig());

        String chr = "chr2";
        int start = 300700;
        int end = 300788;

        for (BBZoomLevelHeader header : bbReader.getZoomLevels().getZoomLevelHeaders()) {
            assertNotNull(header);
            ZoomLevelIterator zlIter = bbReader.getZoomLevelIterator(header.getZoomLevel(), chr, start, chr, end, false);

            while (zlIter.hasNext()) {
                ZoomDataRecord rec = zlIter.next();
                System.out.println("chr start {}" + rec.getChromStart() + " chr end "+ rec.getChromEnd());
                int n = rec.getBasesCovered();
                if (n > 0) {
                    assertTrue(chr.equals(rec.getChromName()));
                    assertTrue(rec.getChromEnd() >= start && rec.getChromStart() <= end);
                }

            }
        }

    }
}
