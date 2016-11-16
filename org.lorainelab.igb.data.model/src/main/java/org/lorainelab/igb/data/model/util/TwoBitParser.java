package org.lorainelab.igb.data.model.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import htsjdk.samtools.seekablestream.SeekableBufferedStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import java.io.IOException;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.synonymservice.ChromosomeSynomymService;
import org.slf4j.LoggerFactory;

public class TwoBitParser implements ReferenceSequenceProvider {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TwoBitParser.class);
    public static int DEFAULT_BUFFER_SIZE = 10000;
    private static final int MAGIC_NUMBER = 0x1A412743;
    private boolean reverse;
    private String[] seq_names;
    private Map<String, Long> seq2pos;
    private String cur_seq_name;
    private long[][] cur_nn_blocks;
    private long[][] cur_mask_blocks;
    private long cur_seq_pos;
    private long cur_dna_size;
    private int cur_nn_block_num;
    private int cur_mask_block_num;
    private int[] cur_bits;
    private byte[] buffer;
    private long buffer_size;
    private long buffer_pos;
    private long startFilePos;
    private long file_pos;
    private static final char[] BIT_CHARS = {'T', 'C', 'A', 'G'};
    private ObservableSet<Chromosome> chromosomes;
    private final String path;
    private boolean isInitialized;
    private final ChromosomeSynomymService chromosomeSynomymService;
    private Map<String, String> seq2prefNameRef;

    public TwoBitParser(String path, ChromosomeSynomymService chromosomeSynomymService) throws Exception {
        this.path = path;
        this.chromosomeSynomymService = chromosomeSynomymService;
        seq2prefNameRef = Maps.newHashMap();
        seq2pos = Maps.newLinkedHashMap();
        chromosomes = FXCollections.observableSet(Sets.newTreeSet((o1, o2) -> o1.compareTo(o2)));
        isInitialized = false;
    }

    private void initializeLazily() {
        try (final SeekableStream seekableStream = SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(path))) {
            long sign = readFourBytes(seekableStream);
            reverse = isLittleEndianByteOrder(sign);
            readFourBytes(seekableStream);
            int seq_qnt = (int) readFourBytes(seekableStream);
            readFourBytes(seekableStream);
            seq_names = new String[seq_qnt];
            for (int i = 0; i < seq_qnt; i++) {
                int name_len = seekableStream.read();
                char[] chars = new char[name_len];
                for (int j = 0; j < name_len; j++) {
                    chars[j] = (char) seekableStream.read();
                }
                seq_names[i] = new String(chars);
                long pos = readFourBytes(seekableStream);
                seq2pos.put(seq_names[i], pos);
            }
            initializeChromosomeInfo();
            isInitialized = true;
//            CompletableFuture.supplyAsync(() -> {
//                initializeChromosomeInfo();
//                return null;
//            }).whenComplete((u, t) -> {
//                if (t != null) {
//                    Throwable ex = (Throwable) t;
//                    LOG.error(ex.getMessage(), ex);
//                }
//            });
        } catch (Exception ex) {
            LOG.error("Could not read 2Bit file", ex);
        }
    }

    private long readFourBytes(SeekableStream seekableStream) throws Exception {
        long ret = 0;
        if (!reverse) {
            ret = seekableStream.read();
            ret += seekableStream.read() * 0x100;
            ret += seekableStream.read() * 0x10000;
            ret += seekableStream.read() * 0x1000000;
        } else {
            ret = seekableStream.read() * 0x1000000;
            ret += seekableStream.read() * 0x10000;
            ret += seekableStream.read() * 0x100;
            ret += seekableStream.read();
        }
        return ret;
    }

    private String[] getSequenceNames() {
        String[] ret = new String[seq_names.length];
        System.arraycopy(seq_names, 0, ret, 0, seq_names.length);
        return ret;
    }

    /**
     * Method open nucleotide stream for sequence with given name.
     *
     * @param seqName name of sequence (one of returned by getSequenceNames()).
     * @throws Exception
     */
    private void setCurrentSequence(SeekableStream seekableStream, String seqName) throws Exception {
        if (cur_seq_name != null) {
            throw new Exception("Sequence [" + cur_seq_name + "] was not closed");
        }
        if (seq2pos.get(seqName) == null) {
            throw new Exception("Sequence [" + seqName + "] was not found in 2bit file");
        }
        cur_seq_name = seqName;
        long pos = seq2pos.get(seqName);
        seekableStream.seek(pos);
        long dna_size = readFourBytes(seekableStream);
        cur_dna_size = dna_size;
        int nn_block_qnt = (int) readFourBytes(seekableStream);
        cur_nn_blocks = new long[nn_block_qnt][2];
        for (int i = 0; i < nn_block_qnt; i++) {
            cur_nn_blocks[i][0] = readFourBytes(seekableStream);
        }
        for (int i = 0; i < nn_block_qnt; i++) {
            cur_nn_blocks[i][1] = readFourBytes(seekableStream);
        }
        int mask_block_qnt = (int) readFourBytes(seekableStream);
        cur_mask_blocks = new long[mask_block_qnt][2];
        for (int i = 0; i < mask_block_qnt; i++) {
            cur_mask_blocks[i][0] = readFourBytes(seekableStream);
        }
        for (int i = 0; i < mask_block_qnt; i++) {
            cur_mask_blocks[i][1] = readFourBytes(seekableStream);
        }
        readFourBytes(seekableStream);
        startFilePos = seekableStream.position();
        reset();
    }

    /**
     * Method resets current position to the begining of sequence stream.
     */
    private synchronized void reset() throws IOException {
        cur_seq_pos = 0;
        cur_nn_block_num = (cur_nn_blocks.length > 0) ? 0 : -1;
        cur_mask_block_num = (cur_mask_blocks.length > 0) ? 0 : -1;
        cur_bits = new int[4];
        file_pos = startFilePos;
        buffer_size = 0;
        buffer_pos = -1;
    }

    private void setCurrentSequencePosition(SeekableStream seekableStream, long pos) throws IOException {
        if (cur_seq_name == null) {
            throw new RuntimeException("Sequence is not set");
        }
        if (pos > cur_dna_size) {
            throw new RuntimeException(
                    "Postion is too high (more than " + cur_dna_size + ")");
        }
        if (cur_seq_pos > pos) {
            reset();
        }
        skip(seekableStream, pos - cur_seq_pos);
    }

    private void loadBits(SeekableStream seekableStream) throws IOException {
        if ((buffer == null) || (buffer_pos < 0) || (file_pos < buffer_pos)
                || (file_pos >= buffer_pos + buffer_size)) {
            if ((buffer == null) || (buffer.length != DEFAULT_BUFFER_SIZE)) {
                buffer = new byte[DEFAULT_BUFFER_SIZE];
            }
            buffer_pos = file_pos;
            buffer_size = seekableStream.read(buffer);
        }
        int cur_byte = buffer[(int) (file_pos - buffer_pos)] & 0xff;
        for (int i = 0; i < 4; i++) {
            cur_bits[3 - i] = cur_byte % 4;
            cur_byte /= 4;
        }
    }

    /**
     * Method reads 1 nucleotide from sequence stream. You should set current sequence
     * before use it.
     */
    private int read(SeekableStream seekableStream) throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        if (cur_seq_pos == cur_dna_size) {
            return -1;
        }
        int bit_num = (int) cur_seq_pos % 4;
        if (bit_num == 0) {
            loadBits(seekableStream);
        } else if (bit_num == 3) {
            file_pos++;
        }
        char ret = 'N';
        if ((cur_nn_block_num >= 0)
                && (cur_nn_blocks[cur_nn_block_num][0] <= cur_seq_pos)) {
            if (cur_bits[bit_num] != 0) {
                throw new IOException("Wrong data in NN-block (" + cur_bits[bit_num] + ") "
                        + "at position " + cur_seq_pos);
            }
            if (cur_nn_blocks[cur_nn_block_num][0] + cur_nn_blocks[cur_nn_block_num][1] == cur_seq_pos + 1) {
                cur_nn_block_num++;
                if (cur_nn_block_num >= cur_nn_blocks.length) {
                    cur_nn_block_num = -1;
                }
            }
            ret = 'N';
        } else {
            ret = BIT_CHARS[cur_bits[bit_num]];
        }
        if ((cur_mask_block_num >= 0)
                && (cur_mask_blocks[cur_mask_block_num][0] <= cur_seq_pos)) {
            ret = Character.toLowerCase(ret);
            if (cur_mask_blocks[cur_mask_block_num][0] + cur_mask_blocks[cur_mask_block_num][1] == cur_seq_pos + 1) {
                cur_mask_block_num++;
                if (cur_mask_block_num >= cur_mask_blocks.length) {
                    cur_mask_block_num = -1;
                }
            }
        }
        cur_seq_pos++;
        return (int) ret;
    }

    /**
     * Method skips n nucleotides in sequence stream. You should set current sequence
     * before use it.
     */
    private synchronized long skip(SeekableStream seekableStream, long n) throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        if (n < 4) {
            int ret = 0;
            while ((ret < n) && (read(seekableStream) >= 0)) {
                ret++;
            }
            return ret;
        }
        if (n > cur_dna_size - cur_seq_pos) {
            n = cur_dna_size - cur_seq_pos;
        }
        cur_seq_pos += n;
        file_pos = startFilePos + (cur_seq_pos / 4);
        seekableStream.seek(file_pos);
        if ((cur_seq_pos % 4) != 0) {
            loadBits(seekableStream);
        }
        while ((cur_nn_block_num >= 0)
                && (cur_nn_blocks[cur_nn_block_num][0] + cur_nn_blocks[cur_nn_block_num][1] <= cur_seq_pos)) {
            cur_nn_block_num++;
            if (cur_nn_block_num >= cur_nn_blocks.length) {
                cur_nn_block_num = -1;
            }
        }
        while ((cur_mask_block_num >= 0)
                && (cur_mask_blocks[cur_mask_block_num][0] + cur_mask_blocks[cur_mask_block_num][1] <= cur_seq_pos)) {
            cur_mask_block_num++;
            if (cur_mask_block_num >= cur_mask_blocks.length) {
                cur_mask_block_num = -1;
            }
        }
        return n;
    }

    /**
     * Method closes current sequence and it's necessary to invoke it before setting
     * new current sequence.
     */
    public void close() {
        cur_seq_name = null;
        cur_nn_blocks = null;
        cur_mask_blocks = null;
        cur_seq_pos = -1;
        cur_dna_size = -1;
        cur_nn_block_num = -1;
        cur_mask_block_num = -1;
        cur_bits = null;
        buffer_size = 0;
        buffer_pos = -1;
        file_pos = -1;
        startFilePos = -1;
    }

    private int available() throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        return (int) (cur_dna_size - cur_seq_pos);
    }

    private String loadFragment(SeekableStream seekableStream, long seq_pos, int len) throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        setCurrentSequencePosition(seekableStream, seq_pos);
        char[] ret = new char[len];
        int i = 0;
        for (; i < len; i++) {
            int ch = read(seekableStream);
            if (ch < 0) {
                break;
            }
            ret[i] = (char) ch;
        }
        return new String(ret, 0, i);
    }

    @Override
    public String getSequence(String chromosomeId) {
        if (!isInitialized) {
            initializeLazily();
        }
        chromosomeId = seq2prefNameRef.get(chromosomeId);
        String sequence = "";
        try (SeekableStream seekableStream = new SeekableBufferedStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(path)))) {
            setCurrentSequence(seekableStream, chromosomeId);
            sequence = loadFragment(seekableStream, 0, available());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            close();
        }
        return sequence;
    }

    @Override
    public String getSequence(String chromosomeId, int start, int length) {
        if (!isInitialized) {
            initializeLazily();
        }
        chromosomeId = seq2prefNameRef.get(chromosomeId);
        String sequence = "";
        try (SeekableStream seekableStream = new SeekableBufferedStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(path)))) {
            setCurrentSequence(seekableStream, chromosomeId);
            sequence = loadFragment(seekableStream, start, length);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            close();
        }
        return sequence;
    }

    private void initializeChromosomeInfo() {
        try (SeekableStream seekableStream = new SeekableBufferedStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(path)))) {
            seq2pos.entrySet().forEach(entry -> {
                try {
                    long pos = seq2pos.get(entry.getKey());
                    seekableStream.seek(pos);
                    long size = readFourBytes(seekableStream);
                    String chromosomeName = entry.getKey();
                    chromosomeName = chromosomeSynomymService.getPreferredChromosomeName(chromosomeName).orElse(chromosomeName);
                    seq2prefNameRef.put(chromosomeName, entry.getKey());
                    chromosomes.add(new Chromosome(chromosomeName, (int) size, this));
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            });
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public ObservableSet<Chromosome> getChromosomes() {
        if (!isInitialized) {
            initializeLazily();
        }
        return chromosomes;
    }

    private boolean isLittleEndianByteOrder(long sign) throws Exception {
        if ((int) sign == MAGIC_NUMBER) {
            return false;
        } else if (sign == 0x4327411A) {
            return true;
        } else {
            throw new Exception("Wrong start signature in 2BIT format");
        }
    }

    @Override
    public String getPath() {
        return path;
    }

}
