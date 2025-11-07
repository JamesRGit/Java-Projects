import java.util.List;

/**
 * DAO (Data Access Object) Pattern Interface
 * Defines the contract for account persistence.
 */
public interface AccountRepository {
    /**
     * Saves all account data to persistence.
     * @param account The account to save.
     */
    void saveAccount(Account account);

    /**
     * Loads a single account from persistence.
     * @param accountId The ID of the account to load.
     * @return The Account object, or null if not found.
     */
    Account loadAccount(String accountId);

    /**
     * Gets a list of all account IDs available in persistence.
     * @return A list of account ID strings.
     */
    List<String> getAllAccountIds();
}