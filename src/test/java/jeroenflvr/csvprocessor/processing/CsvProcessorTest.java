package jeroenflvr.csvprocessor.processing;

import org.junit.jupiter.api.Test;

import jeroenflvr.csvprocessor.model.ProcessedRecord;
import jeroenflvr.csvprocessor.processing.CsvProcessor;

import static org.junit.jupiter.api.Assertions.*;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

/**
 * Unit tests for CsvProcessor.
 */
class CsvProcessorTest {

    private final CsvProcessor csvProcessor = new CsvProcessor();

    @Test
    void testProcessValidCsv() throws Exception {
        String csvData = "o_orderkey;o_custkey;o_orderstatus;o_totalprice;o_orderdate\n" +
                        "1;123;O;100.50;1996-01-02\n" +
                        "2;456;F;200.75;1996-12-01\n";
        
        BufferedReader reader = new BufferedReader(new StringReader(csvData));
        List<ProcessedRecord> records = csvProcessor.processCSV(reader, "test-file.csv");
        
        assertEquals(2, records.size());
        
        ProcessedRecord firstRecord = records.get(0);
        assertEquals("1_123", firstRecord.getCompositeKey());
        assertEquals("O", firstRecord.getData().get("o_orderstatus"));
        assertEquals("100.50", firstRecord.getData().get("o_totalprice"));
        assertEquals("1996-01-02", firstRecord.getData().get("o_orderdate"));
        assertEquals("test-file.csv", firstRecord.getData().get("input_filename"));
        assertNotNull(firstRecord.getData().get("imported"));
        
        // Verify that composite key columns are not in the data (to avoid duplication)
        assertNull(firstRecord.getData().get("o_orderkey"));
        assertNull(firstRecord.getData().get("o_custkey"));
    }

    @Test
    void testProcessEmptyFile() throws Exception {
        String csvData = "";
        BufferedReader reader = new BufferedReader(new StringReader(csvData));
        List<ProcessedRecord> records = csvProcessor.processCSV(reader, "empty-file.csv");
        
        assertTrue(records.isEmpty());
    }

    @Test
    void testProcessCsvWithBOM() throws Exception {
        String csvData = "\uFEFFo_orderkey;o_custkey;o_orderstatus;o_totalprice;o_orderdate\n" +
                        "1;123;O;100.50;1996-01-02\n";
        
        BufferedReader reader = new BufferedReader(new StringReader(csvData));
        List<ProcessedRecord> records = csvProcessor.processCSV(reader, "bom-file.csv");
        
        assertEquals(1, records.size());
        assertEquals("1_123", records.get(0).getCompositeKey());
    }

    @Test
    void testProcessCsvWithTrailingEmptyColumns() throws Exception {
        String csvData = "o_orderkey;o_custkey;o_orderstatus;o_totalprice;;\n" +
                        "1;123;O;100.50;;\n";
        
        BufferedReader reader = new BufferedReader(new StringReader(csvData));
        List<ProcessedRecord> records = csvProcessor.processCSV(reader, "trailing-empty.csv");
        
        assertEquals(1, records.size());
        assertEquals("1_123", records.get(0).getCompositeKey());
    }
}
