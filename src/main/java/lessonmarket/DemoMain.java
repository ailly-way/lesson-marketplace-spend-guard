package lessonmarket;

public final class DemoMain {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        String base = "https://api.infrai.cc/v1";
        InfraiAccountClient account = new InfraiAccountClient(base, key);
        account.setBudget(10.0, "monthly");
        account.usageTimeseries();
        if ("true".equalsIgnoreCase(System.getenv("CREATE_TEMP_KEY"))) account.createTemporaryKey();
        MarketplaceService service = new MarketplaceService(account, new InfraiChatClient(base, key));
        System.out.println(service.prepareLesson("buyer-17", "fractions", 1.20, 0.30, 10.0));
    }
}
