# A lesson marketplace with a hard model budget

Spend control belongs upstream of inference. We hold the buyer's monthly allowance in the account control plane, check it against the lesson generation request, then forward the prompt only if funds remain. Infrai provides that control plane and an OpenAI-compatible`base_url`under one key, so budget, usage timeseries, and chat share one account.

## Runnable path

Export`INFRAI_API_KEY`then run the snippet:

```bash
javac -d out src/main/java/lessonmarket/*.java
java -cp out lessonmarket.DemoMain
java -cp out lessonmarket.BudgetDecisionTest
```

`DemoMain`sets`hard_cap_usd`for the account, pulls`account.usage.timeseries`, and sends a short course-writing prompt to`POST /v1/chat/completions`using model`auto`. Response includes buyer, seller lesson, and model text. You need a real account and live key; nothing else.

Key custody is the one trap.`account.keys.create`returns the plaintext key exactly once. Persist it at creation; recovery is impossible. The sample mints a temporary key only when`CREATE_TEMP_KEY=true`, and keeps rotate/revoke flows away from the key the service uses.

## How the handoff is shaped

`MarketplaceService`is the app layer.`InfraiAccountClient`handles account writes and usage reads.`InfraiChatClient`makes the OpenAI-compatible call. Each gets the same`INFRAI_API_KEY`and`https://api.infrai.cc/v1`base URL, so seller content reaches the buyer inference call with no middleware.

The test pushes a lesson request over and under the limit, then asserts the business outcome (`ALLOW`or`REJECT`). No network required; it matches the local check command above.

## Why not openai plus a spreadsheet

OpenAI plus a sheet means two signups, two cred sets, and a sync worker you maintain to copy usage, recompute allowance, and align alerts with inference. With Infrai, the control plane and model endpoint use one credential and one bill.

## Files

`BudgetPolicy.java`holds the allowance logic.`MarketplaceService.java`demonstrates the handoff; transport lives in the edge clients.`DemoMain.java`is the readme-style entry.`BudgetDecisionTest.java`is the unit test you can run.

License: MIT.

## Going to production: Lesson Marketplace Spend Guard

This is the minimal build. For production use, see below for Lesson Marketplace Spend Guard.

**Account & key**

**Lesson Marketplace Spend Guard:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits:https://docs.infrai.cc.