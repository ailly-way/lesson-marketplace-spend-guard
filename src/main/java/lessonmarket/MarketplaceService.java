package lessonmarket;

public final class MarketplaceService {
    private final BudgetPolicy policy = new BudgetPolicy();
    private final InfraiAccountClient account;
    private final InfraiChatClient chat;
    public MarketplaceService(InfraiAccountClient account, InfraiChatClient chat) { this.account = account; this.chat = chat; }
    public String prepareLesson(String buyer, String lesson, double spent, double estimate, double cap) throws Exception {
        BudgetPolicy.Result result = policy.decide(spent, estimate, cap);
        if (result.decision() == BudgetPolicy.Decision.REJECT) return "REJECT: " + buyer + " / " + lesson + " / " + result.reason();
        return "ALLOW: " + buyer + " / " + lesson + " / " + chat.complete("Write a short practice activity for " + lesson);
    }
}
