# TJ's Social Strategy Research

## Purpose

TJ's Social Strategy is a simplified blackjack strategy designed for casual and social players. The goal is to preserve nearly all of Basic Strategy's value while making the rules easier to remember, less intimidating to use at a live table, and less dependent on memorizing isolated exceptions.

The strategy was developed by comparing candidate simplifications against perfect Basic Strategy using paired Monte Carlo simulation.

## Research Baseline

Unless otherwise noted, direct-decision research used:

- 8 decks
- Dealer hits soft 17 (H17)
- Double after split (DAS)
- Late surrender
- 6:5 blackjack
- $25 reference bet
- 160-hand reference session

Final session-level validation was run under both 6:5 and 3:2 blackjack.

---

# Final Social Strategy v1

## Mental Order

1. Check surrender.
2. Check pairs.
3. Check soft hands.
4. Otherwise use the hard-hand rules.

## Surrender

Surrender any hard 15 or hard 16 against a dealer 10.

Otherwise, do not surrender.

This rule intentionally includes 8,8 vs 10.

## Pairs

After checking surrender first:

- A,A: Split.
- 10,10: Stand.
- 9,9: Split vs 2-9; otherwise stand.
- 8,8: Split.
- 7,7: Split vs 2-7; otherwise hit.
- 6,6: Split vs 2-7; otherwise hit.
- 5,5: Never split; play as hard 10.
- 4,4: Never split; hit.
- 3,3: Split vs 2-7; otherwise hit.
- 2,2: Split vs 2-7; otherwise hit.

Because surrender is checked first, 8,8 vs 10 is surrendered rather than split.

## Soft Hands

- Soft 19+: Stand.
- Soft 18:
  - Double vs 4-6.
  - Hit vs 9-Ace.
  - Otherwise stand.
- Soft 17 or lower:
  - Double vs 4-6.
  - Otherwise hit.

If a double is unavailable:
- Soft 18 falls back to stand.
- Soft 17 or lower falls back to hit.

## Hard Hands

- Hard 17+: Stand.
- Hard 12-16:
  - Stand vs 2-6.
  - Hit otherwise, subject to the surrender rule above.
- Hard 11: Double; otherwise hit.
- Hard 10:
  - Double vs 2-9.
  - Hit vs 10 or Ace.
- Hard 9:
  - Double vs 2-6.
  - Hit otherwise.
- Hard 8 or lower: Hit.

---

# Final Whole-Policy Validation

**Status: ACCEPTED — Social Strategy v1 frozen**

TJ's Social Strategy was tested directly against perfect Basic Strategy over **1,000,000 paired 160-hand sessions**, using an 8-deck H17 DAS late-surrender game, a $500 starting bankroll, and a $25 base bet.

The simulation used the production `BasicStrategy` and `SocialStrategy` classes for every decision throughout each session, including later hit decisions and split hands.

## 6:5 Blackjack

- Basic Strategy average ending bankroll: **$428.82**
- Social Strategy average ending bankroll: **$425.74**
- Social vs Basic difference: **-$3.08/session**
- 95% CI: **-$3.62 to -$2.54**
- Social finishes with more money: **47.676%**
- Same ending bankroll: **4.296%**
- Basic finishes with more money: **48.027%**
- Basic head-to-head advantage: **0.351 percentage points**
- Social net survival difference: **-0.393 percentage points**
- Social wager exposure difference: **-$11.30/session**

## 3:2 Blackjack

- Basic Strategy average ending bankroll: **$478.26**
- Social Strategy average ending bankroll: **$475.09**
- Social vs Basic difference: **-$3.17/session**
- 95% CI: **-$3.72 to -$2.61**
- Social finishes with more money: **45.937%**
- Same ending bankroll: **7.883%**
- Basic finishes with more money: **46.181%**
- Basic head-to-head advantage: **0.244 percentage points**
- Social net survival difference: **-0.329 percentage points**
- Social wager exposure difference: **-$10.54/session**

## Human Interpretation

Social Strategy gives up only about **$3 over a 160-hand session at a $25 base bet** compared with perfect Basic Strategy.

Perfect Basic Strategy finishes ahead only about **0.2-0.4 percentage points more often** than Social Strategy finishes ahead.

This is the strongest overall validation of the project: the simplified rules are measurably worse in EV, as expected, but the practical gap is very small relative to the amount wagered and the amount of memorization removed.

Canonical research file:

- `paired-basic-vs-social-strategy-1m-160hands-500bankroll-25bet.csv`

---

# Accepted Simplifications

## Hard 12 vs Dealer 2/3

### Social Rule

With hard 12-16, stand against dealer 2-6.

### Basic Strategy Exceptions Removed

- Hard 12 vs 2: Hit.
- Hard 12 vs 3: Hit.

### Frequency

- **2.028 occurrences per 160-hand session**

### Paired Outcome Results

- Basic Strategy better: **27.568%**
- Same monetary result: **46.938%**
- Social Strategy better: **25.494%**
- Basic Strategy head-to-head advantage: **2.073 percentage points**

### EV

- Cost per occurrence at $25: **$0.575**
- Cost per 160-hand session: **$1.166**
- 95% CI: **$1.105-$1.227/session**

### Human Interpretation

Perfect Basic Strategy has two exceptions: hit 12 against 2 or 3. Those situations come up only about twice in a 160-hand session. At a $25 average bet, ignoring both exceptions costs about **$1.17 in expected value per session**.

When one of these hands occurs, hitting produces a better result about **27.6%** of the time, standing does better about **25.5%**, and about **46.9%** of the time the eventual monetary result is the same.

**In other words, hitting beats standing only about 2.1 percentage points more often than standing beats hitting.**

Do not describe this as "Basic Strategy only matters 2.1% of the time." The outcomes differ much more often; 2.1 percentage points is the net head-to-head advantage.

**Status: ACCEPTED**

Canonical research files:

- `frequency-basicstrategy-compositions-8d-h17-das-ls-6to5-10m-rounds.csv`
- `paired-hard12-vs2to3-8d-h17-das-ls-6to5-1m.csv`

---

## Hard 9 vs Dealer 2

### Social Rule

Double hard 9 against dealer 2-6.

### Basic Strategy Exception Removed

Hit hard 9 against dealer 2.

### Direct Paired-Hand Result

- Basic Strategy (HIT) better: **44.820%**
- Same monetary result: **7.101%**
- Social Strategy (DOUBLE) better: **48.079%**
- Social head-to-head advantage: **3.259 percentage points**
- Estimated direct EV cost: about **$0.10/session** at a $25 base bet

### 160-Hand Session Validation

3:2 blackjack:

- Social vs Basic bankroll difference: **-$0.159**
- 95% CI: **-$0.438 to +$0.120**
- Social finishes with more money: **15.959%**
- Same ending bankroll: **71.027%**
- Basic finishes with more money: **13.014%**
- Net survival difference: **-0.202 percentage points**
- Extra hard-9 doubles: **0.451/session**
- Extra wager exposure: **$7.48/session**

6:5 blackjack:

- Social vs Basic bankroll difference: **+$0.025**
- 95% CI: **-$0.246 to +$0.295**
- Social finishes with more money: **15.678%**
- Same ending bankroll: **71.742%**
- Basic finishes with more money: **12.580%**
- Net survival difference: **-0.103 percentage points**
- Extra hard-9 doubles: **0.444/session**
- Extra wager exposure: **$7.60/session**

### Human Interpretation

The dealer-2 exception is not worth memorizing for Social Strategy. Expanding the hard-9 double range from 3-6 to **2-6** produces essentially indistinguishable session EV from Basic Strategy while creating a cleaner rule.

Interestingly, Social's double produces the better monetary result more often in the direct paired comparison, even though Basic retains a tiny EV edge.

**Status: ACCEPTED**

Canonical research file:

- `paired-session-policy-hard9-100k-160hands-500bankroll-25bet.csv`

---

## Hard 10

### Final Social Rule

- Double hard 10 vs 2-9.
- Hit vs 10 or Ace.

This matches Basic Strategy.

### Rejected Candidate: Never Double Hard 10

3:2:

- Average bankroll difference vs Basic: **-$19.489/session**
- 95% CI: **-$20.241 to -$18.738**
- Net survival difference: **-0.175 percentage points**

6:5:

- Average bankroll difference vs Basic: **-$19.029/session**
- 95% CI: **-$19.758 to -$18.300**
- Net survival difference: **-0.264 percentage points**

Removing hard-10 doubles gives away substantial EV without meaningfully improving session survival.

### Rejected Candidate: Always Double Hard 10

3:2:

- Average bankroll difference vs Basic: **-$1.749/session**
- 95% CI: **-$2.574 to -$0.925**
- Net survival difference: **-0.720 percentage points**
- Extra hard-10 doubles: **2.094/session**
- Extra wager exposure: **$36.42/session**

6:5:

- Average bankroll difference vs Basic: **-$1.597/session**
- 95% CI: **-$2.396 to -$0.798**
- Net survival difference: **-0.820 percentage points**
- Extra hard-10 doubles: **2.054/session**
- Extra wager exposure: **$32.83/session**

Always doubling can finish ahead more often while still having lower EV because the losing double-down outcomes are larger.

It is also a conspicuously nonstandard play against dealer 10/A and is more likely to attract comments or make a novice player uncomfortable.

**Status: KEEP BASIC STRATEGY RULE**

---

## Soft Hands

### Social Rule

- Soft 19+: Stand.
- Soft 18: Double vs 4-6; hit vs 9-Ace; otherwise stand.
- Soft 17 or lower: Double vs 4-6; otherwise hit.

### Whole-Policy Validation

1,000,000 paired 160-hand sessions, $500 starting bankroll, $25 base bet.

6:5:

- Average bankroll difference vs Basic: **-$0.406/session**
- 95% CI: **-$0.617 to -$0.196**
- Net survival difference: **+0.038 percentage points**
- Wager exposure difference: **-$6.03/session**
- Soft doubles difference: **-0.291/session**

3:2:

- Average bankroll difference vs Basic: **-$0.407/session**
- 95% CI: **-$0.624 to -$0.191**
- Net survival difference: **+0.042 percentage points**
- Wager exposure difference: **-$6.24/session**
- Soft doubles difference: **-0.296/session**

### Human Interpretation

The simplified soft-hand system sacrifices only about **$0.41 over a 160-hand $25 session** while leaving bankroll survival essentially unchanged.

It replaces several irregular Basic Strategy soft-double boundaries with one much easier pattern: **4-6 is the double range**.

**Status: ACCEPTED**

### Individual Soft-Hand Deviations

#### A,8 vs 6 — STAND instead of DOUBLE

Purpose: preserves the simple rule **"Soft 19+ stands."**

- Basic better: **59.380%**
- Same: **1.090%**
- Social better: **39.529%**
- EV cost: **0.009991 units**
- At $25: about **$0.25 per occurrence**

Human interpretation: Basic doubles here, but the EV edge is surprisingly small. Social stands so the player can treat every soft 19+ the same way.

#### A,7 vs 2 — STAND instead of DOUBLE

Purpose: keeps Soft 18 centered on doubling only against 4-6.

- Basic better: **51.315%**
- Same: **1.664%**
- Social better: **47.021%**
- EV cost: **0.001743 units**
- At $25: about **$0.04 per occurrence**

Human interpretation: Basic's edge is extremely small here, making this an inexpensive exception to remove.

#### A,7 vs 3 — STAND instead of DOUBLE

Purpose: same simplification as A,7 vs 2.

- Basic better: **52.605%**
- Same: **1.516%**
- Social better: **45.879%**
- EV cost: **0.032865 units**
- At $25: about **$0.82 per occurrence**

#### A,6 vs 3 — HIT instead of DOUBLE

Purpose: keeps the rule **"Soft 17 or lower doubles only against 4-6."**

- Basic better: **47.572%**
- Same: **7.635%**
- Social better: **44.793%**
- EV cost: **0.026363 units**
- At $25: about **$0.66 per occurrence**

#### A,2 vs 4 — DOUBLE instead of HIT

Purpose: lets every Soft-17-or-lower hand use the same 4-6 doubling range.

- Basic better: **46.447%**
- Same: **3.818%**
- Social better: **49.735%**
- EV cost: **0.036446 units**
- At $25: about **$0.91 per occurrence**

Human interpretation: Social actually produces the better monetary outcome more often, but Basic retains slightly higher EV because the sizes of the favorable and unfavorable outcomes differ.

#### A,3 vs 4 — DOUBLE instead of HIT

Purpose: same unified 4-6 doubling rule.

- Basic better: **46.468%**
- Same: **3.761%**
- Social better: **49.771%**
- EV cost: **0.014171 units**
- At $25: about **$0.35 per occurrence**

Human interpretation: again, Social wins the head-to-head outcome comparison more often while Basic retains slightly higher EV.

Canonical research file:

- `paired-session-refined-soft-8d-h17-das-ls-1m.csv`

---

## Pairs

### Social Rule

After surrender is checked first:

- Split Aces.
- Split 8s.
- Split 9s vs 2-9.
- Split 2s, 3s, 6s, and 7s vs 2-7.
- Never split 4s, 5s, or 10s.

Remember: **8,8 vs 10 is surrendered before the pair rule is reached.**

### Intentional Pair Deviations from Basic Strategy

- 9,9 vs 7: SPLIT instead of STAND.
- 6,6 vs 7: SPLIT instead of HIT.
- 4,4 vs 5: HIT instead of SPLIT.
- 4,4 vs 6: HIT instead of SPLIT.

### Direct Paired Evidence

#### 9,9 vs 7 — SPLIT instead of STAND

- EV cost: **0.027571 units**
- At $25: about **$0.69 per occurrence**
- Frequency: about **0.071/session**
- Expected session cost: about **$0.05**

#### 6,6 vs 7 — SPLIT instead of HIT

- EV cost: **0.033233 units**
- At $25: about **$0.83 per occurrence**
- Frequency: about **0.070/session**
- Expected session cost: about **$0.06**

#### 4,4 vs 5 — HIT instead of SPLIT

- EV cost: **0.026900 units**
- At $25: about **$0.67 per occurrence**
- Frequency: about **0.072/session**
- Expected session cost: about **$0.05**

#### 4,4 vs 6 — HIT instead of SPLIT

- EV cost: **0.093182 units**
- At $25: about **$2.33 per occurrence**
- Frequency: about **0.071/session**
- Expected session cost: about **$0.17**

Combined expected cost of the four intentional pair deviations is about **$0.32/session** at a $25 base bet.

### Whole-Policy Validation

1,000,000 paired 160-hand sessions.

6:5:

- Average bankroll difference: **-$0.427/session**
- 95% CI: **-$0.592 to -$0.262**
- Net survival difference: **+0.010 percentage points**

3:2:

- Average bankroll difference: **-$0.399/session**
- 95% CI: **-$0.570 to -$0.229**
- Net survival difference: **+0.030 percentage points**

### Human Interpretation

The simplified pair policy costs only about **$0.40 per 160-hand $25 session** while leaving bankroll risk essentially unchanged.

The largest individual sacrifice is 4,4 vs 6, but that situation appears only about **0.07 times per session**.

**Status: ACCEPTED**

Canonical research files:

- `paired-pair-deviations-8d-h17-das-ls-6to5-1m.csv`
- `paired-session-social-pairs-8d-h17-das-ls-1m.csv`

---

## Surrender

### Social Rule

Check surrender before pair strategy.

**Surrender any hard 15 or hard 16 against dealer 10, including 8,8. Otherwise, do not surrender.**

### Final 1M-Session Validation

6:5:

- Social vs Basic difference: about **-$0.78/session**
- Net survival difference: about **-0.19 percentage points**

3:2:

- Social vs Basic difference: about **-$0.77/session**
- Net survival difference: about **-0.14 percentage points**

Compared with never surrendering, this one-line rule retains roughly three-quarters of Basic Strategy surrender's EV benefit.

### Direct Deviation Evidence

#### Hard 15 vs 10 — KEEP SURRENDER

- Frequency: **3.218/session**
- Surrender EV advantage over hit: **0.003290 units**
- At $25: about **$0.08 per occurrence**
- Expected value preserved: about **$0.26/session**

Surrender is only slightly better than hitting on each occurrence, but the situation is common and costs no extra mental complexity because it shares the same rule as hard 16 vs 10.

#### Hard 16 vs 10 — KEEP SURRENDER

- Frequency: **2.685/session**
- Surrender EV advantage over hit: **0.035687 units**
- At $25: about **$0.89 per occurrence**
- Expected value preserved: about **$2.40/session**

This is the strongest reason to keep surrender in Social Strategy.

#### Hard 15 vs Ace — HIT instead of SURRENDER

- Frequency: **0.607/session**
- EV cost: about **$0.19 per occurrence**
- Expected session cost: about **$0.12**

#### Hard 16 vs 9 — HIT instead of SURRENDER

- Frequency: **0.728/session**
- EV cost: about **$0.14 per occurrence**
- Expected session cost: about **$0.10**

#### Hard 16 vs Ace — HIT instead of SURRENDER

- Frequency: **0.508/session**
- EV cost: about **$1.00 per occurrence**
- Expected session cost: about **$0.51**

This is the most expensive surrender exception Social removes, but retaining it would make the surrender rule substantially less memorable.

#### Hard 17 vs Ace — STAND instead of SURRENDER

- Frequency: **0.505/session**
- EV cost: about **$0.29 per occurrence**
- Expected session cost: about **$0.15**

#### 8,8 vs Ace — SPLIT instead of SURRENDER

- Frequency: **0.049/session**
- EV cost: about **$0.41 per occurrence**
- Expected session cost: about **$0.02**

The exception is so rare that its session-level cost is negligible.

#### 8,8 vs 10 — SURRENDER instead of SPLIT

- Frequency: **0.259/session**
- Split better monetary result: **46.698%**
- Surrender better monetary result: **53.302%**
- EV cost: **0.027307 units**
- At $25: about **$0.68 per occurrence**
- Expected session cost: about **$0.18**

### Human Interpretation: 8,8 vs 10

Perfect Basic Strategy splits, but Social Strategy applies the much easier surrender-first rule: **hard 15 or 16 against a 10 -> surrender.**

Surrender actually produces the better monetary result **53.3%** of the time, while splitting does better **46.7%** of the time.

**In other words, surrender gets the better result about 6.6 percentage points more often, even though splitting retains higher expected value because its successful outcomes have more upside.**

This is an unusually strong Social Strategy simplification: it removes an awkward exception, reduces volatility, and costs only about **$0.18/session**.

**Status: ACCEPTED**

Canonical research files:

- final surrender-policy session validation CSV
- paired surrender-deviation CSV

---

# Research History and Rejected Alternatives

The entries below were useful during development but are not part of the final strategy.

## Never Surrender

Rejected.

Over 1,000,000 paired sessions:

6:5:
- Never surrender vs Basic: about **-$2.89/session**
- Survival difference: about **-0.78 percentage points**

3:2:
- Never surrender vs Basic: about **-$3.09/session**
- Survival difference: about **-0.86 percentage points**

Conclusion: removing surrender entirely gives away too much value for too little benefit.

## Aggressive Surrender

A broad rule that surrendered hard 15-17 against 9-Ace was rejected.

It reduced volatility somewhat but cost roughly **$7.5-$7.6/session** versus Basic Strategy.

## Ultra-Simple Soft Policy

Rejected.

It cost roughly **$1.6-$1.7/session more** than the accepted refined soft policy without providing enough additional simplicity.

## Hard 9: Never Double

Rejected.

It improved early-session survival only slightly while costing roughly **$3.23/session**.

## Hard 10: Never Double / Always Double

Both rejected.

The accepted Social Strategy keeps Basic Strategy's hard-10 rule.

---

# Remaining Product Work

Research for Social Strategy v1 is complete.

Next steps:

1. Build the deviation-explanation catalog used by the app.
2. Add Social Strategy support to Decision Help / the `?` dialog.
3. Show Basic vs Social comparisons when the two strategies differ.
4. Add concise "Pro Tip" explanations using the human-readable interpretations above.
5. Create a compact printable Social Strategy cheat sheet / casino guide.
6. Keep all app recommendations, explanations, and printable materials driven by the same strategy definitions so they cannot drift apart.
