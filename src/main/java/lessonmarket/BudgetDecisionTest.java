package lessonmarket;

public final class BudgetDecisionTest {
    public static void main(String[] args) {
        BudgetPolicy policy = new BudgetPolicy();
        if (policy.decide(2.0, 1.0, 5.0).decision() != BudgetPolicy.Decision.ALLOW) throw new AssertionError();
        if (policy.decide(4.5, 1.0, 5.0).decision() != BudgetPolicy.Decision.REJECT) throw new AssertionError();
        System.out.println("BudgetDecisionTest passed");
    }
}
