package com.org.app.dcas.service;

import com.org.app.dcas.model.SalesTransaction;
import com.org.app.dcas.model.Distributor;
import com.org.app.dcas.repository.SalesTransactionRepository;
import com.org.app.dcas.repository.DistributorRepository;
import com.org.app.dcas.dto.SalesTransactionResponse;
import com.org.app.dcas.dto.SalesTransactionListResponse;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesTransactionService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final DistributorRepository distributorRepository;

    public SalesTransactionService(SalesTransactionRepository salesTransactionRepository,
                                  DistributorRepository distributorRepository) {
        this.salesTransactionRepository = salesTransactionRepository;
        this.distributorRepository = distributorRepository;
    }

    public List<SalesTransaction> getAllByDistributor(Long distributorId) {
        return salesTransactionRepository.findByDistributorDistributorId(distributorId);
    }

    public List<SalesTransaction> getMatchedByDistributor(Long distributorId) {
        return salesTransactionRepository.findByDistributorDistributorIdAndIsMatchedTrue(distributorId);
    }

    public List<SalesTransaction> getUnmatchedByDistributor(Long distributorId) {
        return salesTransactionRepository.findByDistributorDistributorIdAndIsMatchedFalse(distributorId);
    }

    public SalesTransactionListResponse getSalesTransactionsByDistributor(Long distributorId, String matched) {
        List<SalesTransaction> txs;
        switch (matched.toUpperCase()) {
            case "ALL":
                txs = getAllByDistributor(distributorId);
                break;
            case "MATCHED":
                txs = getMatchedByDistributor(distributorId);
                break;
            case "UNMATCHED":
                txs = getUnmatchedByDistributor(distributorId);
                break;
            default:
                throw new IllegalArgumentException("Invalid matched value. Use ALL, MATCHED, or UNMATCHED.");
        }
        Distributor distributor = distributorRepository.findById(distributorId).orElse(null);
        List<SalesTransactionResponse> transactions = txs.stream()
            .map(SalesTransactionResponse::from)
            .collect(Collectors.toList());
        return new SalesTransactionListResponse(distributor, transactions);
    }
}
