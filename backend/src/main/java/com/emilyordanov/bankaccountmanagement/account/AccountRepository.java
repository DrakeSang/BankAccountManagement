package com.emilyordanov.bankaccountmanagement.account;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Account entities.
 *
 * JpaRepository gives us common database operations:
 * - findAll()
 * - findById()
 * - save()
 * - delete()
 * - existsById()
 *
 * Spring Data JPA can also generate queries automatically from method names.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Checks if an account with the given name already exists.
     *
     * Used during account creation because account names must be unique.
     */
    boolean existsByName(String name);

    /**
     * Checks if an account with the given IBAN already exists.
     *
     * Used during account creation because IBANs must be unique.
     */
    boolean existsByIban(String iban);

    /**
     * Checks if another account already uses the same name.
     *
     * This is used during update.
     *
     * Why do we need "AndIdNot"?
     *
     * Example:
     * Account 1 has name "Main Account".
     * We update Account 1 and keep the same name.
     *
     * existsByName("Main Account") would return true,
     * because Account 1 itself already has that name.
     *
     * But this should not be treated as duplicate.
     *
     * existsByNameAndIdNot("Main Account", 1)
     * means:
     * "Is there an account with this name, but with id different from 1?"
     *
     * If yes -> another account uses the name -> duplicate.
     * If no  -> only the current account uses it -> allowed.
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * Same logic as existsByNameAndIdNot, but for IBAN.
     *
     * Used during update so the account can keep its own IBAN,
     * but cannot take an IBAN that belongs to another account.
     */
    boolean existsByIbanAndIdNot(String iban, Long id);
}
