package evidence_verification.service;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.repository.BlockchainRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private BlockchainRecordRepository repository;

    @InjectMocks
    private MockBlockchainServiceImpl mockBlockchainService;

    @Test
    void testAnchorHash_Success() {
        when(repository.save(any(BlockchainRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlockchainRecord record = mockBlockchainService.anchorHash("EVI-200", "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e");

        assertNotNull(record);
        assertEquals("EVI-200", record.getEvidenceId());
        assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", record.getFileHash());
        assertNotNull(record.getTransactionHash());
        assertTrue(record.getTransactionHash().startsWith("0x"));
        assertEquals("CONFIRMED", record.getStatus());
        assertNotNull(record.getBlockchainTimestamp());

        verify(repository, times(1)).save(any(BlockchainRecord.class));
    }

    @Test
    void testGetRecord_Success() {
        BlockchainRecord record = new BlockchainRecord("EVI-200", "hash123", "0xtx123", "CONFIRMED");

        when(repository.findByEvidenceId("EVI-200")).thenReturn(Optional.of(record));

        Optional<BlockchainRecord> result = mockBlockchainService.getRecord("EVI-200");

        assertTrue(result.isPresent());
        assertEquals("EVI-200", result.get().getEvidenceId());
        assertEquals("hash123", result.get().getFileHash());
        assertEquals("0xtx123", result.get().getTransactionHash());

        verify(repository, times(1)).findByEvidenceId("EVI-200");
    }

    @Test
    void testRealBlockchainService_AnchorHash_RPCFailure_HandledGracefully() {
        when(repository.save(any(BlockchainRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Instantiate RealBlockchainServiceImpl with invalid credentials/RPC to test graceful error handling
        RealBlockchainServiceImpl realService = new RealBlockchainServiceImpl(
                repository,
                "http://invalid-rpc-url-that-fails.internal",
                "0x0000000000000000000000000000000000000000000000000000000000000001",
                "0x0000000000000000000000000000000000000002",
                80002L
        );

        BlockchainRecord record = realService.anchorHash("EVI-300", "filehash300");

        assertNotNull(record);
        assertEquals("EVI-300", record.getEvidenceId());
        assertEquals("filehash300", record.getFileHash());
        assertTrue(record.getStatus().startsWith("FAILED"));
        verify(repository, times(1)).save(any(BlockchainRecord.class));
    }

    @Test
    void testRealBlockchainService_GetRecord_FallbackToRepository() {
        BlockchainRecord existingRecord = new BlockchainRecord("EVI-400", "hash400", "0xtx400", "CONFIRMED");
        when(repository.findByEvidenceId("EVI-400")).thenReturn(Optional.of(existingRecord));

        RealBlockchainServiceImpl realService = new RealBlockchainServiceImpl(
                repository,
                "http://invalid-rpc-url-that-fails.internal",
                "0x0000000000000000000000000000000000000000000000000000000000000001",
                "0x0000000000000000000000000000000000000002",
                80002L
        );

        Optional<BlockchainRecord> result = realService.getRecord("EVI-400");

        assertTrue(result.isPresent());
        assertEquals("EVI-400", result.get().getEvidenceId());
        assertEquals("hash400", result.get().getFileHash());
        verify(repository, times(1)).findByEvidenceId("EVI-400");
    }
}
