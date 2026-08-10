package evidence_verification.service;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.repository.BlockchainRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RealBlockchainServiceImpl implements BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(RealBlockchainServiceImpl.class);

    private final BlockchainRecordRepository repository;
    private final String rpcUrl;
    private final String privateKey;
    private final String contractAddress;
    private final long chainId;

    private Web3j web3j;

    public RealBlockchainServiceImpl(
            BlockchainRecordRepository repository,
            String rpcUrl,
            String privateKey,
            String contractAddress,
            long chainId) {
        this.repository = repository;
        this.rpcUrl = rpcUrl;
        this.privateKey = privateKey;
        this.contractAddress = contractAddress;
        this.chainId = chainId;
    }

    public RealBlockchainServiceImpl(
            BlockchainRecordRepository repository,
            String rpcUrl,
            String privateKey,
            String contractAddress,
            long chainId,
            Web3j web3j) {
        this.repository = repository;
        this.rpcUrl = rpcUrl;
        this.privateKey = privateKey;
        this.contractAddress = contractAddress;
        this.chainId = chainId;
        this.web3j = web3j;
    }

    private synchronized Web3j getWeb3j() {
        if (this.web3j == null) {
            this.web3j = Web3j.build(new HttpService(rpcUrl));
        }
        return this.web3j;
    }

    @Override
    public BlockchainRecord anchorHash(String evidenceId, String fileHash) {
        logger.info("Anchoring evidence ID {} to Polygon Amoy smart contract at {}", evidenceId, contractAddress);

        try {
            Web3j web3 = getWeb3j();
            Credentials credentials = Credentials.create(privateKey);
            RawTransactionManager txManager = new RawTransactionManager(web3, credentials, chainId);

            Function function = new Function(
                    "storeEvidence",
                    Arrays.asList(new Utf8String(evidenceId), new Utf8String(fileHash)),
                    Collections.emptyList()
            );

            String encodedFunction = FunctionEncoder.encode(function);

            BigInteger gasPrice = web3.ethGasPrice().send().getGasPrice();
            BigInteger gasLimit = BigInteger.valueOf(300_000);

            EthSendTransaction response = txManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    contractAddress,
                    encodedFunction,
                    BigInteger.ZERO
            );

            if (response.hasError()) {
                String errMsg = "RPC error when sending transaction: " + response.getError().getMessage();
                logger.error(errMsg);
                return createAndSaveFailedRecord(evidenceId, fileHash, "FAILED: " + response.getError().getMessage());
            }

            String txHash = response.getTransactionHash();
            logger.info("Submitted transaction to Polygon Amoy. TxHash: {}", txHash);

            TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3, 2000, 30);
            TransactionReceipt receipt = receiptProcessor.waitForTransactionReceipt(txHash);

            if (receipt != null && receipt.isStatusOK()) {
                logger.info("Transaction confirmed on Polygon Amoy! Block number: {}", receipt.getBlockNumber());

                BlockchainRecord record = new BlockchainRecord(
                        evidenceId,
                        fileHash,
                        txHash,
                        "CONFIRMED"
                );
                return repository.save(record);
            } else {
                logger.error("Transaction failed or reverted on Polygon Amoy. TxHash: {}", txHash);
                return createAndSaveFailedRecord(evidenceId, fileHash, txHash, "REVERTED");
            }

        } catch (Exception e) {
            logger.error("Failed to anchor evidence to Polygon Amoy: {}", e.getMessage());
            return createAndSaveFailedRecord(evidenceId, fileHash, "FAILED: " + e.getMessage());
        }
    }

    @Override
    public Optional<BlockchainRecord> getRecord(String evidenceId) {
        Optional<BlockchainRecord> localOpt = repository.findByEvidenceId(evidenceId);

        try {
            Web3j web3 = getWeb3j();
            Credentials credentials = Credentials.create(privateKey);

            Function function = new Function(
                    "getEvidence",
                    Arrays.asList(new Utf8String(evidenceId)),
                    Arrays.asList(
                            new TypeReference<Utf8String>() {},
                            new TypeReference<Uint256>() {},
                            new TypeReference<Address>() {}
                    )
            );

            String encodedFunction = FunctionEncoder.encode(function);
            EthCall ethCall = web3.ethCall(
                    Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST
            ).send();

            if (!ethCall.hasError() && ethCall.getValue() != null && !ethCall.getValue().equals("0x")) {
                List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                if (!results.isEmpty() && results.size() >= 3) {
                    String onChainFileHash = (String) results.get(0).getValue();
                    BigInteger onChainTimestampSec = (BigInteger) results.get(1).getValue();

                    if (onChainTimestampSec.compareTo(BigInteger.ZERO) > 0) {
                        LocalDateTime onChainTime = LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(onChainTimestampSec.longValue()),
                                ZoneId.systemDefault()
                        );

                        if (localOpt.isPresent()) {
                            BlockchainRecord rec = localOpt.get();
                            BlockchainRecord verifiedRecord = new BlockchainRecord(
                                    rec.getEvidenceId(),
                                    onChainFileHash,
                                    rec.getTransactionHash(),
                                    rec.getStatus()
                            );
                            verifiedRecord.setBlockchainTimestamp(onChainTime);
                            return Optional.of(verifiedRecord);
                        } else {
                            BlockchainRecord record = new BlockchainRecord(
                                    evidenceId,
                                    onChainFileHash,
                                    "0x-on-chain-read",
                                    "CONFIRMED"
                            );
                            record.setBlockchainTimestamp(onChainTime);
                            return Optional.of(record);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not query Polygon Amoy smart contract directly for evidence ID {}: {}", evidenceId, e.getMessage());
        }

        return localOpt;
    }

    private BlockchainRecord createAndSaveFailedRecord(String evidenceId, String fileHash, String status) {
        return createAndSaveFailedRecord(evidenceId, fileHash, "0x-failed-" + System.currentTimeMillis(), status);
    }

    private BlockchainRecord createAndSaveFailedRecord(String evidenceId, String fileHash, String txHash, String status) {
        BlockchainRecord record = new BlockchainRecord(
                evidenceId,
                fileHash,
                txHash,
                status
        );
        return repository.save(record);
    }
}
