package sessionapp.models;

public record Transaction(Money sum, String category, TransactionType type, Long ts) {}
