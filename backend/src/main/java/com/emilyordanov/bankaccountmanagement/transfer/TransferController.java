package com.emilyordanov.bankaccountmanagement.transfer;

import com.emilyordanov.bankaccountmanagement.transfer.dto.TransferCreateRequest;
import com.emilyordanov.bankaccountmanagement.transfer.dto.TransferOperationResponse;
import com.emilyordanov.bankaccountmanagement.transfer.dto.TransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for transfers.
 * <p>
 * Endpoints:
 * <p>
 * POST /api/transfers
 * - creates a new transfer between two accounts
 * <p>
 * GET /api/transfers/{id}
 * - returns one transfer row by id
 * <p>
 * GET /api/accounts/{accountId}/transfers
 * - returns transfer history for a specific account
 */
@RestController
@RequestMapping("/api")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Creates a new bank transfer.
     * <p>
     * Request body example:
     * <p>
     * {
     * "accountId": 1,
     * "beneficiaryAccountId": 2,
     * "amount": 100.00
     * }
     */
    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferOperationResponse createTransfer(@Valid @RequestBody TransferCreateRequest request) {
        return transferService.createTransfer(request);
    }

    /**
     * Returns one transfer row by id.
     */
    @GetMapping("/transfers/{id}")
    public TransferResponse getTransferById(@PathVariable Long id) {
        return transferService.getTransferById(id);
    }

    /**
     * Returns transfer history for one account.
     * <p>
     * This endpoint is intentionally nested under accounts because
     * the user story says:
     * <p>
     * "As a user, you should be able to see a list of all transfers
     * for certain account."
     */
    @GetMapping("/accounts/{accountId}/transfers")
    public List<TransferResponse> getTransfersByAccountId(@PathVariable Long accountId) {
        return transferService.getTransfersByAccountId(accountId);
    }
}
