# Ace Awareness / Social Advantage Research — Final Findings

## Executive Conclusion

For the intended social blackjack player, **Ace-only awareness is more compelling than Ace/Five wager spreading**.

The strongest current product concept is:

1. Play **Social Strategy** or **Basic Strategy** for hand decisions.
2. Track **Aces only** as an optional advanced layer.
3. Near the midpoint of an 8-deck shoe, use the Ace count as a **table-awareness signal**:
   - Ace surplus ≥ 0 → stay.
   - Ace surplus < 0 → the remaining shoe is Ace-poor; consider switching tables if the social cost of moving is low.
4. When the shoe becomes strongly Ace-rich, use the same signal for the **$5 Super 4 side bet**.
5. Under the tested $650,000 major progressive assumption, **Ace surplus +4 or higher is the current balanced social trigger** for Super 4.

The important word is *balanced*. Lower thresholds can produce more long-run expected value because they generate more side bets, but they also worsen typical-session bankroll outcomes because the progressive jackpot is extremely rare.

---

## 1. Product Philosophy

The goal is not necessarily to beat blackjack overall.

The goal is to identify improvements that are worth their:

- mental effort,
- bankroll risk,
- social disruption,
- and practical complexity.

The product hierarchy remains:

**Social Strategy → Basic Strategy → optional Social Advantage tools**

Social Strategy is the low-effort hand strategy.

Basic Strategy remains the mathematically stronger fixed strategy.

Ace Awareness is the current preferred optional advantage layer because one simple signal can support multiple useful decisions without requiring a traditional full card count or an aggressive bet spread.

---

## 2. Why Ace/Five Betting Is Not the Primary Recommendation

### The Ace/Five count

Ace/Five is a simple running count:

- 5 = +1
- Ace = -1
- everything else = 0
- start each shoe at 0

The signal itself is real. Earlier 50-million-round calibration showed that higher Ace/Five counts correspond to better future blackjack EV.

Examples from the earlier research included:

- RC +2 exact: about -0.17% EV
- RC +4 exact: about +0.15% EV
- RC +5 exact: about +0.37% EV
- RC +8 exact: about +1.03% EV

So Ace/Five contains useful information.

### The problem is bankroll exploitation

With a large research bankroll and an aggressive spread, Ace/Five improved EV meaningfully.

But with the intended social-player bankroll:

- $500 starting bankroll
- $25 table minimum

the wager-spread versions became unattractive.

Earlier $500-bankroll results:

| Strategy | Avg P/L | Unable to continue |
|---|---:|---:|
| Flat Basic | -$22.29 | ~18.5% |
| Ace/Five 1–2 | -$21.51 | ~32.0% |
| Ace/Five 1–3 | -$21.70 | ~42.5% |
| Ace/Five 1–4 | -$22.00 | ~50.7% |
| Ace/Five 1–8 | -$26.14 | ~67.2% |

The conservative 1–2 spread improved average P/L by only about **$0.78 per 160-hand night** while raising the unable-to-continue rate by roughly **13.5 percentage points**.

That does not pass the intended effort-versus-reward test.

The player would have to:

- count Aces and 5s continuously,
- preserve the count while socializing,
- alter wagers repeatedly,
- accept much more variance,
- and make visible count-correlated bet changes,

for roughly $1/night of expected improvement.

### Ace/Five conclusion

Ace/Five is mathematically valid and remains useful as a research benchmark.

It is **not the preferred social-player recommendation**.

---

## 3. Ace-Only Counting

Ace-only became more attractive once the count was used for **shoe awareness instead of continuous wager spreading**.

Track:

- `S` = number of visible cards seen
- `A` = number of Aces seen

Since Aces make up 1/13 of a normal deck:

`Expected Aces seen = S / 13`

Define:

`Ace surplus = S / 13 - A`

Interpretation:

- **positive surplus** → fewer Aces have appeared than expected → remaining shoe is relatively Ace-rich
- **negative surplus** → more Aces have appeared than expected → remaining shoe is relatively Ace-poor

For the current social rule, no true-count division is required.

---

## 4. Midpoint Table-Switching Rule

The current simple rule is:

> **At roughly half of an 8-deck shoe, evaluate the Ace surplus once.**

At approximately four decks remaining:

- **Ace surplus ≥ 0** → stay.
- **Ace surplus < 0** → the shoe is Ace-poor; consider moving to a fresh table if doing so is socially and practically reasonable.

This is deliberately a recommendation rather than a command.

A player may rationally stay because:

- friends are at the table,
- the dealer/table is enjoyable,
- another seat is unavailable,
- moving is inconvenient,
- or the social experience matters more than a small EV difference.

The count identifies a favorable moment to leave **if the player was already indifferent**.

---

## 5. Ace-Only Table-Switch Simulation

### Research environment

- 8 decks
- dealer hits soft 17
- double after split
- late surrender
- 3:2 blackjack
- $500 starting bankroll
- $25 flat main wager
- 160 intended hands
- Basic Strategy
- 500,000 paired sessions

### Results

#### Flat Basic

- Average P/L: **-$22.29**
- Ending bankroll SD: **$345.61**
- Unable to continue: **18.635%**
- Finish with ≥$400: **58.199%**
- Profitable sessions: **46.342%**

#### Ace-only + midpoint switching

- Average P/L: **-$19.97**
- Ending bankroll SD: **$346.84**
- Unable to continue: **18.553%**
- Finish with ≥$400: **58.418%**
- Profitable sessions: **46.571%**

### Paired improvement

Versus Flat Basic:

- Average P/L improvement: **+$2.326 per 160-hand night**
- 95% CI: **+$1.372 to +$3.279**
- Unable-to-continue difference: **-0.082 percentage points**
- $400+ night difference: **+0.219 percentage points**

The rule averaged:

- **1.555 table switches per session**
- **2.873 midpoint checks per session**

### Table-switch conclusion

This remains one of the strongest social-advantage findings.

It produces a measurable EV improvement without materially worsening session bankroll risk.

---

## 6. Important Table-Switching Caveat

The switching simulation is idealized.

When the model chooses to change tables, it effectively moves immediately to a fresh shoe.

Real casino switching may involve:

- walking,
- finding a table,
- waiting for a seat,
- or simply choosing not to move.

This makes the fixed-hand simulation an upper-bound-style estimate of how cleanly the strategy can be executed.

However, lost playing time is not automatically an EV penalty. In a negative-EV blackjack game, playing fewer hands during a fixed-time casino visit can reduce expected loss.

A future production model could distinguish:

- **fixed-hand sessions**
- **fixed-time sessions**

---

## 7. Why Super 4 Changed the Value of Ace Awareness

The $5 Super 4 side bet is unusually compatible with Ace-only information because dealer blackjack necessarily contains:

- one Ace,
- one ten-value card.

That means an Ace-rich remaining shoe directly affects the event that activates the side bet.

The same count can now support:

1. shoe-quality awareness,
2. table-selection decisions,
3. selective progressive side-bet opportunities.

That makes the mental-effort equation much stronger than it was for Ace-only counting used only to save a small amount on the main game.

---

## 8. Super 4 Modeling Assumptions

The simulations used the following practical payout model:

- Three of a Kind: **$800**
- Straight: **$400**
- Flush: **$300**
- Two Pair: **$200**
- All four cards same color: **$100**
- Pair: **$50**
- Other dealer blackjack: **$25**
- Dealer Ace showing, no blackjack: **$10**
- Otherwise: **loss**

Progressive royal modeling:

- chosen-suit royal: **100% of major progressive**
- another-suit royal: **10% of major progressive**

Because the four suits are symmetric, the expected progressive payout conditional on a royal is:

`0.25 × J + 0.75 × 0.10 × J = 0.325 × J`

where `J` is the major progressive amount.

### Important paytable caveat

An earlier research note referenced an authorized Massachusetts configuration with a **$750 Three of a Kind** payout, while the simulation runners used **$800**.

Therefore the exact live Encore table signage should be verified before treating the simulated paytable as a definitive description of every current physical table.

The qualitative Ace-surplus findings remain useful, but the exact EV numbers depend on the actual live paytable.

---

## 9. 50-Million-Hand Super 4 Density Study

A dedicated 50-million-round simulation tracked the exact pre-deal Ace state and classified Super 4 outcomes.

This was more reliable than trying to estimate progressive EV from only a handful of jackpot hits inside full-session simulations.

### Fixed-payout-only Super 4 edge

As Ace surplus increased, the fixed part of the side bet improved cleanly:

| Ace surplus trigger | Fixed-payout-only edge |
|---|---:|
| +2.0 | -18.39% |
| +2.5 | -15.83% |
| +3.0 | -13.44% |
| +3.5 | -11.16% |
| +4.0 | -8.87% |
| +4.5 | -6.16% |
| +5.0 | -3.26% |
| +5.5 | +0.96% |
| +6.0 | +3.11% |

This was an important validation result.

Even before including the progressive jackpot, the side bet improves steadily as the remaining shoe becomes more Ace-rich.

### Approximate major-jackpot breakevens

Using the symmetric 0.325 × jackpot royal expectation, approximate breakeven major values were:

| Ace surplus trigger | Approx. breakeven major |
|---|---:|
| +2.0 | ~$315K |
| +2.5 | ~$272K |
| +3.0 | ~$211K |
| +3.5 | ~$178K |
| +4.0 | ~$125K |
| +4.5 | ~$77K |
| +5.0 | ~$46K |
| +5.5 | already positive before progressive |

These estimates explain why lower thresholds can become long-run +EV when the progressive is large.

But that is not enough to choose a social strategy.

---

## 10. Why Long-Run +EV Is Not the Whole Decision

A progressive side bet can be positive EV while still being unattractive for a finite-night bankroll.

The reason is simple:

> Most of the positive expectation may come from an extremely rare royal jackpot.

On a normal night, the player is far more likely to experience repeated $5 losses than the jackpot outcome that makes the strategy look attractive in the long-run mean.

Therefore the correct product question is not:

> What is the lowest threshold with positive EV?

It is:

> What threshold offers the best balance of EV, bankroll survival, opportunity frequency, and fun?

---

## 11. Full-Session Super 4 Risk Study

### Environment

The final session-risk study used:

- 500,000 paired sessions
- $500 starting bankroll
- $25 main wager
- $5 Super 4 wager
- 160 intended hands
- Ace-only midpoint switching
- Basic Strategy
- **$650,000 assumed major progressive**

The $650K figure is a **research scenario**, not a fixed live casino value. The progressive fluctuates.

### Risk/reward table

All differences below are versus **Ace-switch-only**.

| Super 4 trigger | Extra EV / session | Unable-to-continue Δ | $400+ night Δ | Lose $100+ Δ | Avg side bets / night |
|---|---:|---:|---:|---:|---:|
| +2.5 | +$24.02 | +2.90 pp | -3.35 pp | +2.40 pp | 18.88 |
| +3.0 | +$18.83 | +2.05 pp | -2.36 pp | +1.49 pp | 13.58 |
| +3.5 | +$12.60 | +1.32 pp | -1.51 pp | +0.77 pp | 8.95 |
| **+4.0** | **+$7.68** | **+0.89 pp** | **-1.02 pp** | **+0.40 pp** | **6.09** |
| +4.5 | +$2.28 | +0.53 pp | -0.60 pp | +0.14 pp | 3.76 |
| +5.0 | +$2.74 | +0.35 pp | -0.36 pp | +0.01 pp | 2.42 |

### Typical-night behavior

The aggressive thresholds produced higher simulated mean EV, but worse normal-session outcomes.

Median ending bankrolls:

- Ace-switch-only: **$475**
- +2.5: **$450**
- +3.0: **$457.50**
- +3.5: **$462.50**
- +4.0: **$467.50**
- +4.5: **$475**
- +5.0: **$475**

This is the key reason not to automatically select +2.5 merely because its long-run average was highest.

---

## 12. Final Super 4 Recommendation

At an assumed **$650,000 major progressive**:

### +2.5
Aggressive long-run EV approach.

- Highest simulated mean gain.
- Nearly 19 side bets per night.
- Meaningfully worse bankroll survival and typical-night results.
- Too aggressive for the intended social product.

### +3.0
Aggressive but more defensible.

- Strong long-run EV.
- Still materially worsens normal-session bankroll outcomes.
- Reasonable only for a player intentionally prioritizing EV over session stability.

### +3.5
Moderately aggressive.

- Retains a substantial amount of progressive opportunity.
- Risk penalty becomes much smaller.
- A legitimate alternate setting.

### +4.0
**Current balanced recommendation.**

- About 6 side bets per simulated night.
- +$7.68/session estimated improvement versus Ace-switch-only in the $650K scenario.
- 95% CI for paired P/L improvement: **+$0.88 to +$14.48**
- Unable-to-continue penalty: only **+0.89 pp**
- $400+ night penalty: **-1.02 pp**
- Lose-$100+ penalty: **+0.40 pp**

This is the current sweet spot between:

- enough opportunities to make the side quest interesting,
- meaningful positive long-run expectation under the assumed jackpot,
- and avoiding excessive bankroll bleed on ordinary nights.

### +4.5
Conservative.

- Much smaller session-risk penalty.
- Fewer opportunities.
- Progressive advantage in the 500K-session run was not clearly distinguishable from zero after accounting for jackpot variance.

### +5.0
Bankroll-preservation / rare-opportunity version.

- Very small typical-session impact.
- Only about 2.4 side bets per night.
- Too restrictive if the goal includes actually getting to play the side quest with some regularity.

---

## 13. Jackpot Sensitivity

The +4 recommendation is **not universal**.

The progressive meter changes the economics.

If the jackpot rises substantially:

- lower thresholds become more defensible,
- because more marginal Ace-rich states become +EV.

If the jackpot falls:

- higher thresholds become more appropriate,
- because more of the side bet's value must come from the underlying hand composition rather than the progressive.

A future productized version should ideally take the **current displayed jackpot** as an input and adjust the recommended threshold dynamically.

For now, the in-game research overview should explicitly state:

> The current +4 recommendation assumes a major royal progressive around $650,000.

---

## 14. Current Product Direction

### Social Strategy
Primary low-effort hand strategy.

### Basic Strategy
Higher-precision hand strategy.

### Ace Awareness / Social Advantage
Optional advanced layer:

- track Aces,
- judge shoe quality,
- evaluate the shoe near midpoint,
- consider changing tables when the remaining shoe is Ace-poor,
- selectively play Super 4 when the remaining shoe is strongly Ace-rich.

### Ace/Five
Keep as an advanced research/reference system rather than the primary recommendation.

---

## 15. Current Practical Rules

### Midpoint table-awareness rule

At roughly four decks remaining:

- **Ace surplus ≥ 0** → stay.
- **Ace surplus < 0** → consider moving to a fresh table.

### Super 4 rule under the current $650K research assumption

- **Ace surplus < +4** → skip the side bet.
- **Ace surplus ≥ +4** → $5 Super 4 becomes the current balanced social opportunity.

Alternative personalities:

- **+3 / +3.5** → more aggressive EV-seeking
- **+4** → balanced
- **+4.5 / +5** → more conservative / bankroll-preserving

---

## 16. Final Research Conclusion

The Ace research began with the question of whether a very simple count could improve a social blackjack player's decisions without turning the experience into traditional card counting.

The answer is now substantially clearer.

**Ace/Five betting works mathematically, but its wager-spread implementation is a poor fit for the intended bankroll and social experience.**

**Ace-only awareness is more useful because one low-complexity signal can support multiple decisions with a better effort/risk tradeoff.**

Its strongest uses are:

1. identifying Ace-poor shoes near midpoint,
2. supporting low-disruption table selection,
3. identifying especially Ace-rich states where Super 4 becomes more attractive.

The current combined Social Advantage recommendation is therefore:

> **Track Aces, use the midpoint rule for table awareness, and use +4 Ace surplus as the balanced Super 4 trigger when the major progressive is around $650K.**

That conclusion should remain explicitly tied to the tested assumptions. It is a research-backed practical rule, not a universal casino guarantee.

