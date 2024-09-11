package com.mortisdevelopment.mortisbank.transactions;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;

import com.mortisdevelopment.mortiscore.messages.Messages;
import com.mortisdevelopment.mortiscore.placeholder.Placeholder;
import com.mortisdevelopment.mortiscore.placeholder.methods.ClassicPlaceholderMethod;
import com.mortisdevelopment.mortiscore.utils.ColorUtils;
import com.mortisdevelopment.mortiscore.utils.NumberUtils;
import com.mortisdevelopment.mortiscore.utils.TimeUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Getter
public class Transaction {

    public enum TransactionType {
        DEPOSIT,
        WITHDRAW
    }
    private final String id;
    private final UUID uniqueId;
    private final String rawTransaction;

    protected Transaction(String id, UUID uniqueId, String rawTransaction) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.rawTransaction = rawTransaction;
    }

    public Transaction(UUID uniqueId, Messages messages, @NotNull TransactionType type, double amount, @NotNull LocalDateTime time, @NotNull String transactor) {
        this.id = UUID.randomUUID().toString();
        this.uniqueId = uniqueId;
        this.rawTransaction = serialize(messages, type, amount, time, transactor);
    }

    public Transaction(UUID uniqueId, Messages messages, @NotNull TransactionType type, double amount, @NotNull String transactor) {
        this(uniqueId, messages, type, amount, LocalDateTime.now(), transactor);
    }

    private String serialize(Messages messages, @NotNull TransactionType type, double amount, @NotNull LocalDateTime time, @NotNull String transactor) {
        String message = switch (type) {
            case DEPOSIT -> messages.getSimpleMessage("deposit_transaction");
            case WITHDRAW -> messages.getSimpleMessage("withdraw_transaction");
        };
        ClassicPlaceholderMethod method = new ClassicPlaceholderMethod();
        method.addReplacement("%amount%", NumberUtils.format(amount));
        method.addReplacement("%transactor%", transactor);
        method.addReplacement("%time%", "%time%" + time + "%time%");
        return new Placeholder(method).setPlaceholders(message);
    }

    public LocalDateTime getTime() {
        return getTime(rawTransaction);
    }

    private String getDuration(Messages messages) {
        LocalDateTime time = getTime();
        if (time == null) {
            return null;
        }
        return TimeUtils.getTime(time, LocalDateTime.now(),
                " " + messages.getSimpleMessage("years"),
                " " + messages.getSimpleMessage("months"),
                " " + messages.getSimpleMessage("days"),
                " " + messages.getSimpleMessage("hours"),
                " " + messages.getSimpleMessage("minutes"),
                " " + messages.getSimpleMessage("seconds"));
    }

    public String getTransactionMessage(Messages messages) {
        return ColorUtils.color(new Placeholder(new ClassicPlaceholderMethod("%time%", getDuration(messages))).setPlaceholders(rawTransaction));
    }

    public static Transaction deserialize(String id, UUID uniqueId, String rawTransaction) {
        LocalDateTime time = getTime(rawTransaction);
        if (time == null) {
            return null;
        }
        return new Transaction(id, uniqueId, rawTransaction);
    }

    private static LocalDateTime getTime(String serializedString) {
        String time = StringUtils.substringBetween(serializedString, "%time%", "%time%");
        if (time == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(time);
        }catch (DateTimeParseException exp) {
            return null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uniqueId, rawTransaction);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Transaction that = (Transaction) object;
        return Objects.equals(id, that.id) &&
                Objects.equals(uniqueId, that.uniqueId) &&
                Objects.equals(rawTransaction, that.rawTransaction);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", uniqueId=" + uniqueId +
                ", rawTransaction='" + rawTransaction + '\'' +
                '}';
    }
}