package lessonmarket;

public final class BudgetPolicy {
    public enum Decision { ALLOW, REJECT }
    public record Result(Decision decision, String reason) {}

    public Result decide(double spentUsd, double requestedUsd, double hardCapUsd) {
        if (requestedUsd < 0 || spentUsd < 0 || hardCapUsd < 0) {
            return new Result(Decision.REJECT, "amounts must be non-negative");
        }
        if (spentUsd + requestedUsd > hardCapUsd) {
            return new Result(Decision.REJECT, "lesson would exceed hard cap");
        }
        return new Result(Decision.ALLOW, "within hard cap");
    }
}
