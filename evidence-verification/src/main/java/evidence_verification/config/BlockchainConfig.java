package evidence_verification.config;

import evidence_verification.repository.BlockchainRecordRepository;
import evidence_verification.service.BlockchainService;
import evidence_verification.service.MockBlockchainServiceImpl;
import evidence_verification.service.RealBlockchainServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class BlockchainConfig {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainConfig.class);

    @Value("${blockchain.mode:mock}")
    private String mode;

    @Value("${blockchain.rpc-url:https://rpc-amoy.polygon.technology}")
    private String rpcUrl;

    @Value("${blockchain.private-key:}")
    private String privateKey;

    @Value("${blockchain.contract-address:}")
    private String contractAddress;

    @Value("${blockchain.chain-id:80002}")
    private long chainId;

    @Bean
    @Primary
    public BlockchainService blockchainService(BlockchainRecordRepository repository) {
        if ("real".equalsIgnoreCase(mode)) {
            if (privateKey == null || privateKey.isBlank()) {
                logger.warn("BLOCKCHAIN_MODE is set to 'real', but BLOCKCHAIN_PRIVATE_KEY is missing! Falling back to Mock Blockchain service.");
            } else if (contractAddress == null || contractAddress.isBlank()) {
                logger.warn("BLOCKCHAIN_MODE is set to 'real', but BLOCKCHAIN_CONTRACT_ADDRESS is missing! Falling back to Mock Blockchain service.");
            } else {
                logger.info("Initializing REAL Blockchain Service connected to Polygon Amoy Testnet (RPC: {}, Contract: {})", rpcUrl, contractAddress);
                return new RealBlockchainServiceImpl(repository, rpcUrl, privateKey, contractAddress, chainId);
            }
        }
        logger.info("Initializing MOCK Blockchain Service.");
        return new MockBlockchainServiceImpl(repository);
    }
}
